package com.ecuforge.transport.fake

import com.ecuforge.core.transport.TransportFailureCode

enum class FakeTransportOperation {
    CONNECT,
    WRITE,
    READ,
    DISCONNECT
}

data class FakeTransportStep(
    val operation: FakeTransportOperation,
    val success: Boolean = true,
    val readPayload: ByteArray = byteArrayOf(),
    val bytesWritten: Int = 0,
    val errorCode: TransportFailureCode = TransportFailureCode.IO_ERROR,
    val errorMessage: String = "operation failed",
    val recoverable: Boolean = true
)

class FakeTransportScenario private constructor(
    val steps: List<FakeTransportStep>
) {
    companion object {
        fun of(vararg steps: FakeTransportStep): FakeTransportScenario {
            return FakeTransportScenario(steps.toList())
        }
    }
}
