package com.ecuforge.transport

import com.ecuforge.core.transport.TransportConnectionState
import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportOperationResult

interface TransportGateway {
    fun currentState(): TransportConnectionState

    suspend fun connect(endpoint: TransportEndpoint): TransportOperationResult<Unit>

    suspend fun disconnect(): TransportOperationResult<Unit>

    suspend fun write(payload: ByteArray): TransportOperationResult<Int>

    suspend fun read(maxBytes: Int = 1024): TransportOperationResult<ByteArray>
}
