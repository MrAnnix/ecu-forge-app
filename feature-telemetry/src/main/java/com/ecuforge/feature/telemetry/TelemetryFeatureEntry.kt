package com.ecuforge.feature.telemetry

import com.ecuforge.feature.telemetry.domain.TelemetryUiState

/**
 * Contract used to provide telemetry flows behind a stable app-facing entrypoint.
 */
interface TelemetryFlowProvider {
    /**
     * Executes read-only telemetry flow.
     */
    suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState

    /**
     * Executes read-only telemetry timeout flow.
     */
    suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState
}

/**
 * Public entrypoint constants for the telemetry feature module.
 */
object TelemetryFeatureEntry {
    @Volatile
    private var flowProvider: TelemetryFlowProvider = TelemetryDemoDelegate

    /**
     * Navigation route used by the app shell to open telemetry flows.
     */
    const val ROUTE: String = "telemetry"

    /**
     * Installs a telemetry flow provider without changing app call sites.
     */
    fun installProvider(provider: TelemetryFlowProvider) {
        flowProvider = provider
    }

    /**
     * Restores the default variant provider.
     */
    fun resetProvider() {
        flowProvider = TelemetryDemoDelegate
    }

    /**
     * Runs the read-only telemetry snapshot demo flow.
     */
    suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState {
        return flowProvider.readTelemetryReadOnlyDemo()
    }

    /**
     * Runs the read-only telemetry timeout demo flow.
     */
    suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState {
        return flowProvider.readTelemetryReadOnlyTimeoutDemo()
    }
}
