package com.ecuforge.feature.telemetry

import com.ecuforge.core.transport.TransportConnectionState
import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.core.transport.TransportOperationResult
import com.ecuforge.transport.TransportGateway

/**
 * Debug-only concrete Bluetooth telemetry gateway for transport-backed provider piloting.
 *
 * @property behavior Deterministic behavior profile for nominal and failure-path testing.
 */
internal class DebugBluetoothTelemetryTransportGateway(
    private val behavior: Behavior = Behavior.NOMINAL,
) : TransportGateway {
    private var state: TransportConnectionState = TransportConnectionState.IDLE
    private var lastCommand: String? = null
    private var readCursor: Int = 0

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

    override fun currentState(): TransportConnectionState = state

    /**
     * Connects only to Bluetooth endpoints in debug pilot mode.
     */
    override suspend fun connect(endpoint: TransportEndpoint): TransportOperationResult<Unit> {
        if (endpoint !is TransportEndpoint.Bluetooth) {
            state = TransportConnectionState.ERROR
            return TransportOperationResult.failure(
                code = TransportFailureCode.INVALID_ENDPOINT,
                message = "Debug Bluetooth telemetry gateway requires a Bluetooth endpoint",
                recoverable = false,
            )
        }

        state = TransportConnectionState.CONNECTING
        if (behavior == Behavior.CONNECT_FAILURE) {
            state = TransportConnectionState.ERROR
            return TransportOperationResult.failure(
                code = TransportFailureCode.CONNECTION_FAILED,
                message = "Debug Bluetooth telemetry connection failed",
            )
        }

        state = TransportConnectionState.CONNECTED
        return TransportOperationResult.success(Unit)
    }

    override suspend fun disconnect(): TransportOperationResult<Unit> {
        state = TransportConnectionState.DISCONNECTING
        lastCommand = null
        readCursor = 0
        state = TransportConnectionState.DISCONNECTED
        return TransportOperationResult.success(Unit)
    }

    override suspend fun write(payload: ByteArray): TransportOperationResult<Int> {
        if (state != TransportConnectionState.CONNECTED) {
            return TransportOperationResult.failure(
                code = TransportFailureCode.NOT_CONNECTED,
                message = "Debug Bluetooth telemetry gateway is not connected",
            )
        }

        lastCommand = payload.toString(Charsets.UTF_8)
        readCursor = 0
        return TransportOperationResult.success(payload.size)
    }

    override suspend fun read(maxBytes: Int): TransportOperationResult<ByteArray> {
        if (state != TransportConnectionState.CONNECTED) {
            return TransportOperationResult.failure(
                code = TransportFailureCode.NOT_CONNECTED,
                message = "Debug Bluetooth telemetry gateway is not connected",
            )
        }

        if (behavior == Behavior.READ_TIMEOUT) {
            return TransportOperationResult.failure(
                code = TransportFailureCode.TIMEOUT,
                message = "Debug Bluetooth telemetry read timeout",
            )
        }

        val command = lastCommand.orEmpty()
        if (!command.startsWith("TELEMETRY_SNAPSHOT?")) {
            return TransportOperationResult.failure(
                code = TransportFailureCode.UNSUPPORTED_OPERATION,
                message = "Debug Bluetooth telemetry gateway cannot respond to command: $command",
                recoverable = false,
            )
        }

        val frames =
            listOf(
                "RPM=1440|TPS=2.0|ECT=81.8|VBAT=13.8",
                "RPM=1450|TPS=2.1|ECT=82.0|VBAT=13.9",
                "RPM=1460|TPS=2.2|ECT=82.1|VBAT=13.9",
            )
        val frame = frames[minOf(readCursor, frames.lastIndex)]
        readCursor += 1
        val payload = frame.encodeToByteArray().take(maxBytes.coerceAtLeast(1)).toByteArray()
        return TransportOperationResult.success(payload)
    }
}
