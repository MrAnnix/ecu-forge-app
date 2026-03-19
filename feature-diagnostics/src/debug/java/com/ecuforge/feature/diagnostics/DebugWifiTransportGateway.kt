package com.ecuforge.feature.diagnostics

import com.ecuforge.core.transport.TransportConnectionState
import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.core.transport.TransportOperationResult
import com.ecuforge.transport.TransportGateway

/**
 * Debug-only concrete WiFi gateway used to pilot transport-backed diagnostics flows.
 *
 * @property behavior Deterministic behavior profile for nominal and failure-path testing.
 */
internal class DebugWifiTransportGateway(
    private val behavior: Behavior = Behavior.NOMINAL,
) : TransportGateway {
    private var state: TransportConnectionState = TransportConnectionState.IDLE
    private var lastCommand: String? = null

    /**
     * Behavior profiles for deterministic debug execution.
     */
    internal enum class Behavior {
        /** Successful connect/write/read/disconnect behavior. */
        NOMINAL,

        /** Connection fails at connect boundary. */
        CONNECT_FAILURE,

        /** Read operation fails with timeout. */
        READ_TIMEOUT,
    }

    /**
     * Returns current state for this gateway instance.
     */
    override fun currentState(): TransportConnectionState = state

    /**
     * Connects only to WiFi endpoints in debug pilot mode.
     */
    override suspend fun connect(endpoint: TransportEndpoint): TransportOperationResult<Unit> {
        if (endpoint !is TransportEndpoint.Wifi) {
            state = TransportConnectionState.ERROR
            return TransportOperationResult.failure(
                code = TransportFailureCode.INVALID_ENDPOINT,
                message = "Debug WiFi gateway requires a WiFi endpoint",
                recoverable = false,
            )
        }

        state = TransportConnectionState.CONNECTING
        if (behavior == Behavior.CONNECT_FAILURE) {
            state = TransportConnectionState.ERROR
            return TransportOperationResult.failure(
                code = TransportFailureCode.CONNECTION_FAILED,
                message = "Debug WiFi connection failed",
            )
        }

        state = TransportConnectionState.CONNECTED
        return TransportOperationResult.success(Unit)
    }

    /**
     * Disconnects and clears the last written command.
     */
    override suspend fun disconnect(): TransportOperationResult<Unit> {
        state = TransportConnectionState.DISCONNECTING
        lastCommand = null
        state = TransportConnectionState.DISCONNECTED
        return TransportOperationResult.success(Unit)
    }

    /**
     * Stores command payload for deterministic read response synthesis.
     */
    override suspend fun write(payload: ByteArray): TransportOperationResult<Int> {
        if (state != TransportConnectionState.CONNECTED) {
            return TransportOperationResult.failure(
                code = TransportFailureCode.NOT_CONNECTED,
                message = "Debug WiFi gateway is not connected",
            )
        }

        lastCommand = payload.toString(Charsets.UTF_8)
        return TransportOperationResult.success(payload.size)
    }

    /**
     * Returns deterministic response payload based on the most recent command.
     */
    override suspend fun read(maxBytes: Int): TransportOperationResult<ByteArray> {
        if (state != TransportConnectionState.CONNECTED) {
            return TransportOperationResult.failure(
                code = TransportFailureCode.NOT_CONNECTED,
                message = "Debug WiFi gateway is not connected",
            )
        }

        if (behavior == Behavior.READ_TIMEOUT) {
            return TransportOperationResult.failure(
                code = TransportFailureCode.TIMEOUT,
                message = "Debug WiFi read timeout",
            )
        }

        val command = lastCommand.orEmpty()
        val payload =
            when {
                command.startsWith("ID?") -> "MODEL=KM601EU|FW=2.10.4|SN=A1B2C3".encodeToByteArray()
                command.startsWith("DTC?") -> "P0130;O2 Sensor Circuit|P0301;Cylinder 1 Misfire".encodeToByteArray()
                else -> {
                    return TransportOperationResult.failure(
                        code = TransportFailureCode.UNSUPPORTED_OPERATION,
                        message = "Debug WiFi gateway cannot respond to command: $command",
                        recoverable = false,
                    )
                }
            }

        val boundedPayload = payload.take(maxBytes.coerceAtLeast(1)).toByteArray()
        return TransportOperationResult.success(boundedPayload)
    }
}
