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
        val requestValidationError = validateRequest(request)
        if (requestValidationError != null) {
            return requestValidationError
        }

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

        val isModelTransportSupported =
            compatibilityGate.isModelSupportedForTransport(
                family = request.ecuFamily,
                model = parsed.model,
                endpointHint = request.endpointHint,
            )
        if (!isModelTransportSupported) {
            transportGateway.disconnect()
            return IdentificationUiState.Error(
                code = "ECU_MODEL_UNSUPPORTED",
                message =
                    "Unsupported model transport combination: " +
                        "${request.ecuFamily}/${parsed.model} on ${request.endpointHint}",
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
     * Validates identification request shape before transport access.
     */
    private fun validateRequest(request: IdentificationRequest): IdentificationUiState.Error? {
        if (request.ecuFamily.isBlank()) {
            return IdentificationUiState.Error(
                code = "REQUEST_INVALID",
                message = "ECU family is required",
            )
        }

        if (request.endpointHint.isBlank()) {
            return IdentificationUiState.Error(
                code = "REQUEST_INVALID",
                message = "Endpoint hint is required",
            )
        }

        return null
    }

    /**
     * Parses identification payload format MODEL/FW/SN into a typed model.
     */
    private fun parseIdentification(payload: ByteArray): EcuIdentification? {
        val text = payload.toString(Charsets.UTF_8).trim()
        val segments = text.split("|").map { segment -> segment.trim() }
        if (segments.size != 3) {
            return null
        }

        val parsedValues = mutableMapOf<String, String>()
        segments.forEach { segment ->
            val parts = segment.split("=", limit = 2)
            if (parts.size != 2) {
                return null
            }

            val key = parts[0].trim()
            val value = parts[1].trim()
            if (key.isBlank() || value.isBlank()) {
                return null
            }
            if (key in parsedValues) {
                return null
            }
            parsedValues[key] = value
        }

        val model = parsedValues["MODEL"]
        val firmware = parsedValues["FW"]
        val serial = parsedValues["SN"]

        if (model == null || firmware == null || serial == null) {
            return null
        }

        if (parsedValues.keys != REQUIRED_IDENTIFICATION_KEYS) {
            return null
        }

        return EcuIdentification(
            model = model,
            firmwareVersion = firmware,
            serialNumber = serial,
        )
    }

    private companion object {
        val REQUIRED_IDENTIFICATION_KEYS: Set<String> = setOf("MODEL", "FW", "SN")
    }
}
