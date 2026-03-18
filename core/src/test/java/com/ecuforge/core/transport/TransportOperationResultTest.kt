package com.ecuforge.core.transport

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TransportOperationResultTest {
    @Test
    fun successFactoryReturnsSuccessResult() {
        val result = TransportOperationResult.success(7)

        assertThat(result)
            .describedAs("Success factory should return a Success result type")
            .isInstanceOf(TransportOperationResult.Success::class.java)
        val success = result as TransportOperationResult.Success
        assertThat(success.value)
            .describedAs("Success result should preserve the provided payload value")
            .isEqualTo(7)
    }

    @Test
    fun failureFactoryReturnsFailureResult() {
        val result =
            TransportOperationResult.failure(
                code = TransportFailureCode.TIMEOUT,
                message = "Read timeout",
                recoverable = true,
            )

        assertThat(result)
            .describedAs("Failure factory should return a Failure result type")
            .isInstanceOf(TransportOperationResult.Failure::class.java)
        val failure = (result as TransportOperationResult.Failure).error
        assertThat(failure.code)
            .describedAs("Failure result should preserve the failure code")
            .isEqualTo(TransportFailureCode.TIMEOUT)
        assertThat(failure.message)
            .describedAs("Failure result should preserve the failure message")
            .isEqualTo("Read timeout")
        assertThat(failure.recoverable)
            .describedAs("Failure result should preserve recoverable flag")
            .isTrue()
    }

    @Test
    fun nonRecoverableFailureCanBeDeclared() {
        val result =
            TransportOperationResult.failure(
                code = TransportFailureCode.CONNECTION_FAILED,
                message = "Could not establish link",
                recoverable = false,
            )

        assertThat(result)
            .describedAs("Non-recoverable failure declaration should still produce a Failure result type")
            .isInstanceOf(TransportOperationResult.Failure::class.java)
        val failure = (result as TransportOperationResult.Failure).error
        assertThat(failure.recoverable)
            .describedAs("Failure should remain marked as non-recoverable when explicitly configured")
            .isFalse()
    }
}
