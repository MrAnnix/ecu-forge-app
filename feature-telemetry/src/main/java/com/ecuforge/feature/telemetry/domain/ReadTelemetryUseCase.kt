package com.ecuforge.feature.telemetry.domain

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportOperationResult
import com.ecuforge.transport.TransportGateway

/**
 * Executes the read-only telemetry snapshot retrieval flow.
 */
class ReadTelemetryUseCase(
    private val transportGateway: TransportGateway,
) {
    /**
     * Runs telemetry snapshot retrieval for [request] against [endpoint] and maps outcomes to UI state.
     */
    suspend fun execute(
        request: ReadTelemetryRequest,
        endpoint: TransportEndpoint,
    ): TelemetryUiState {
        val bufferValidationError = validateBuffer(request)
        if (bufferValidationError != null) {
            return bufferValidationError
        }

        if (!isSupported(request)) {
            return TelemetryUiState.Error(
                code = "ECU_UNSUPPORTED",
                message = "Unsupported ECU family: ${request.ecuFamily}",
            )
        }

        when (val connectResult = transportGateway.connect(endpoint)) {
            is TransportOperationResult.Failure -> {
                return TelemetryUiState.Error(
                    code = connectResult.error.code.name,
                    message = connectResult.error.message,
                )
            }

            is TransportOperationResult.Success -> {
                // Continue flow
            }
        }

        return try {
            val command = buildTelemetrySnapshotCommand(request)
            when (val writeResult = transportGateway.write(command)) {
                is TransportOperationResult.Failure -> {
                    TelemetryUiState.Error(
                        code = writeResult.error.code.name,
                        message = writeResult.error.message,
                    )
                }

                is TransportOperationResult.Success -> {
                    val bufferedFrames = mutableListOf<List<TelemetrySample>>()
                    repeat(request.bufferFrameCount) {
                        when (val readResult = transportGateway.read(maxBytes = 1024)) {
                            is TransportOperationResult.Failure -> {
                                return TelemetryUiState.Error(
                                    code = readResult.error.code.name,
                                    message = readResult.error.message,
                                )
                            }

                            is TransportOperationResult.Success -> {
                                val parsed = parseTelemetryPayload(readResult.value)
                                if (parsed == null) {
                                    return TelemetryUiState.Error(
                                        code = "TELEMETRY_PARSE",
                                        message = "Invalid telemetry payload",
                                    )
                                }
                                bufferedFrames.add(parsed)
                            }
                        }
                    }

                    if (!hasStableSignalSet(bufferedFrames, request.requiredStableFrameCount)) {
                        TelemetryUiState.Error(
                            code = "TELEMETRY_UNSTABLE",
                            message = "Telemetry signal set was unstable across buffered frames",
                        )
                    } else {
                        val consolidatedSamples = bufferedFrames.last()
                        TelemetryUiState.Success(
                            samples = consolidatedSamples,
                            capturedFrameCount = bufferedFrames.size,
                            bufferedFrames = bufferedFrames.toList(),
                        )
                    }
                }
            }
        } finally {
            transportGateway.disconnect()
        }
    }

    /**
     * Validates compatibility for the initial telemetry baseline.
     */
    private fun isSupported(request: ReadTelemetryRequest): Boolean {
        return request.ecuFamily in SUPPORTED_FAMILIES
    }

    /**
     * Validates buffered sampling parameters before transport access.
     */
    private fun validateBuffer(request: ReadTelemetryRequest): TelemetryUiState.Error? {
        val invalidBufferSize = request.bufferFrameCount <= 0
        val invalidStableSize = request.requiredStableFrameCount <= 0
        val stableLargerThanBuffer = request.requiredStableFrameCount > request.bufferFrameCount

        return if (invalidBufferSize || invalidStableSize || stableLargerThanBuffer) {
            TelemetryUiState.Error(
                code = "TELEMETRY_BUFFER_INVALID",
                message =
                    "Invalid telemetry buffer configuration: " +
                        "bufferFrameCount=${request.bufferFrameCount}, " +
                        "requiredStableFrameCount=${request.requiredStableFrameCount}",
            )
        } else {
            null
        }
    }

    /**
     * Builds the raw transport command for telemetry snapshot retrieval.
     */
    private fun buildTelemetrySnapshotCommand(request: ReadTelemetryRequest): ByteArray {
        val command = "TELEMETRY_SNAPSHOT?;FAMILY=${request.ecuFamily};HINT=${request.endpointHint}"
        return command.encodeToByteArray()
    }

    /**
     * Parses telemetry payload format SIGNAL=VALUE|SIGNAL=VALUE into typed samples.
     */
    private fun parseTelemetryPayload(payload: ByteArray): List<TelemetrySample>? {
        val raw = payload.toString(Charsets.UTF_8).trim()
        if (raw.isBlank()) {
            return null
        }

        val pairs = raw.split("|").map { pair -> pair.trim() }
        if (pairs.any { pair -> pair.isEmpty() }) {
            return null
        }

        val unitBySignal =
            mapOf(
                "RPM" to "rpm",
                "TPS" to "%",
                "ECT" to "C",
                "IAT" to "C",
                "VBAT" to "V",
            )

        return pairs.map { pair ->
            val parts = pair.split("=")
            if (parts.size != 2) {
                return null
            }

            val signal = parts[0].trim()
            val numericValue = parts[1].trim().toDoubleOrNull() ?: return null
            if (signal.isBlank()) {
                return null
            }

            TelemetrySample(
                signal = signal,
                value = numericValue,
                unit = unitBySignal[signal] ?: "raw",
            )
        }
    }

    /**
     * Verifies that the last [requiredStableFrameCount] frames expose the same signal set.
     */
    private fun hasStableSignalSet(
        bufferedFrames: List<List<TelemetrySample>>,
        requiredStableFrameCount: Int,
    ): Boolean {
        if (bufferedFrames.size < requiredStableFrameCount) {
            return false
        }

        val recentFrames = bufferedFrames.takeLast(requiredStableFrameCount)
        val referenceSignalSet = recentFrames.first().map { sample -> sample.signal }.toSet()
        if (referenceSignalSet.isEmpty()) {
            return false
        }

        return recentFrames.all { frame -> frame.map { sample -> sample.signal }.toSet() == referenceSignalSet }
    }

    private companion object {
        val SUPPORTED_FAMILIES: Set<String> = setOf("KEIHIN", "WALBRO", "MARELLI", "SIEMENS")
    }
}
