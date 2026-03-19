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
 * Optional configuration contract for diagnostics providers that support transport switching.
 */
interface DiagnosticsTransportConfigurableProvider {
    /**
     * Updates the active read-only transport path.
     */
    fun configureReadOnlyTransport(transport: DiagnosticsReadOnlyTransport)
}

/**
 * Optional configuration contract for diagnostics providers that support full profile overrides.
 */
interface DiagnosticsProfileConfigurableProvider {
    /**
     * Updates the active read-only diagnostics profile.
     */
    fun configureReadOnlyProfile(profile: DiagnosticsReadOnlyProfile)
}

/**
 * Optional configuration contract for providers that accept primitive user transport settings.
 */
interface DiagnosticsConnectionSettingsConfigurableProvider {
    /**
     * Updates active read-only connection settings.
     */
    fun configureReadOnlyConnectionSettings(settings: DiagnosticsReadOnlyConnectionSettings)
}

/**
 * Supported transport selections for read-only diagnostics flows.
 */
enum class DiagnosticsReadOnlyTransport {
    /** Bluetooth ELM327-compatible adapter path. */
    BLUETOOTH,

    /** USB cable ELM327-compatible adapter path. */
    USB,

    /** WiFi ELM327-compatible adapter path. */
    WIFI,
}

/**
 * Primitive diagnostics transport settings captured from app UI configuration.
 *
 * @property transport Selected read-only transport type.
 * @property bluetoothMacAddress Bluetooth MAC value for Bluetooth transport.
 * @property usbVendorId USB vendor ID for USB transport.
 * @property usbProductId USB product ID for USB transport.
 * @property wifiHost WiFi host for WiFi transport.
 * @property wifiPort WiFi port for WiFi transport.
 */
data class DiagnosticsReadOnlyConnectionSettings(
    val transport: DiagnosticsReadOnlyTransport,
    val bluetoothMacAddress: String? = null,
    val usbVendorId: Int? = null,
    val usbProductId: Int? = null,
    val wifiHost: String? = null,
    val wifiPort: Int? = null,
)

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
     * Configures the active read-only transport when the installed provider supports it.
     *
     * @return true when transport was applied by the active provider.
     */
    fun configureReadOnlyTransport(transport: DiagnosticsReadOnlyTransport): Boolean {
        val configurableProvider = flowProvider as? DiagnosticsTransportConfigurableProvider
        return if (configurableProvider == null) {
            false
        } else {
            configurableProvider.configureReadOnlyTransport(transport)
            true
        }
    }

    /**
     * Applies an explicit read-only diagnostics profile when supported by the active provider.
     *
     * @return true when profile was applied by the active provider.
     */
    fun configureReadOnlyProfile(profile: DiagnosticsReadOnlyProfile): Boolean {
        val configurableProvider = flowProvider as? DiagnosticsProfileConfigurableProvider
        return if (configurableProvider == null) {
            false
        } else {
            configurableProvider.configureReadOnlyProfile(profile)
            true
        }
    }

    /**
     * Applies primitive connection settings when supported by the active provider.
     *
     * @return true when settings were applied by the active provider.
     */
    fun configureReadOnlyConnectionSettings(settings: DiagnosticsReadOnlyConnectionSettings): Boolean {
        val configurableProvider = flowProvider as? DiagnosticsConnectionSettingsConfigurableProvider
        return if (configurableProvider == null) {
            false
        } else {
            configurableProvider.configureReadOnlyConnectionSettings(settings)
            true
        }
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
