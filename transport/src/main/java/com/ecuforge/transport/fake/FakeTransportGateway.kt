package com.ecuforge.transport.fake

import com.ecuforge.core.transport.TransportConnectionState
import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.core.transport.TransportOperationResult
import com.ecuforge.transport.TransportGateway

/**
 * Deterministic in-memory transport gateway driven by scripted scenario steps.
 */
class FakeTransportGateway(
    scenario: FakeTransportScenario,
) : TransportGateway {
    private val scriptedSteps: MutableList<FakeTransportStep> = scenario.steps.toMutableList()
    private var state: TransportConnectionState = TransportConnectionState.IDLE

    /**
     * Returns the current internal state of the fake connection.
     */
    override fun currentState(): TransportConnectionState = state

    /**
     * Simulates transport connect according to the next scripted CONNECT step.
     */
    override suspend fun connect(endpoint: TransportEndpoint): TransportOperationResult<Unit> {
        if (!isEndpointValid(endpoint)) {
            state = TransportConnectionState.ERROR
            return TransportOperationResult.failure(
                code = TransportFailureCode.INVALID_ENDPOINT,
                message = "Invalid endpoint",
                recoverable = false,
            )
        }

        state = TransportConnectionState.CONNECTING
        val step =
            nextStep(FakeTransportOperation.CONNECT)
                ?: return failAndSetState(
                    code = TransportFailureCode.UNSUPPORTED_OPERATION,
                    message = "No scripted CONNECT step available",
                )

        if (!step.success) {
            state = TransportConnectionState.ERROR
            return TransportOperationResult.failure(
                code = step.errorCode,
                message = step.errorMessage,
                recoverable = step.recoverable,
            )
        }

        state = TransportConnectionState.CONNECTED
        return TransportOperationResult.success(Unit)
    }

    /**
     * Simulates transport disconnect according to the next scripted DISCONNECT step.
     */
    override suspend fun disconnect(): TransportOperationResult<Unit> {
        if (state != TransportConnectionState.CONNECTED) {
            return TransportOperationResult.failure(
                code = TransportFailureCode.NOT_CONNECTED,
                message = "Transport is not connected",
                recoverable = true,
            )
        }

        state = TransportConnectionState.DISCONNECTING
        val step =
            nextStep(FakeTransportOperation.DISCONNECT)
                ?: return failAndSetState(
                    code = TransportFailureCode.UNSUPPORTED_OPERATION,
                    message = "No scripted DISCONNECT step available",
                )

        if (!step.success) {
            state = TransportConnectionState.ERROR
            return TransportOperationResult.failure(
                code = step.errorCode,
                message = step.errorMessage,
                recoverable = step.recoverable,
            )
        }

        state = TransportConnectionState.DISCONNECTED
        return TransportOperationResult.success(Unit)
    }

    /**
     * Simulates write according to the next scripted WRITE step.
     */
    override suspend fun write(payload: ByteArray): TransportOperationResult<Int> {
        if (state != TransportConnectionState.CONNECTED) {
            return TransportOperationResult.failure(
                code = TransportFailureCode.NOT_CONNECTED,
                message = "Transport is not connected",
                recoverable = true,
            )
        }

        val step =
            nextStep(FakeTransportOperation.WRITE)
                ?: return failAndSetState(
                    code = TransportFailureCode.UNSUPPORTED_OPERATION,
                    message = "No scripted WRITE step available",
                )

        if (!step.success) {
            return TransportOperationResult.failure(
                code = step.errorCode,
                message = step.errorMessage,
                recoverable = step.recoverable,
            )
        }

        val written = if (step.bytesWritten > 0) step.bytesWritten else payload.size
        return TransportOperationResult.success(written)
    }

    /**
     * Simulates read according to the next scripted READ step.
     */
    override suspend fun read(maxBytes: Int): TransportOperationResult<ByteArray> {
        if (state != TransportConnectionState.CONNECTED) {
            return TransportOperationResult.failure(
                code = TransportFailureCode.NOT_CONNECTED,
                message = "Transport is not connected",
                recoverable = true,
            )
        }

        val step =
            nextStep(FakeTransportOperation.READ)
                ?: return failAndSetState(
                    code = TransportFailureCode.UNSUPPORTED_OPERATION,
                    message = "No scripted READ step available",
                )

        if (!step.success) {
            return TransportOperationResult.failure(
                code = step.errorCode,
                message = step.errorMessage,
                recoverable = step.recoverable,
            )
        }

        val payload = step.readPayload
        return if (payload.size <= maxBytes) {
            TransportOperationResult.success(payload)
        } else {
            TransportOperationResult.success(payload.copyOf(maxBytes))
        }
    }

    /**
     * Returns the next scripted step when it matches [operation], otherwise null.
     */
    private fun nextStep(operation: FakeTransportOperation): FakeTransportStep? {
        if (scriptedSteps.isEmpty()) {
            return null
        }

        val step = scriptedSteps.removeAt(0)
        return if (step.operation == operation) {
            step
        } else {
            null
        }
    }

    /**
     * Produces a non-recoverable failure and moves the gateway to ERROR state.
     */
    private fun failAndSetState(
        code: TransportFailureCode,
        message: String,
    ): TransportOperationResult.Failure {
        state = TransportConnectionState.ERROR
        return TransportOperationResult.Failure(
            error =
                com.ecuforge.core.transport.TransportFailure(
                    code = code,
                    message = message,
                    recoverable = false,
                ),
        )
    }

    /**
     * Performs lightweight endpoint validation for scripted tests.
     */
    private fun isEndpointValid(endpoint: TransportEndpoint): Boolean {
        return when (endpoint) {
            is TransportEndpoint.Bluetooth -> endpoint.macAddress.isNotBlank()
            is TransportEndpoint.Usb -> endpoint.vendorId > 0 && endpoint.productId > 0
        }
    }
}
