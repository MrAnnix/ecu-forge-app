package com.ecuforge.transport

interface TransportGateway {
    suspend fun connect(endpoint: String): Result<Unit>
    suspend fun disconnect(): Result<Unit>
}
