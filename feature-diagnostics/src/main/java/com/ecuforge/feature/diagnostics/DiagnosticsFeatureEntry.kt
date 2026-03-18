package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext

/**
 * Contract used to provide diagnostics flows behind a stable app-facing entrypoint.
 */
interface DiagnosticsFlowProvider {
    /**
     * Executes read-only identification flow.
     */
    suspend fun identifyReadOnlyDemo(): IdentificationUiState

    /**
     * Executes read-only identification timeout flow.
     */
    suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState

    /**
     * Executes read-only DTC flow.
     */
    suspend fun readDtcReadOnlyDemo(): DtcUiState

    /**
     * Executes read-only DTC flow with optional vehicle-aware catalog enrichment.
     */
    suspend fun readDtcReadOnlyDemo(
        vehicleCatalogContext: VehicleCatalogContext?,
        preferCatalogDescriptions: Boolean,
    ): DtcUiState

    /**
     * Executes read-only DTC timeout flow.
     */
    suspend fun readDtcReadOnlyTimeoutDemo(): DtcUiState
}

/**
 * Public entrypoint for diagnostics feature flows used by the app layer.
 */
object DiagnosticsFeatureEntry {
    @Volatile
    private var flowProvider: DiagnosticsFlowProvider = DiagnosticsDemoDelegate

    /**
     * Navigation route used by host screens.
     */
    const val ROUTE: String = "diagnostics"

    /**
     * Installs a diagnostics flow provider without changing app call sites.
     */
    fun installProvider(provider: DiagnosticsFlowProvider) {
        flowProvider = provider
    }

    /**
     * Restores the default variant provider.
     */
    fun resetProvider() {
        flowProvider = DiagnosticsDemoDelegate
    }

    /**
     * Runs the read-only identification demo flow.
     */
    suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        return flowProvider.identifyReadOnlyDemo()
    }

    /**
     * Runs the read-only identification timeout demo flow.
     */
    suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        return flowProvider.identifyReadOnlyTimeoutDemo()
    }

    /**
     * Runs the read-only DTC demo flow.
     */
    suspend fun readDtcReadOnlyDemo(): DtcUiState {
        return flowProvider.readDtcReadOnlyDemo()
    }

    /**
     * Runs the read-only DTC demo flow with optional vehicle context and catalog preference.
     */
    suspend fun readDtcReadOnlyDemo(
        vehicleCatalogContext: VehicleCatalogContext?,
        preferCatalogDescriptions: Boolean,
    ): DtcUiState {
        return flowProvider.readDtcReadOnlyDemo(
            vehicleCatalogContext = vehicleCatalogContext,
            preferCatalogDescriptions = preferCatalogDescriptions,
        )
    }

    /**
     * Runs the read-only DTC timeout demo flow.
     */
    suspend fun readDtcReadOnlyTimeoutDemo(): DtcUiState {
        return flowProvider.readDtcReadOnlyTimeoutDemo()
    }
}
