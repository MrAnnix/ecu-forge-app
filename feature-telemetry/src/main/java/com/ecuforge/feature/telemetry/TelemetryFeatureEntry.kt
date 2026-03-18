package com.ecuforge.feature.telemetry

import com.ecuforge.feature.telemetry.domain.TelemetryUiState

/**
 * Public entrypoint constants for the telemetry feature module.
 */
object TelemetryFeatureEntry {
    /**
     * Navigation route used by the app shell to open telemetry flows.
     */
    const val ROUTE: String = "telemetry"

    /**
     * Runs the read-only telemetry snapshot demo flow.
     */
    suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState {
        return TelemetryDemoDelegate.readTelemetryReadOnlyDemo()
    }

    /**
     * Runs the read-only telemetry timeout demo flow.
     */
    suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState {
        return TelemetryDemoDelegate.readTelemetryReadOnlyTimeoutDemo()
    }
}
