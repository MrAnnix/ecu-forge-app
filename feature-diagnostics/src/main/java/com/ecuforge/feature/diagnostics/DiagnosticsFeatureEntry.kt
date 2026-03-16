package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState

/**
 * Public entrypoint for diagnostics feature flows used by the app layer.
 */
object DiagnosticsFeatureEntry {
    /**
     * Navigation route used by host screens.
     */
    const val ROUTE: String = "diagnostics"

    /**
     * Runs the read-only identification demo flow.
     */
    suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        return DiagnosticsDemoDelegate.identifyReadOnlyDemo()
    }

    /**
     * Runs the read-only identification timeout demo flow.
     */
    suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        return DiagnosticsDemoDelegate.identifyReadOnlyTimeoutDemo()
    }

    /**
     * Runs the read-only DTC demo flow.
     */
    suspend fun readDtcReadOnlyDemo(): DtcUiState {
        return DiagnosticsDemoDelegate.readDtcReadOnlyDemo()
    }

    /**
     * Runs the read-only DTC timeout demo flow.
     */
    suspend fun readDtcReadOnlyTimeoutDemo(): DtcUiState {
        return DiagnosticsDemoDelegate.readDtcReadOnlyTimeoutDemo()
    }
}
