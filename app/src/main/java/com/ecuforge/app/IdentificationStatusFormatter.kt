package com.ecuforge.app

import com.ecuforge.feature.diagnostics.domain.IdentificationUiState

object IdentificationStatusFormatter {
    fun format(state: IdentificationUiState): String {
        return when (state) {
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
}

