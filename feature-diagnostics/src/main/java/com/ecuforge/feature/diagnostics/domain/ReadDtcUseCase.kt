package com.ecuforge.feature.diagnostics.domain

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportOperationResult
import com.ecuforge.transport.TransportGateway

/**
 * Executes the read-only DTC retrieval flow.
 */
class ReadDtcUseCase(
    private val transportGateway: TransportGateway,
    private val compatibilityGate: EcuCompatibilityGate = EcuCompatibilityGate(),
) {
    /**
     * Runs DTC retrieval for [request] against [endpoint] and maps outcomes to UI state.
     */
    suspend fun execute(
        request: ReadDtcRequest,
        endpoint: TransportEndpoint,
    ): DtcUiState {
        val requestValidationError = validateRequest(request)
        if (requestValidationError != null) {
            return requestValidationError
        }

        if (!isSupported(request)) {
            return DtcUiState.Error(
                code = "ECU_UNSUPPORTED",
                message = "Unsupported ECU family: ${request.ecuFamily}",
            )
        }

        when (val connectResult = transportGateway.connect(endpoint)) {
            is TransportOperationResult.Failure -> {
                return DtcUiState.Error(
                    code = connectResult.error.code.name,
                    message = connectResult.error.message,
                )
            }

            is TransportOperationResult.Success -> {
                // Continue flow
            }
        }

        return try {
            val readCommand = requestReadDtcPayload(request)
            when (val writeResult = transportGateway.write(readCommand)) {
                is TransportOperationResult.Failure -> {
                    DtcUiState.Error(
                        code = writeResult.error.code.name,
                        message = writeResult.error.message,
                    )
                }

                is TransportOperationResult.Success -> {
                    val readResult = transportGateway.read(maxBytes = 1024)
                    if (readResult is TransportOperationResult.Failure) {
                        DtcUiState.Error(
                            code = readResult.error.code.name,
                            message = readResult.error.message,
                        )
                    } else {
                        val payload = (readResult as TransportOperationResult.Success).value
                        val parsed = parseDtcPayload(payload)
                        if (parsed == null) {
                            DtcUiState.Error(
                                code = "DTC_PARSE",
                                message = "Invalid DTC payload",
                            )
                        } else {
                            DtcUiState.Success(parsed)
                        }
                    }
                }
            }
        } finally {
            transportGateway.disconnect()
        }
    }

    /**
     * Reuses identification gate semantics for DTC request support validation.
     */
    private fun isSupported(request: ReadDtcRequest): Boolean {
        val compatibilityRequest =
            IdentificationRequest(
                ecuFamily = request.ecuFamily,
                endpointHint = request.endpointHint,
            )
        return compatibilityGate.isSupported(compatibilityRequest)
    }

    /**
     * Builds the raw transport command for DTC retrieval.
     */
    private fun requestReadDtcPayload(request: ReadDtcRequest): ByteArray {
        val command = "DTC?;FAMILY=${request.ecuFamily};HINT=${request.endpointHint}"
        return command.encodeToByteArray()
    }

    /**
     * Validates DTC request shape before transport access.
     */
    private fun validateRequest(request: ReadDtcRequest): DtcUiState.Error? {
        if (request.ecuFamily.isBlank()) {
            return DtcUiState.Error(
                code = "REQUEST_INVALID",
                message = "ECU family is required",
            )
        }

        if (request.endpointHint.isBlank()) {
            return DtcUiState.Error(
                code = "REQUEST_INVALID",
                message = "Endpoint hint is required",
            )
        }

        return null
    }

    /**
     * Parses DTC payload format CODE;DESCRIPTION|CODE;DESCRIPTION into typed records.
     */
    private fun parseDtcPayload(payload: ByteArray): List<DtcRecord>? {
        val raw = payload.toString(Charsets.UTF_8).trim()
        if (raw == "NONE") {
            return emptyList()
        }
        if (raw.isBlank()) {
            return null
        }

        val records = raw.split("|").map { token -> token.trim() }
        if (records.any { token -> token.isEmpty() }) {
            return null
        }

        return records.map { recordText ->
            val fields = recordText.split(";")
            if (fields.size != 2) {
                return null
            }

            val code = fields[0].trim()
            val description = fields[1].trim()
            if (code.isBlank() || description.isBlank()) {
                return null
            }

            DtcRecord(code = code, description = description)
        }
    }
}
