package com.ecuforge.core.session

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SessionTransitionEvaluatorTest {
    @Test
    fun startFromIdleMovesToInitializing() {
        val transition = SessionTransitionEvaluator.transition(SessionState.IDLE, SessionEvent.START)

        assertThat(transition.allowed)
            .describedAs("START event from IDLE should be allowed")
            .isTrue()
        assertThat(transition.to)
            .describedAs("START event from IDLE should move state to INITIALIZING")
            .isEqualTo(SessionState.INITIALIZING)
    }

    @Test
    fun invalidTransitionIsRejectedWithReason() {
        val transition = SessionTransitionEvaluator.transition(SessionState.IDLE, SessionEvent.READ_REQUESTED)

        assertThat(transition.allowed)
            .describedAs("READ_REQUESTED from IDLE should be rejected")
            .isFalse()
        assertThat(transition.to)
            .describedAs("Rejected transition should keep current state")
            .isEqualTo(SessionState.IDLE)
        assertThat(transition.reason)
            .describedAs("Rejected transition should include an invalid transition reason")
            .contains("Invalid transition")
        assertThat(transition.errorCode)
            .describedAs("Rejected transition should expose INVALID_TRANSITION error code")
            .isEqualTo(SessionTransitionErrorCode.INVALID_TRANSITION)
    }

    @Test
    fun transportLostFromReadingMovesToDisconnected() {
        val transition = SessionTransitionEvaluator.transition(SessionState.READING, SessionEvent.TRANSPORT_LOST)

        assertThat(transition.allowed)
            .describedAs("TRANSPORT_LOST from READING should be allowed")
            .isTrue()
        assertThat(transition.to)
            .describedAs("TRANSPORT_LOST from READING should move state to DISCONNECTED")
            .isEqualTo(SessionState.DISCONNECTED)
    }

    @Test
    fun resetFromFailedMovesToIdle() {
        val transition = SessionTransitionEvaluator.transition(SessionState.FAILED, SessionEvent.RESET)

        assertThat(transition.allowed)
            .describedAs("RESET from FAILED should be allowed")
            .isTrue()
        assertThat(transition.to)
            .describedAs("RESET from FAILED should move state to IDLE")
            .isEqualTo(SessionState.IDLE)
    }

    @Test
    fun disconnectedGuardRejectsReadRequested() {
        val transition =
            SessionTransitionEvaluator.transition(
                current = SessionState.READY,
                event = SessionEvent.READ_REQUESTED,
                guardInput = SessionTransitionGuardInput(transportConnected = false),
            )

        assertThat(transition.allowed)
            .describedAs("READ_REQUESTED should be rejected when transport is disconnected")
            .isFalse()
        assertThat(transition.to)
            .describedAs("Guard rejection should keep READY state")
            .isEqualTo(SessionState.READY)
        assertThat(transition.errorCode)
            .describedAs("Guard rejection should expose TRANSPORT_NOT_CONNECTED error code")
            .isEqualTo(SessionTransitionErrorCode.TRANSPORT_NOT_CONNECTED)
    }

    @Test
    fun retryRequestedUnderLimitMovesToRetryWait() {
        val transition =
            SessionTransitionEvaluator.transition(
                current = SessionState.READING,
                event = SessionEvent.RETRY_REQUESTED,
                guardInput = SessionTransitionGuardInput(retryCount = 1, maxRetries = 3),
            )

        assertThat(transition.allowed)
            .describedAs("RETRY_REQUESTED below max retries should be allowed")
            .isTrue()
        assertThat(transition.to)
            .describedAs("Allowed retry should move state to RETRY_WAIT")
            .isEqualTo(SessionState.RETRY_WAIT)
        assertThat(transition.errorCode)
            .describedAs("Allowed retry should not set an error code")
            .isNull()
    }

    @Test
    fun retryRequestedAtLimitForcesFailed() {
        val transition =
            SessionTransitionEvaluator.transition(
                current = SessionState.READING,
                event = SessionEvent.RETRY_REQUESTED,
                guardInput = SessionTransitionGuardInput(retryCount = 3, maxRetries = 3),
            )

        assertThat(transition.allowed)
            .describedAs("RETRY_REQUESTED at max retries should still be handled")
            .isTrue()
        assertThat(transition.to)
            .describedAs("Retry at limit should move state to FAILED")
            .isEqualTo(SessionState.FAILED)
        assertThat(transition.errorCode)
            .describedAs("Retry at limit should expose RETRY_LIMIT_REACHED error code")
            .isEqualTo(SessionTransitionErrorCode.RETRY_LIMIT_REACHED)
    }

    @Test
    fun startFromDisconnectedMovesToInitializing() {
        val transition =
            SessionTransitionEvaluator.transition(
                current = SessionState.DISCONNECTED,
                event = SessionEvent.START,
            )

        assertThat(transition.allowed)
            .describedAs("START from DISCONNECTED should be allowed")
            .isTrue()
        assertThat(transition.to)
            .describedAs("START from DISCONNECTED should move state to INITIALIZING")
            .isEqualTo(SessionState.INITIALIZING)
    }
}
