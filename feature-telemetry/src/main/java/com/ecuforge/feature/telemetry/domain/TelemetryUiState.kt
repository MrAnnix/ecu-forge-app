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
     * Terminal success state with consolidated samples and buffered frame context.
     *
     * @property samples Consolidated telemetry samples shown to the user.
     * @property capturedFrameCount Number of raw frames captured during acquisition.
     * @property bufferedFrames Rolling buffered frames kept for diagnostics context.
     */
    data class Success(
        val samples: List<TelemetrySample>,
        val capturedFrameCount: Int,
        val bufferedFrames: List<List<TelemetrySample>>,
    ) : TelemetryUiState

    /**
     * Terminal failure state with stable error code and message.
     *
     * @property code Stable machine-readable error code.
     * @property message Human-readable error message for UI.
     */
    data class Error(
        val code: String,
        val message: String,
    ) : TelemetryUiState
}
