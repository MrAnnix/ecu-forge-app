package com.ecuforge.feature.telemetry

import com.ecuforge.feature.telemetry.domain.TelemetryUiState

/**
 * Contract used to provide telemetry flows behind a stable app-facing entrypoint.
 */
interface TelemetryFlowProvider {
    /**
     * Executes read-only telemetry flow.
     */
    suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState

    /**
     * Executes read-only telemetry timeout flow.
     */
    suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState
}

/**
 * Optional configuration contract for telemetry providers that support transport switching.
 */
interface TelemetryTransportConfigurableProvider {
    /**
     * Updates the active read-only telemetry transport path.
     */
    fun configureReadOnlyTransport(transport: TelemetryReadOnlyTransport)
}

/**
 * Optional configuration contract for telemetry providers that support full profile overrides.
 */
interface TelemetryProfileConfigurableProvider {
    /**
     * Updates the active read-only telemetry profile.
     */
    fun configureReadOnlyProfile(profile: TelemetryReadOnlyProfile)
}

/**
 * Optional configuration contract for providers that accept primitive user transport settings.
 */
interface TelemetryConnectionSettingsConfigurableProvider {
    /**
     * Updates active read-only telemetry connection settings.
     */
    fun configureReadOnlyConnectionSettings(settings: TelemetryReadOnlyConnectionSettings)
}

/**
 * Supported transport selections for read-only telemetry flows.
 */
enum class TelemetryReadOnlyTransport {
    /** Bluetooth ELM327-compatible adapter path. */
    BLUETOOTH,

    /** USB cable ELM327-compatible adapter path. */
    USB,

    /** WiFi ELM327-compatible adapter path. */
    WIFI,
}

/**
 * Primitive telemetry transport settings captured from app UI configuration.
 *
 * @property transport Selected read-only transport type.
 * @property bluetoothMacAddress Bluetooth MAC value for Bluetooth transport.
 * @property usbVendorId USB vendor ID for USB transport.
 * @property usbProductId USB product ID for USB transport.
 * @property wifiHost WiFi host for WiFi transport.
 * @property wifiPort WiFi port for WiFi transport.
 */
data class TelemetryReadOnlyConnectionSettings(
    val transport: TelemetryReadOnlyTransport,
    val bluetoothMacAddress: String? = null,
    val usbVendorId: Int? = null,
    val usbProductId: Int? = null,
    val wifiHost: String? = null,
    val wifiPort: Int? = null,
)

/**
 * Public entrypoint constants for the telemetry feature module.
 */
object TelemetryFeatureEntry {
    @Volatile
    private var flowProvider: TelemetryFlowProvider = TelemetryDemoDelegate

    /**
     * Navigation route used by the app shell to open telemetry flows.
     */
    const val ROUTE: String = "telemetry"

    /**
     * Installs a telemetry flow provider without changing app call sites.
     */
    fun installProvider(provider: TelemetryFlowProvider) {
        flowProvider = provider
    }

    /**
     * Configures the active read-only transport when the installed provider supports it.
     *
     * @return true when transport was applied by the active provider.
     */
    fun configureReadOnlyTransport(transport: TelemetryReadOnlyTransport): Boolean {
        val configurableProvider = flowProvider as? TelemetryTransportConfigurableProvider
        return if (configurableProvider == null) {
            false
        } else {
            configurableProvider.configureReadOnlyTransport(transport)
            true
        }
    }

    /**
     * Applies an explicit read-only telemetry profile when supported by the active provider.
     *
     * @return true when profile was applied by the active provider.
     */
    fun configureReadOnlyProfile(profile: TelemetryReadOnlyProfile): Boolean {
        val configurableProvider = flowProvider as? TelemetryProfileConfigurableProvider
        return if (configurableProvider == null) {
            false
        } else {
            configurableProvider.configureReadOnlyProfile(profile)
            true
        }
    }

    /**
     * Applies primitive telemetry connection settings when supported by the active provider.
     *
     * @return true when settings were applied by the active provider.
     */
    fun configureReadOnlyConnectionSettings(settings: TelemetryReadOnlyConnectionSettings): Boolean {
        val configurableProvider = flowProvider as? TelemetryConnectionSettingsConfigurableProvider
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
        flowProvider = TelemetryDemoDelegate
    }

    /**
     * Runs the read-only telemetry snapshot demo flow.
     */
    suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState {
        return flowProvider.readTelemetryReadOnlyDemo()
    }

    /**
     * Runs the read-only telemetry timeout demo flow.
     */
    suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState {
        return flowProvider.readTelemetryReadOnlyTimeoutDemo()
    }
}
