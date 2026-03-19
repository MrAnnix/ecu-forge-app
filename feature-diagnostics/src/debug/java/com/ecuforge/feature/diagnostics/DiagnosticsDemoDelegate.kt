package com.ecuforge.feature.diagnostics

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.feature.diagnostics.data.IndexedDtcCatalogRepository
import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationRequest
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.diagnostics.domain.IdentifyEcuUseCase
import com.ecuforge.feature.diagnostics.domain.ReadDtcRequest
import com.ecuforge.feature.diagnostics.domain.ReadDtcUseCase
import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext

/**
 * Debug-only demo delegate that wires diagnostics flows to fake transport scenarios.
 */
internal object DiagnosticsDemoDelegate :
    DiagnosticsFlowProvider,
    DiagnosticsTransportConfigurableProvider,
    DiagnosticsProfileConfigurableProvider,
    DiagnosticsConnectionSettingsConfigurableProvider {
    @Volatile
    private var selectedTransport: DiagnosticsReadOnlyTransport = DiagnosticsReadOnlyTransport.USB

    @Volatile
    private var configuredProfile: DiagnosticsReadOnlyProfile? = null

    private val dtcCatalogRepository: IndexedDtcCatalogRepository = IndexedDtcCatalogRepository()

    /**
     * Updates active debug transport profile from app-selected transport.
     */
    override fun configureReadOnlyTransport(transport: DiagnosticsReadOnlyTransport) {
        selectedTransport = transport
        configuredProfile = null
    }

    /**
     * Updates active debug profile from app-provided transport settings.
     */
    override fun configureReadOnlyProfile(profile: DiagnosticsReadOnlyProfile) {
        configuredProfile = profile
    }

    /**
     * Updates profile override using primitive transport connection settings.
     */
    override fun configureReadOnlyConnectionSettings(settings: DiagnosticsReadOnlyConnectionSettings) {
        val profile =
            when (settings.transport) {
                DiagnosticsReadOnlyTransport.BLUETOOTH ->
                    DiagnosticsReadOnlyProfile(
                        ecuFamily = "KEIHIN",
                        endpointHint = "BLUETOOTH",
                        endpoint =
                            TransportEndpoint.Bluetooth(
                                macAddress = settings.bluetoothMacAddress ?: "00:11:22:33:44:55",
                            ),
                    )

                DiagnosticsReadOnlyTransport.USB ->
                    DiagnosticsReadOnlyProfile(
                        ecuFamily = "KEIHIN",
                        endpointHint = "USB",
                        endpoint =
                            TransportEndpoint.Usb(
                                vendorId = settings.usbVendorId ?: 1027,
                                productId = settings.usbProductId ?: 48960,
                            ),
                    )

                DiagnosticsReadOnlyTransport.WIFI ->
                    DiagnosticsReadOnlyProfile(
                        ecuFamily = "KEIHIN",
                        endpointHint = "WIFI",
                        endpoint =
                            TransportEndpoint.Wifi(
                                host = settings.wifiHost ?: "192.168.0.10",
                                port = settings.wifiPort ?: 35000,
                            ),
                    )
            }

        configureReadOnlyProfile(profile)
    }

    /**
     * Executes the happy-path identification demo.
     */
    override suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        return providerForSelectedTransport().identifyReadOnlyDemo()
    }

    /**
     * Executes identification demo that simulates a read timeout.
     */
    override suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        val useCase =
            IdentifyEcuUseCase(
                transportGateway = timeoutGatewayForSelectedTransport(),
            )

        val profile = profileForSelectedTransport()

        return useCase.execute(
            request =
                IdentificationRequest(
                    ecuFamily = profile.ecuFamily,
                    endpointHint = profile.endpointHint,
                ),
            endpoint = profile.endpoint,
        )
    }

    /**
     * Executes the happy-path DTC read demo.
     */
    override suspend fun readDtcReadOnlyDemo(): DtcUiState {
        return providerForSelectedTransport().readDtcReadOnlyDemo()
    }

    /**
     * Executes the happy-path DTC read demo with optional catalog context.
     */
    override suspend fun readDtcReadOnlyDemo(
        vehicleCatalogContext: VehicleCatalogContext?,
        preferCatalogDescriptions: Boolean,
    ): DtcUiState {
        return providerForSelectedTransport().readDtcReadOnlyDemo(
            vehicleCatalogContext = vehicleCatalogContext,
            preferCatalogDescriptions = preferCatalogDescriptions,
        )
    }

    /**
     * Executes DTC read demo that simulates a read timeout.
     */
    override suspend fun readDtcReadOnlyTimeoutDemo(): DtcUiState {
        val useCase =
            ReadDtcUseCase(
                transportGateway = timeoutGatewayForSelectedTransport(),
                dtcCatalogRepository = dtcCatalogRepository,
            )

        val profile = profileForSelectedTransport()

        return useCase.execute(
            request =
                ReadDtcRequest(
                    ecuFamily = profile.ecuFamily,
                    endpointHint = profile.endpointHint,
                ),
            endpoint = profile.endpoint,
        )
    }

    private fun providerForSelectedTransport(): TransportBackedDiagnosticsFlowProvider {
        return TransportBackedDiagnosticsFlowProvider(
            transportGatewayFactory = {
                nominalGatewayForSelectedTransport()
            },
            profile = profileForSelectedTransport(),
            dtcCatalogRepository = dtcCatalogRepository,
        )
    }

    private fun profileForSelectedTransport(): DiagnosticsReadOnlyProfile {
        configuredProfile?.let { profile ->
            return profile
        }

        return when (selectedTransport) {
            DiagnosticsReadOnlyTransport.BLUETOOTH ->
                DiagnosticsReadOnlyProfile(
                    ecuFamily = "KEIHIN",
                    endpointHint = "BLUETOOTH",
                    endpoint = TransportEndpoint.Bluetooth(macAddress = "00:11:22:33:44:55"),
                )

            DiagnosticsReadOnlyTransport.USB ->
                DiagnosticsReadOnlyProfile(
                    ecuFamily = "KEIHIN",
                    endpointHint = "USB",
                    endpoint = TransportEndpoint.Usb(vendorId = 1027, productId = 48960),
                )

            DiagnosticsReadOnlyTransport.WIFI ->
                DiagnosticsReadOnlyProfile(
                    ecuFamily = "KEIHIN",
                    endpointHint = "WIFI",
                    endpoint = TransportEndpoint.Wifi(host = "192.168.0.10", port = 35000),
                )
        }
    }

    private fun nominalGatewayForSelectedTransport() =
        when (activeGatewayTransport()) {
            DiagnosticsReadOnlyTransport.BLUETOOTH ->
                DebugBluetoothTransportGateway(
                    behavior = DebugBluetoothTransportGateway.Behavior.NOMINAL,
                )

            DiagnosticsReadOnlyTransport.USB ->
                DebugUsbTransportGateway(
                    behavior = DebugUsbTransportGateway.Behavior.NOMINAL,
                )

            DiagnosticsReadOnlyTransport.WIFI ->
                DebugWifiTransportGateway(
                    behavior = DebugWifiTransportGateway.Behavior.NOMINAL,
                )
        }

    private fun timeoutGatewayForSelectedTransport() =
        when (activeGatewayTransport()) {
            DiagnosticsReadOnlyTransport.BLUETOOTH ->
                DebugBluetoothTransportGateway(
                    behavior = DebugBluetoothTransportGateway.Behavior.READ_TIMEOUT,
                )

            DiagnosticsReadOnlyTransport.USB ->
                DebugUsbTransportGateway(
                    behavior = DebugUsbTransportGateway.Behavior.READ_TIMEOUT,
                )

            DiagnosticsReadOnlyTransport.WIFI ->
                DebugWifiTransportGateway(
                    behavior = DebugWifiTransportGateway.Behavior.READ_TIMEOUT,
                )
        }

    private fun activeGatewayTransport(): DiagnosticsReadOnlyTransport {
        val overrideProfile = configuredProfile
        if (overrideProfile != null) {
            return when (overrideProfile.endpoint) {
                is TransportEndpoint.Bluetooth -> DiagnosticsReadOnlyTransport.BLUETOOTH
                is TransportEndpoint.Usb -> DiagnosticsReadOnlyTransport.USB
                is TransportEndpoint.Wifi -> DiagnosticsReadOnlyTransport.WIFI
            }
        }

        return selectedTransport
    }
}
