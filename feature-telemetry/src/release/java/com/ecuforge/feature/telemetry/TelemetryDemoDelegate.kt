package com.ecuforge.feature.telemetry

import com.ecuforge.feature.telemetry.domain.TelemetryUiState

/**
 * Release-only demo delegate that blocks telemetry demo entrypoints.
 */
internal object TelemetryDemoDelegate : TelemetryFlowProvider {
    /**
     * Returns disabled state for telemetry demo in release builds.
     */
    override suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState {
        return disabledTelemetryState()
    }

    /**
     * Returns disabled state for telemetry timeout demo in release builds.
     */
    override suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState {
        return disabledTelemetryState()
    }

    /**
     * Builds the standard disabled telemetry state.
     */
    private fun disabledTelemetryState(): TelemetryUiState {
        return TelemetryUiState.Error(
            code = "DEMO_DISABLED",
            message = "Demo telemetry flow is disabled in release builds",
        )
    }
}
