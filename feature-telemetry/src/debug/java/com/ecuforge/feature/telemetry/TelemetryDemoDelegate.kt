package com.ecuforge.feature.telemetry

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.feature.telemetry.domain.ReadTelemetryRequest
import com.ecuforge.feature.telemetry.domain.ReadTelemetryUseCase
import com.ecuforge.feature.telemetry.domain.TelemetryUiState

/**
 * Debug-only demo delegate that wires telemetry flows to fake transport scenarios.
 */
internal object TelemetryDemoDelegate : TelemetryFlowProvider {
    private val defaultProfile =
        TelemetryReadOnlyProfile(
            ecuFamily = "KEIHIN",
            endpointHint = "USB",
            endpoint = TransportEndpoint.Usb(vendorId = 1027, productId = 48960),
            bufferFrameCount = 3,
            requiredStableFrameCount = 2,
        )

    private val transportBackedProvider =
        TransportBackedTelemetryFlowProvider(
            transportGatewayFactory = {
                DebugUsbTelemetryTransportGateway(
                    behavior = DebugUsbTelemetryTransportGateway.Behavior.NOMINAL,
                )
            },
            profile = defaultProfile,
        )

    /**
     * Executes the happy-path telemetry snapshot demo.
     */
    override suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState {
        return transportBackedProvider.readTelemetryReadOnlyDemo()
    }

    /**
     * Executes telemetry snapshot demo that simulates a read timeout.
     */
    override suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState {
        val useCase =
            ReadTelemetryUseCase(
                transportGateway =
                    DebugUsbTelemetryTransportGateway(
                        behavior = DebugUsbTelemetryTransportGateway.Behavior.READ_TIMEOUT,
                    ),
            )

        return useCase.execute(
            request =
                ReadTelemetryRequest(
                    ecuFamily = defaultProfile.ecuFamily,
                    endpointHint = defaultProfile.endpointHint,
                    bufferFrameCount = defaultProfile.bufferFrameCount,
                    requiredStableFrameCount = defaultProfile.requiredStableFrameCount,
                ),
            endpoint = defaultProfile.endpoint,
        )
    }
}
