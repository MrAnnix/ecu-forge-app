package com.ecuforge.core.session

enum class SessionState {
    IDLE,
    INITIALIZING,
    AUTHENTICATING,
    READY,
    READING,
    RETRY_WAIT,
    DISCONNECTED,
    FAILED
}

enum class SessionEvent {
    START,
    INIT_OK,
    AUTH_OK,
    READ_REQUESTED,
    READ_COMPLETED,
    TRANSPORT_LOST,
    RETRY_REQUESTED,
    RETRY_TIMEOUT,
    RESET,
    FAIL
}

enum class SessionTransitionErrorCode {
    INVALID_TRANSITION,
    TRANSPORT_NOT_CONNECTED,
    RETRY_LIMIT_REACHED
}

data class SessionTransitionGuardInput(
    val transportConnected: Boolean = true,
    val retryCount: Int = 0,
    val maxRetries: Int = 3
)

data class SessionTransition(
    val from: SessionState,
    val event: SessionEvent,
    val to: SessionState,
    val allowed: Boolean,
    val reason: String? = null,
    val errorCode: SessionTransitionErrorCode? = null
)

object SessionTransitionEvaluator {
    fun transition(
        current: SessionState,
        event: SessionEvent,
        guardInput: SessionTransitionGuardInput = SessionTransitionGuardInput()
    ): SessionTransition {
        if (!guardInput.transportConnected && requiresConnection(event)) {
            return rejected(
                current = current,
                event = event,
                reason = "Transport not connected for event: $event",
                errorCode = SessionTransitionErrorCode.TRANSPORT_NOT_CONNECTED
            )
        }

        val next = when (current) {
            SessionState.IDLE -> when (event) {
                SessionEvent.START -> SessionState.INITIALIZING
                SessionEvent.RESET -> SessionState.IDLE
                SessionEvent.FAIL -> SessionState.FAILED
                else -> null
            }

            SessionState.INITIALIZING -> when (event) {
                SessionEvent.INIT_OK -> SessionState.AUTHENTICATING
                SessionEvent.TRANSPORT_LOST -> SessionState.DISCONNECTED
                SessionEvent.FAIL -> SessionState.FAILED
                else -> null
            }

            SessionState.AUTHENTICATING -> when (event) {
                SessionEvent.AUTH_OK -> SessionState.READY
                SessionEvent.TRANSPORT_LOST -> SessionState.DISCONNECTED
                SessionEvent.FAIL -> SessionState.FAILED
                else -> null
            }

            SessionState.READY -> when (event) {
                SessionEvent.READ_REQUESTED -> SessionState.READING
                SessionEvent.TRANSPORT_LOST -> SessionState.DISCONNECTED
                SessionEvent.FAIL -> SessionState.FAILED
                SessionEvent.RESET -> SessionState.IDLE
                else -> null
            }

            SessionState.READING -> when (event) {
                SessionEvent.READ_COMPLETED -> SessionState.READY
                SessionEvent.RETRY_REQUESTED -> {
                    if (guardInput.retryCount < guardInput.maxRetries) {
                        SessionState.RETRY_WAIT
                    } else {
                        return forcedFailure(
                            from = current,
                            event = event,
                            reason = "Retry limit reached: ${guardInput.retryCount}/${guardInput.maxRetries}",
                            errorCode = SessionTransitionErrorCode.RETRY_LIMIT_REACHED
                        )
                    }
                }
                SessionEvent.TRANSPORT_LOST -> SessionState.DISCONNECTED
                SessionEvent.FAIL -> SessionState.FAILED
                else -> null
            }

            SessionState.RETRY_WAIT -> when (event) {
                SessionEvent.RETRY_TIMEOUT -> SessionState.READING
                SessionEvent.TRANSPORT_LOST -> SessionState.DISCONNECTED
                SessionEvent.FAIL -> SessionState.FAILED
                SessionEvent.RESET -> SessionState.IDLE
                else -> null
            }

            SessionState.DISCONNECTED -> when (event) {
                SessionEvent.START -> SessionState.INITIALIZING
                SessionEvent.RESET -> SessionState.IDLE
                SessionEvent.FAIL -> SessionState.FAILED
                else -> null
            }

            SessionState.FAILED -> when (event) {
                SessionEvent.RESET -> SessionState.IDLE
                SessionEvent.FAIL -> SessionState.FAILED
                else -> null
            }
        }

        if (next == null) {
            return rejected(
                current = current,
                event = event,
                reason = "Invalid transition: $current + $event",
                errorCode = SessionTransitionErrorCode.INVALID_TRANSITION
            )
        }

        return SessionTransition(
            from = current,
            event = event,
            to = next,
            allowed = true,
            reason = null,
            errorCode = null
        )
    }

    private fun requiresConnection(event: SessionEvent): Boolean {
        return when (event) {
            SessionEvent.INIT_OK,
            SessionEvent.AUTH_OK,
            SessionEvent.READ_REQUESTED,
            SessionEvent.READ_COMPLETED,
            SessionEvent.RETRY_REQUESTED,
            SessionEvent.RETRY_TIMEOUT -> true

            else -> false
        }
    }

    private fun rejected(
        current: SessionState,
        event: SessionEvent,
        reason: String,
        errorCode: SessionTransitionErrorCode
    ): SessionTransition {
        return SessionTransition(
            from = current,
            event = event,
            to = current,
            allowed = false,
            reason = reason,
            errorCode = errorCode
        )
    }

    private fun forcedFailure(
        from: SessionState,
        event: SessionEvent,
        reason: String,
        errorCode: SessionTransitionErrorCode
    ): SessionTransition {
        return SessionTransition(
            from = from,
            event = event,
            to = SessionState.FAILED,
            allowed = true,
            reason = reason,
            errorCode = errorCode
        )
    }
}
