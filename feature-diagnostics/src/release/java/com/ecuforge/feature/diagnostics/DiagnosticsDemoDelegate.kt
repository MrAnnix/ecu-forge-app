package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState

/**
 * Release-only demo delegate that blocks demo diagnostics entrypoints.
 */
internal object DiagnosticsDemoDelegate {
    /**
     * Returns disabled state for identification demo in release builds.
     */
    suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        return disabledState()
    }

    /**
     * Returns disabled state for identification timeout demo in release builds.
     */
    suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        return disabledState()
    }

    /**
     * Returns disabled state for DTC demo in release builds.
     */
    suspend fun readDtcReadOnlyDemo(): DtcUiState {
        return disabledDtcState()
    }

    /**
     * Returns disabled state for DTC timeout demo in release builds.
     */
    suspend fun readDtcReadOnlyTimeoutDemo(): DtcUiState {
        return disabledDtcState()
    }

    /**
     * Builds the standard disabled identification state.
     */
    private fun disabledState(): IdentificationUiState {
        return IdentificationUiState.Error(
            code = "DEMO_DISABLED",
            message = "Demo identification is disabled in release builds",
        )
    }

    /**
     * Builds the standard disabled DTC state.
     */
    private fun disabledDtcState(): DtcUiState {
        return DtcUiState.Error(
            code = "DEMO_DISABLED",
            message = "Demo DTC flow is disabled in release builds",
        )
    }
}
