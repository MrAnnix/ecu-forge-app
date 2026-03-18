package com.ecuforge.feature.telemetry.domain

/**
 * UI state model for read-only telemetry snapshot retrieval.
 */
sealed interface TelemetryUiState {
    /**
     * Initial idle state before any action is triggered.
     */
    data object Idle : TelemetryUiState

    /**
     * State emitted while telemetry snapshot retrieval is running.
     */
    data object Loading : TelemetryUiState

    /**
     * Terminal success state with one snapshot of telemetry samples.
     */
    data class Success(
        val samples: List<TelemetrySample>,
    ) : TelemetryUiState

    /**
     * Terminal failure state with stable error code and message.
     */
    data class Error(
        val code: String,
        val message: String,
    ) : TelemetryUiState
}
