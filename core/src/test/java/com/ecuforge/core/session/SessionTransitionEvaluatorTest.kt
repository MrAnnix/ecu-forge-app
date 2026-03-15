package com.ecuforge.core.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTransitionEvaluatorTest {

    @Test
    fun startFromIdleMovesToInitializing() {
        val transition = SessionTransitionEvaluator.transition(SessionState.IDLE, SessionEvent.START)

        assertTrue(transition.allowed)
        assertEquals(SessionState.INITIALIZING, transition.to)
    }

    @Test
    fun invalidTransitionIsRejectedWithReason() {
        val transition = SessionTransitionEvaluator.transition(SessionState.IDLE, SessionEvent.READ_REQUESTED)

        assertFalse(transition.allowed)
        assertEquals(SessionState.IDLE, transition.to)
        assertTrue(transition.reason?.contains("Invalid transition") == true)
        assertEquals(SessionTransitionErrorCode.INVALID_TRANSITION, transition.errorCode)
    }

    @Test
    fun transportLostFromReadingMovesToDisconnected() {
        val transition = SessionTransitionEvaluator.transition(SessionState.READING, SessionEvent.TRANSPORT_LOST)

        assertTrue(transition.allowed)
        assertEquals(SessionState.DISCONNECTED, transition.to)
    }

    @Test
    fun resetFromFailedMovesToIdle() {
        val transition = SessionTransitionEvaluator.transition(SessionState.FAILED, SessionEvent.RESET)

        assertTrue(transition.allowed)
        assertEquals(SessionState.IDLE, transition.to)
    }

    @Test
    fun disconnectedGuardRejectsReadRequested() {
        val transition = SessionTransitionEvaluator.transition(
            current = SessionState.READY,
            event = SessionEvent.READ_REQUESTED,
            guardInput = SessionTransitionGuardInput(transportConnected = false)
        )

        assertFalse(transition.allowed)
        assertEquals(SessionState.READY, transition.to)
        assertEquals(SessionTransitionErrorCode.TRANSPORT_NOT_CONNECTED, transition.errorCode)
    }

    @Test
    fun retryRequestedUnderLimitMovesToRetryWait() {
        val transition = SessionTransitionEvaluator.transition(
            current = SessionState.READING,
            event = SessionEvent.RETRY_REQUESTED,
            guardInput = SessionTransitionGuardInput(retryCount = 1, maxRetries = 3)
        )

        assertTrue(transition.allowed)
        assertEquals(SessionState.RETRY_WAIT, transition.to)
        assertEquals(null, transition.errorCode)
    }

    @Test
    fun retryRequestedAtLimitForcesFailed() {
        val transition = SessionTransitionEvaluator.transition(
            current = SessionState.READING,
            event = SessionEvent.RETRY_REQUESTED,
            guardInput = SessionTransitionGuardInput(retryCount = 3, maxRetries = 3)
        )

        assertTrue(transition.allowed)
        assertEquals(SessionState.FAILED, transition.to)
        assertEquals(SessionTransitionErrorCode.RETRY_LIMIT_REACHED, transition.errorCode)
    }

    @Test
    fun startFromDisconnectedMovesToInitializing() {
        val transition = SessionTransitionEvaluator.transition(
            current = SessionState.DISCONNECTED,
            event = SessionEvent.START
        )

        assertTrue(transition.allowed)
        assertEquals(SessionState.INITIALIZING, transition.to)
    }
}
