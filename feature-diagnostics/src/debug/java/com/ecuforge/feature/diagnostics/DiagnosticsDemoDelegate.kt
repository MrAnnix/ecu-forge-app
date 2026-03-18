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
internal object DiagnosticsDemoDelegate : DiagnosticsFlowProvider {
    private val defaultProfile =
        DiagnosticsReadOnlyProfile(
            ecuFamily = "KEIHIN",
            endpointHint = "USB",
            endpoint = TransportEndpoint.Usb(vendorId = 1027, productId = 48960),
        )

    private val transportBackedProvider =
        TransportBackedDiagnosticsFlowProvider(
            transportGatewayFactory = {
                DebugUsbTransportGateway(
                    behavior = DebugUsbTransportGateway.Behavior.NOMINAL,
                )
            },
            profile = defaultProfile,
            dtcCatalogRepository = IndexedDtcCatalogRepository(),
        )

    /**
     * Executes the happy-path identification demo.
     */
    override suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        return transportBackedProvider.identifyReadOnlyDemo()
    }

    /**
     * Executes identification demo that simulates a read timeout.
     */
    override suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        val useCase =
            IdentifyEcuUseCase(
                transportGateway =
                    DebugUsbTransportGateway(
                        behavior = DebugUsbTransportGateway.Behavior.READ_TIMEOUT,
                    ),
            )

        return useCase.execute(
            request =
                IdentificationRequest(
                    ecuFamily = defaultProfile.ecuFamily,
                    endpointHint = defaultProfile.endpointHint,
                ),
            endpoint = defaultProfile.endpoint,
        )
    }

    /**
     * Executes the happy-path DTC read demo.
     */
    override suspend fun readDtcReadOnlyDemo(): DtcUiState {
        return transportBackedProvider.readDtcReadOnlyDemo()
    }

    /**
     * Executes the happy-path DTC read demo with optional catalog context.
     */
    override suspend fun readDtcReadOnlyDemo(
        vehicleCatalogContext: VehicleCatalogContext?,
        preferCatalogDescriptions: Boolean,
    ): DtcUiState {
        return transportBackedProvider.readDtcReadOnlyDemo(
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
                transportGateway =
                    DebugUsbTransportGateway(
                        behavior = DebugUsbTransportGateway.Behavior.READ_TIMEOUT,
                    ),
                dtcCatalogRepository = IndexedDtcCatalogRepository(),
            )

        return useCase.execute(
            request =
                ReadDtcRequest(
                    ecuFamily = defaultProfile.ecuFamily,
                    endpointHint = defaultProfile.endpointHint,
                ),
            endpoint = defaultProfile.endpoint,
        )
    }
}
