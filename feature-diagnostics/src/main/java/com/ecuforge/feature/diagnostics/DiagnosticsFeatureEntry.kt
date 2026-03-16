package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.IdentificationUiState

object DiagnosticsFeatureEntry {
    const val ROUTE: String = "diagnostics"

    suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        return DiagnosticsDemoDelegate.identifyReadOnlyDemo()
    }

    suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        return DiagnosticsDemoDelegate.identifyReadOnlyTimeoutDemo()
    }
}
