package com.ecuforge.transport.fake

import com.ecuforge.core.transport.TransportFailureCode

/**
 * Scripted operation names supported by the fake transport gateway.
 */
enum class FakeTransportOperation {
    CONNECT,
    WRITE,
    READ,
    DISCONNECT,
}

/**
 * One scripted operation result in a fake transport scenario.
 */
data class FakeTransportStep(
    val operation: FakeTransportOperation,
    val success: Boolean = true,
    val readPayload: ByteArray = byteArrayOf(),
    val bytesWritten: Int = 0,
    val errorCode: TransportFailureCode = TransportFailureCode.IO_ERROR,
    val errorMessage: String = "operation failed",
    val recoverable: Boolean = true,
)

/**
 * Immutable scripted scenario consumed by [FakeTransportGateway].
 */
class FakeTransportScenario private constructor(
    val steps: List<FakeTransportStep>,
) {
    /**
     * Factory helpers for creating immutable fake transport scenarios.
     */
    companion object {
        /**
         * Builds a scenario from an ordered list of scripted steps.
         */
        fun of(vararg steps: FakeTransportStep): FakeTransportScenario {
            return FakeTransportScenario(steps.toList())
        }
    }
}
