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
                    when (val readResult = transportGateway.read(maxBytes = 1024)) {
                        is TransportOperationResult.Failure -> {
                            TelemetryUiState.Error(
                                code = readResult.error.code.name,
                                message = readResult.error.message,
                            )
                        }

                        is TransportOperationResult.Success -> {
                            val parsed = parseTelemetryPayload(readResult.value)
                            if (parsed == null) {
                                TelemetryUiState.Error(
                                    code = "TELEMETRY_PARSE",
                                    message = "Invalid telemetry payload",
                                )
                            } else {
                                TelemetryUiState.Success(parsed)
                            }
                        }
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

    private companion object {
        val SUPPORTED_FAMILIES: Set<String> = setOf("KEIHIN", "WALBRO", "MARELLI", "SIEMENS")
    }
}
