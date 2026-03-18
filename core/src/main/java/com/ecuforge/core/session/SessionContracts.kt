package com.ecuforge.core.session

/**
 * High-level lifecycle states for an ECU session.
 *
 * @property IDLE Session is inactive and ready to start.
 * @property INITIALIZING Session is running startup initialization steps.
 * @property AUTHENTICATING Session is running authentication/handshake flow.
 * @property READY Session is ready to execute read-only operations.
 * @property READING Session is actively reading data from ECU.
 * @property RETRY_WAIT Session is waiting before retrying a failed operation.
 * @property DISCONNECTED Session lost transport connectivity.
 * @property FAILED Session ended in a failure state awaiting reset.
 */
enum class SessionState {
    /** Session is inactive and ready to start. */
    IDLE,

    /** Session is running startup initialization steps. */
    INITIALIZING,

    /** Session is running authentication/handshake flow. */
    AUTHENTICATING,

    /** Session is ready to execute read-only operations. */
    READY,

    /** Session is actively reading data from ECU. */
    READING,

    /** Session is waiting before retrying a failed operation. */
    RETRY_WAIT,

    /** Session lost transport connectivity. */
    DISCONNECTED,

    /** Session ended in a failure state awaiting reset. */
    FAILED,
}

/**
 * Events that can trigger session state transitions.
 *
 * @property START Starts a new session attempt.
 * @property INIT_OK Initialization completed successfully.
 * @property AUTH_OK Authentication completed successfully.
 * @property READ_REQUESTED Read operation has been requested.
 * @property READ_COMPLETED Read operation completed successfully.
 * @property TRANSPORT_LOST Transport connection was lost.
 * @property RETRY_REQUESTED Retry was requested after a failure.
 * @property RETRY_TIMEOUT Retry wait timeout elapsed.
 * @property RESET Session is reset back to idle.
 * @property FAIL Session transitions to failed state.
 */
enum class SessionEvent {
    /** Starts a new session attempt. */
    START,

    /** Initialization completed successfully. */
    INIT_OK,

    /** Authentication completed successfully. */
    AUTH_OK,

    /** Read operation has been requested. */
    READ_REQUESTED,

    /** Read operation completed successfully. */
    READ_COMPLETED,

    /** Transport connection was lost. */
    TRANSPORT_LOST,

    /** Retry was requested after a failure. */
    RETRY_REQUESTED,

    /** Retry wait timeout elapsed. */
    RETRY_TIMEOUT,

    /** Session is reset back to idle. */
    RESET,

    /** Session transitions to failed state. */
    FAIL,
}

/**
 * Canonical error codes for rejected or degraded transitions.
 *
 * @property INVALID_TRANSITION Current state and event combination is invalid.
 * @property TRANSPORT_NOT_CONNECTED Transition requires an active transport connection.
 * @property RETRY_LIMIT_REACHED Retry count exceeded configured retry limit.
 */
enum class SessionTransitionErrorCode {
    /** Current state and event combination is invalid. */
    INVALID_TRANSITION,

    /** Transition requires an active transport connection. */
    TRANSPORT_NOT_CONNECTED,

    /** Retry count exceeded configured retry limit. */
    RETRY_LIMIT_REACHED,
}

/**
 * Guard input evaluated before accepting a transition.
 *
 * @property transportConnected True when transport is currently connected.
 * @property retryCount Current retry attempt count.
 * @property maxRetries Maximum retry attempts allowed.
 */
data class SessionTransitionGuardInput(
    val transportConnected: Boolean = true,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
)

/**
 * Result of evaluating a state transition.
 *
 * @property from Source session state before evaluating transition.
 * @property event Event used to evaluate transition.
 * @property to Target session state after evaluating transition.
 * @property allowed True when transition is accepted.
 * @property reason Optional human-readable reason for rejection or forced path.
 * @property errorCode Optional canonical error code for rejected/forced transitions.
 */
data class SessionTransition(
    val from: SessionState,
    val event: SessionEvent,
    val to: SessionState,
    val allowed: Boolean,
    val reason: String? = null,
    val errorCode: SessionTransitionErrorCode? = null,
)

/**
 * Evaluates session transitions and enforces guard rules.
 */
object SessionTransitionEvaluator {
    /**
     * Computes the next transition from [current] and [event] using [guardInput].
     */
    fun transition(
        current: SessionState,
        event: SessionEvent,
        guardInput: SessionTransitionGuardInput = SessionTransitionGuardInput(),
    ): SessionTransition {
        if (!guardInput.transportConnected && requiresConnection(event)) {
            return rejected(
                current = current,
                event = event,
                reason = "Transport not connected for event: $event",
                errorCode = SessionTransitionErrorCode.TRANSPORT_NOT_CONNECTED,
            )
        }

        val next =
            when (current) {
                SessionState.IDLE ->
                    when (event) {
                        SessionEvent.START -> SessionState.INITIALIZING
                        SessionEvent.RESET -> SessionState.IDLE
                        SessionEvent.FAIL -> SessionState.FAILED
                        else -> null
                    }

                SessionState.INITIALIZING ->
                    when (event) {
                        SessionEvent.INIT_OK -> SessionState.AUTHENTICATING
                        SessionEvent.TRANSPORT_LOST -> SessionState.DISCONNECTED
                        SessionEvent.FAIL -> SessionState.FAILED
                        else -> null
                    }

                SessionState.AUTHENTICATING ->
                    when (event) {
                        SessionEvent.AUTH_OK -> SessionState.READY
                        SessionEvent.TRANSPORT_LOST -> SessionState.DISCONNECTED
                        SessionEvent.FAIL -> SessionState.FAILED
                        else -> null
                    }

                SessionState.READY ->
                    when (event) {
                        SessionEvent.READ_REQUESTED -> SessionState.READING
                        SessionEvent.TRANSPORT_LOST -> SessionState.DISCONNECTED
                        SessionEvent.FAIL -> SessionState.FAILED
                        SessionEvent.RESET -> SessionState.IDLE
                        else -> null
                    }

                SessionState.READING ->
                    when (event) {
                        SessionEvent.READ_COMPLETED -> SessionState.READY
                        SessionEvent.RETRY_REQUESTED -> {
                            if (guardInput.retryCount < guardInput.maxRetries) {
                                SessionState.RETRY_WAIT
                            } else {
                                return forcedFailure(
                                    from = current,
                                    event = event,
                                    reason = "Retry limit reached: ${guardInput.retryCount}/${guardInput.maxRetries}",
                                    errorCode = SessionTransitionErrorCode.RETRY_LIMIT_REACHED,
                                )
                            }
                        }
                        SessionEvent.TRANSPORT_LOST -> SessionState.DISCONNECTED
                        SessionEvent.FAIL -> SessionState.FAILED
                        else -> null
                    }

                SessionState.RETRY_WAIT ->
                    when (event) {
                        SessionEvent.RETRY_TIMEOUT -> SessionState.READING
                        SessionEvent.TRANSPORT_LOST -> SessionState.DISCONNECTED
                        SessionEvent.FAIL -> SessionState.FAILED
                        SessionEvent.RESET -> SessionState.IDLE
                        else -> null
                    }

                SessionState.DISCONNECTED ->
                    when (event) {
                        SessionEvent.START -> SessionState.INITIALIZING
                        SessionEvent.RESET -> SessionState.IDLE
                        SessionEvent.FAIL -> SessionState.FAILED
                        else -> null
                    }

                SessionState.FAILED ->
                    when (event) {
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
                errorCode = SessionTransitionErrorCode.INVALID_TRANSITION,
            )
        }

        return SessionTransition(
            from = current,
            event = event,
            to = next,
            allowed = true,
            reason = null,
            errorCode = null,
        )
    }

    /**
     * Returns true when [event] requires an active transport connection.
     */
    private fun requiresConnection(event: SessionEvent): Boolean {
        return when (event) {
            SessionEvent.INIT_OK,
            SessionEvent.AUTH_OK,
            SessionEvent.READ_REQUESTED,
            SessionEvent.READ_COMPLETED,
            SessionEvent.RETRY_REQUESTED,
            SessionEvent.RETRY_TIMEOUT,
            -> true

            else -> false
        }
    }

    /**
     * Produces a rejected transition that keeps the current state unchanged.
     */
    private fun rejected(
        current: SessionState,
        event: SessionEvent,
        reason: String,
        errorCode: SessionTransitionErrorCode,
    ): SessionTransition {
        return SessionTransition(
            from = current,
            event = event,
            to = current,
            allowed = false,
            reason = reason,
            errorCode = errorCode,
        )
    }

    /**
     * Produces a forced transition to FAILED while preserving diagnostics context.
     */
    private fun forcedFailure(
        from: SessionState,
        event: SessionEvent,
        reason: String,
        errorCode: SessionTransitionErrorCode,
    ): SessionTransition {
        return SessionTransition(
            from = from,
            event = event,
            to = SessionState.FAILED,
            allowed = true,
            reason = reason,
            errorCode = errorCode,
        )
    }
}
