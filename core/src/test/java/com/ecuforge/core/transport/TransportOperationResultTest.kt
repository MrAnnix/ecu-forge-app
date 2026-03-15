package com.ecuforge.core.transport

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransportOperationResultTest {

    @Test
    fun successFactoryReturnsSuccessResult() {
        val result = TransportOperationResult.success(7)

        assertTrue(result is TransportOperationResult.Success)
        assertEquals(7, (result as TransportOperationResult.Success).value)
    }

    @Test
    fun failureFactoryReturnsFailureResult() {
        val result = TransportOperationResult.failure(
            code = TransportFailureCode.TIMEOUT,
            message = "Read timeout",
            recoverable = true
        )

        assertTrue(result is TransportOperationResult.Failure)
        val failure = (result as TransportOperationResult.Failure).error
        assertEquals(TransportFailureCode.TIMEOUT, failure.code)
        assertEquals("Read timeout", failure.message)
        assertTrue(failure.recoverable)
    }

    @Test
    fun nonRecoverableFailureCanBeDeclared() {
        val result = TransportOperationResult.failure(
            code = TransportFailureCode.CONNECTION_FAILED,
            message = "Could not establish link",
            recoverable = false
        )

        assertTrue(result is TransportOperationResult.Failure)
        val failure = (result as TransportOperationResult.Failure).error
        assertFalse(failure.recoverable)
    }
}
