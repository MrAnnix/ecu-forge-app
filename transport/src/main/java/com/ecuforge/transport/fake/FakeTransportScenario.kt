package com.ecuforge.transport.fake

import com.ecuforge.core.transport.TransportFailureCode

/**
 * Scripted operation names supported by the fake transport gateway.
 *
 * @property CONNECT Connect operation.
 * @property WRITE Write operation.
 * @property READ Read operation.
 * @property DISCONNECT Disconnect operation.
 */
enum class FakeTransportOperation {
    /** Connect operation. */
    CONNECT,

    /** Write operation. */
    WRITE,

    /** Read operation. */
    READ,

    /** Disconnect operation. */
    DISCONNECT,
}

/**
 * One scripted operation result in a fake transport scenario.
 *
 * @property operation Fake operation executed in this step.
 * @property success True when this scripted step should succeed.
 * @property readPayload Payload returned for read operations.
 * @property bytesWritten Number of bytes considered written in this step.
 * @property errorCode Failure code used when [success] is false.
 * @property errorMessage Human-readable failure reason used when [success] is false.
 * @property recoverable True when a failure in this step is recoverable.
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
 *
 * @property steps Ordered fake transport steps to execute.
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
