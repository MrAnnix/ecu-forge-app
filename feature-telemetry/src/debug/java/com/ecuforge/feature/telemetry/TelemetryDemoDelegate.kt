package com.ecuforge.feature.telemetry

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.feature.telemetry.domain.ReadTelemetryRequest
import com.ecuforge.feature.telemetry.domain.ReadTelemetryUseCase
import com.ecuforge.feature.telemetry.domain.TelemetryUiState

/**
 * Debug-only demo delegate that wires telemetry flows to fake transport scenarios.
 */
internal object TelemetryDemoDelegate :
    TelemetryFlowProvider,
    TelemetryTransportConfigurableProvider,
    TelemetryProfileConfigurableProvider,
    TelemetryConnectionSettingsConfigurableProvider {
    @Volatile
    private var selectedTransport: TelemetryReadOnlyTransport = TelemetryReadOnlyTransport.USB

    @Volatile
    private var configuredProfile: TelemetryReadOnlyProfile? = null

    /**
     * Updates active debug telemetry transport profile from app-selected transport.
     */
    override fun configureReadOnlyTransport(transport: TelemetryReadOnlyTransport) {
        selectedTransport = transport
        configuredProfile = null
    }

    /**
     * Updates active debug profile from app-provided transport settings.
     */
    override fun configureReadOnlyProfile(profile: TelemetryReadOnlyProfile) {
        configuredProfile = profile
    }

    /**
     * Updates profile override using primitive transport connection settings.
     */
    override fun configureReadOnlyConnectionSettings(settings: TelemetryReadOnlyConnectionSettings) {
        val profile =
            when (settings.transport) {
                TelemetryReadOnlyTransport.BLUETOOTH ->
                    TelemetryReadOnlyProfile(
                        ecuFamily = "KEIHIN",
                        endpointHint = "BLUETOOTH",
                        endpoint =
                            TransportEndpoint.Bluetooth(
                                macAddress = settings.bluetoothMacAddress ?: "00:11:22:33:44:55",
                            ),
                        bufferFrameCount = 3,
                        requiredStableFrameCount = 2,
                    )

                TelemetryReadOnlyTransport.USB ->
                    TelemetryReadOnlyProfile(
                        ecuFamily = "KEIHIN",
                        endpointHint = "USB",
                        endpoint =
                            TransportEndpoint.Usb(
                                vendorId = settings.usbVendorId ?: 1027,
                                productId = settings.usbProductId ?: 48960,
                            ),
                        bufferFrameCount = 3,
                        requiredStableFrameCount = 2,
                    )

                TelemetryReadOnlyTransport.WIFI ->
                    TelemetryReadOnlyProfile(
                        ecuFamily = "KEIHIN",
                        endpointHint = "WIFI",
                        endpoint =
                            TransportEndpoint.Wifi(
                                host = settings.wifiHost ?: "192.168.0.10",
                                port = settings.wifiPort ?: 35000,
                            ),
                        bufferFrameCount = 3,
                        requiredStableFrameCount = 2,
                    )
            }

        configureReadOnlyProfile(profile)
    }

    /**
     * Executes the happy-path telemetry snapshot demo.
     */
    override suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState {
        return providerForSelectedTransport().readTelemetryReadOnlyDemo()
    }

    /**
     * Executes telemetry snapshot demo that simulates a read timeout.
     */
    override suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState {
        val useCase =
            ReadTelemetryUseCase(
                transportGateway = timeoutGatewayForSelectedTransport(),
            )

        val profile = profileForSelectedTransport()

        return useCase.execute(
            request =
                ReadTelemetryRequest(
                    ecuFamily = profile.ecuFamily,
                    endpointHint = profile.endpointHint,
                    bufferFrameCount = profile.bufferFrameCount,
                    requiredStableFrameCount = profile.requiredStableFrameCount,
                ),
            endpoint = profile.endpoint,
        )
    }

    private fun providerForSelectedTransport(): TransportBackedTelemetryFlowProvider {
        return TransportBackedTelemetryFlowProvider(
            transportGatewayFactory = {
                nominalGatewayForSelectedTransport()
            },
            profile = profileForSelectedTransport(),
        )
    }

    private fun profileForSelectedTransport(): TelemetryReadOnlyProfile {
        configuredProfile?.let { profile ->
            return profile
        }

        return when (selectedTransport) {
            TelemetryReadOnlyTransport.BLUETOOTH ->
                TelemetryReadOnlyProfile(
                    ecuFamily = "KEIHIN",
                    endpointHint = "BLUETOOTH",
                    endpoint = TransportEndpoint.Bluetooth(macAddress = "00:11:22:33:44:55"),
                    bufferFrameCount = 3,
                    requiredStableFrameCount = 2,
                )

            TelemetryReadOnlyTransport.USB ->
                TelemetryReadOnlyProfile(
                    ecuFamily = "KEIHIN",
                    endpointHint = "USB",
                    endpoint = TransportEndpoint.Usb(vendorId = 1027, productId = 48960),
                    bufferFrameCount = 3,
                    requiredStableFrameCount = 2,
                )

            TelemetryReadOnlyTransport.WIFI ->
                TelemetryReadOnlyProfile(
                    ecuFamily = "KEIHIN",
                    endpointHint = "WIFI",
                    endpoint = TransportEndpoint.Wifi(host = "192.168.0.10", port = 35000),
                    bufferFrameCount = 3,
                    requiredStableFrameCount = 2,
                )
        }
    }

    private fun nominalGatewayForSelectedTransport() =
        when (activeGatewayTransport()) {
            TelemetryReadOnlyTransport.BLUETOOTH ->
                DebugBluetoothTelemetryTransportGateway(
                    behavior = DebugBluetoothTelemetryTransportGateway.Behavior.NOMINAL,
                )

            TelemetryReadOnlyTransport.USB ->
                DebugUsbTelemetryTransportGateway(
                    behavior = DebugUsbTelemetryTransportGateway.Behavior.NOMINAL,
                )

            TelemetryReadOnlyTransport.WIFI ->
                DebugWifiTelemetryTransportGateway(
                    behavior = DebugWifiTelemetryTransportGateway.Behavior.NOMINAL,
                )
        }

    private fun timeoutGatewayForSelectedTransport() =
        when (activeGatewayTransport()) {
            TelemetryReadOnlyTransport.BLUETOOTH ->
                DebugBluetoothTelemetryTransportGateway(
                    behavior = DebugBluetoothTelemetryTransportGateway.Behavior.READ_TIMEOUT,
                )

            TelemetryReadOnlyTransport.USB ->
                DebugUsbTelemetryTransportGateway(
                    behavior = DebugUsbTelemetryTransportGateway.Behavior.READ_TIMEOUT,
                )

            TelemetryReadOnlyTransport.WIFI ->
                DebugWifiTelemetryTransportGateway(
                    behavior = DebugWifiTelemetryTransportGateway.Behavior.READ_TIMEOUT,
                )
        }

    private fun activeGatewayTransport(): TelemetryReadOnlyTransport {
        val overrideProfile = configuredProfile
        if (overrideProfile != null) {
            return when (overrideProfile.endpoint) {
                is TransportEndpoint.Bluetooth -> TelemetryReadOnlyTransport.BLUETOOTH
                is TransportEndpoint.Usb -> TelemetryReadOnlyTransport.USB
                is TransportEndpoint.Wifi -> TelemetryReadOnlyTransport.WIFI
            }
        }

        return selectedTransport
    }
}
