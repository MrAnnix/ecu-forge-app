package com.ecuforge.app

import com.ecuforge.feature.telemetry.domain.TelemetryUiState

/**
 * Maps telemetry UI states to user-facing status text.
 */
object TelemetryStatusFormatter {
    /**
     * Returns a display string for the provided telemetry [state].
     */
    fun format(state: TelemetryUiState): String =
        when (state) {
            TelemetryUiState.Idle -> "Ready for read-only telemetry snapshot."
            TelemetryUiState.Loading -> "Reading telemetry snapshot..."
            is TelemetryUiState.Success -> {
                if (state.samples.isEmpty()) {
                    "Telemetry snapshot returned no samples."
                } else {
                    val summary =
                        state.samples.joinToString(separator = " | ") { sample ->
                            "${sample.signal}=${sample.value}${sample.unit}"
                        }
                    "Telemetry (frames=${state.capturedFrameCount}): $summary"
                }
            }
            is TelemetryUiState.Error -> "Telemetry retrieval failed (${state.code}): ${state.message}"
        }
}
