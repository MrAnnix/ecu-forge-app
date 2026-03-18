package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext

/**
 * Release-only demo delegate that blocks demo diagnostics entrypoints.
 */
internal object DiagnosticsDemoDelegate : DiagnosticsFlowProvider {
    /**
     * Returns disabled state for identification demo in release builds.
     */
    override suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        return disabledState()
    }

    /**
     * Returns disabled state for identification timeout demo in release builds.
     */
    override suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        return disabledState()
    }

    /**
     * Returns disabled state for DTC demo in release builds.
     */
    override suspend fun readDtcReadOnlyDemo(): DtcUiState {
        return disabledDtcState()
    }

    /**
     * Returns disabled state for catalog-aware DTC demo in release builds.
     */
    override suspend fun readDtcReadOnlyDemo(
        vehicleCatalogContext: VehicleCatalogContext?,
        preferCatalogDescriptions: Boolean,
    ): DtcUiState {
        return disabledDtcState()
    }

    /**
     * Returns disabled state for DTC timeout demo in release builds.
     */
    override suspend fun readDtcReadOnlyTimeoutDemo(): DtcUiState {
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
