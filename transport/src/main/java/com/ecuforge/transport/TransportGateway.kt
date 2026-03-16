package com.ecuforge.transport

import com.ecuforge.core.transport.TransportConnectionState
import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportOperationResult

/**
 * Stable abstraction for ECU transport adapters.
 */
interface TransportGateway {
    /**
     * Returns the last known connection state for this gateway instance.
     */
    fun currentState(): TransportConnectionState

    /**
     * Establishes a connection to the provided endpoint.
     */
    suspend fun connect(endpoint: TransportEndpoint): TransportOperationResult<Unit>

    /**
     * Closes the active connection.
     */
    suspend fun disconnect(): TransportOperationResult<Unit>

    /**
     * Writes raw payload bytes to the transport channel.
     */
    suspend fun write(payload: ByteArray): TransportOperationResult<Int>

    /**
     * Reads up to [maxBytes] bytes from the transport channel.
     */
    suspend fun read(maxBytes: Int = 1024): TransportOperationResult<ByteArray>
}
