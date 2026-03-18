package com.ecuforge.app

import com.ecuforge.feature.diagnostics.domain.DtcUiState

/**
 * Maps DTC UI states to user-facing status text.
 */
object DtcStatusFormatter {
    /**
     * Returns a display string for the provided DTC [state].
     */
    fun format(state: DtcUiState): String =
        when (state) {
            DtcUiState.Idle -> "Ready for read-only DTC retrieval."
            DtcUiState.Loading -> "Reading diagnostic trouble codes..."
            is DtcUiState.Success -> {
                if (state.dtcs.isEmpty()) {
                    "No active DTC codes reported by ECU."
                } else {
                    val records = state.dtcs.joinToString(separator = " | ") { dtc -> "${dtc.code}: ${dtc.description}" }
                    "DTCs: $records"
                }
            }
            is DtcUiState.Error -> "DTC retrieval failed (${state.code}): ${state.message}"
        }
}
