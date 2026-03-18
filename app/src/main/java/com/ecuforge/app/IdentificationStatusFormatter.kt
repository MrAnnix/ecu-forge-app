package com.ecuforge.app

import com.ecuforge.feature.diagnostics.domain.IdentificationUiState

/**
 * Maps identification UI states to user-facing status text.
 */
object IdentificationStatusFormatter {
    /**
     * Returns a display string for the provided identification [state].
     */
    fun format(state: IdentificationUiState): String =
        when (state) {
            IdentificationUiState.Idle -> "Ready for read-only ECU identification."
            IdentificationUiState.Loading -> "Reading ECU identification..."
            is IdentificationUiState.Success -> {
                val identification = state.identification
                "ECU ${identification.model} | FW ${identification.firmwareVersion} | SN ${identification.serialNumber}"
            }

            is IdentificationUiState.Error -> {
                "Identification failed (${state.code}): ${state.message}"
            }
        }
}
