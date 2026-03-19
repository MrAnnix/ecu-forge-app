package com.ecuforge.app

/**
 * Evaluates which read-only actions are available for the current flow state.
 */
internal object ReadOnlyActionAvailability {
    /**
     * Returns deterministic button availability for read-only actions.
     */
    fun evaluate(state: State): Result {
        val isIdentificationDependentActionEnabled = state.hasSuccessfulIdentification
        return Result(
            isReadDtcEnabled = isIdentificationDependentActionEnabled,
            isReadTelemetryEnabled = isIdentificationDependentActionEnabled,
        )
    }

    /**
     * Input state required to determine action availability.
     *
     * @property hasSuccessfulIdentification Whether identification succeeded in the current flow.
     */
    data class State(
        val hasSuccessfulIdentification: Boolean,
    )

    /**
     * Resolved read-only action availability.
     *
     * @property isReadDtcEnabled Whether the DTC read action should be enabled.
     * @property isReadTelemetryEnabled Whether the telemetry read action should be enabled.
     */
    data class Result(
        val isReadDtcEnabled: Boolean,
        val isReadTelemetryEnabled: Boolean,
    )
}

