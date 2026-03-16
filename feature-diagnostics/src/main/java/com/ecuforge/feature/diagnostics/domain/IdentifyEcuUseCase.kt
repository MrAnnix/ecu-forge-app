package com.ecuforge.feature.diagnostics.domain

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportOperationResult
import com.ecuforge.transport.TransportGateway

/**
 * Executes the read-only ECU identification flow.
 */
class IdentifyEcuUseCase(
    private val transportGateway: TransportGateway,
    private val compatibilityGate: EcuCompatibilityGate = EcuCompatibilityGate(),
) {
    /**
     * Runs identification for [request] against [endpoint] and maps outcomes to UI state.
     */
    suspend fun execute(
        request: IdentificationRequest,
        endpoint: TransportEndpoint,
    ): IdentificationUiState {
        if (!compatibilityGate.isSupported(request)) {
            return IdentificationUiState.Error(
                code = "ECU_UNSUPPORTED",
                message = "Unsupported ECU family: ${request.ecuFamily}",
            )
        }

        when (val connectResult = transportGateway.connect(endpoint)) {
            is TransportOperationResult.Failure -> {
                return IdentificationUiState.Error(
                    code = connectResult.error.code.name,
                    message = connectResult.error.message,
                )
            }

            is TransportOperationResult.Success -> {
                // Continue flow
            }
        }

        val probePayload = requestIdentificationPayload(request)
        when (val writeResult = transportGateway.write(probePayload)) {
            is TransportOperationResult.Failure -> {
                transportGateway.disconnect()
                return IdentificationUiState.Error(
                    code = writeResult.error.code.name,
                    message = writeResult.error.message,
                )
            }

            is TransportOperationResult.Success -> {
                // Continue flow
            }
        }

        val readResult = transportGateway.read(maxBytes = 512)
        if (readResult is TransportOperationResult.Failure) {
            transportGateway.disconnect()
            return IdentificationUiState.Error(
                code = readResult.error.code.name,
                message = readResult.error.message,
            )
        }

        val rawResponse = (readResult as TransportOperationResult.Success).value
        val parsed = parseIdentification(rawResponse)
        if (parsed == null) {
            transportGateway.disconnect()
            return IdentificationUiState.Error(
                code = "IDENT_PARSE",
                message = "Invalid identification payload",
            )
        }

        transportGateway.disconnect()
        return IdentificationUiState.Success(parsed)
    }

    /**
     * Builds the raw transport command for identification.
     */
    private fun requestIdentificationPayload(request: IdentificationRequest): ByteArray {
        val command = "ID?;FAMILY=${request.ecuFamily};HINT=${request.endpointHint}"
        return command.encodeToByteArray()
    }

    /**
     * Parses identification payload format MODEL/FW/SN into a typed model.
     */
    private fun parseIdentification(payload: ByteArray): EcuIdentification? {
        val text = payload.toString(Charsets.UTF_8)
        val segments = text.split("|")
        if (segments.size != 3) {
            return null
        }

        val model = segments[0].removePrefix("MODEL=")
        val firmware = segments[1].removePrefix("FW=")
        val serial = segments[2].removePrefix("SN=")

        if (model == segments[0] || firmware == segments[1] || serial == segments[2]) {
            return null
        }

        if (model.isBlank() || firmware.isBlank() || serial.isBlank()) {
            return null
        }

        return EcuIdentification(
            model = model,
            firmwareVersion = firmware,
            serialNumber = serial,
        )
    }
}
