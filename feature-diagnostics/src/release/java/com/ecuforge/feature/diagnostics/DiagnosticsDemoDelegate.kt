package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.IdentificationUiState

internal object DiagnosticsDemoDelegate {

    suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        return disabledState()
    }

    suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        return disabledState()
    }

    private fun disabledState(): IdentificationUiState {
        return IdentificationUiState.Error(
            code = "DEMO_DISABLED",
            message = "Demo identification is disabled in release builds"
        )
    }
}

