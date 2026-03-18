package com.ecuforge.feature.diagnostics

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.feature.diagnostics.domain.DtcCatalogRepository
import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationRequest
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.diagnostics.domain.IdentifyEcuUseCase
import com.ecuforge.feature.diagnostics.domain.ReadDtcRequest
import com.ecuforge.feature.diagnostics.domain.ReadDtcUseCase
import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext
import com.ecuforge.transport.TransportGateway

/**
 * Transport-backed implementation for read-only diagnostics flows.
 *
 * This provider is intended for real transport integration, but remains opt-in
 * through [DiagnosticsFeatureEntry.installProvider] so default demo behavior stays unchanged.
 *
 * @property transportGatewayFactory Factory that provides a fresh gateway instance per flow execution.
 * @property profile Read-only request profile used for identification and DTC flows.
 * @property dtcCatalogRepository Optional catalog repository for vehicle-aware DTC enrichment.
 */
class TransportBackedDiagnosticsFlowProvider(
    private val transportGatewayFactory: () -> TransportGateway,
    private val profile: DiagnosticsReadOnlyProfile,
    private val dtcCatalogRepository: DtcCatalogRepository? = null,
) : DiagnosticsFlowProvider {
    /**
     * Executes read-only identification using transport-backed use case wiring.
     */
    override suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        val useCase = IdentifyEcuUseCase(transportGateway = transportGatewayFactory())
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
     * Returns deterministic error because timeout simulation is demo-only.
     */
    override suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        return IdentificationUiState.Error(
            code = SCENARIO_UNAVAILABLE,
            message = "Timeout demo scenario is unavailable for transport-backed provider",
        )
    }

    /**
     * Executes read-only DTC retrieval using transport-backed use case wiring.
     */
    override suspend fun readDtcReadOnlyDemo(): DtcUiState {
        return executeReadDtc(
            vehicleCatalogContext = null,
            preferCatalogDescriptions = false,
        )
    }

    /**
     * Executes read-only DTC retrieval with optional vehicle-aware catalog enrichment.
     */
    override suspend fun readDtcReadOnlyDemo(
        vehicleCatalogContext: VehicleCatalogContext?,
        preferCatalogDescriptions: Boolean,
    ): DtcUiState {
        return executeReadDtc(
            vehicleCatalogContext = vehicleCatalogContext,
            preferCatalogDescriptions = preferCatalogDescriptions,
        )
    }

    /**
     * Returns deterministic error because timeout simulation is demo-only.
     */
    override suspend fun readDtcReadOnlyTimeoutDemo(): DtcUiState {
        return DtcUiState.Error(
            code = SCENARIO_UNAVAILABLE,
            message = "Timeout demo scenario is unavailable for transport-backed provider",
        )
    }

    /**
     * Executes DTC retrieval against transport-backed use case.
     */
    private suspend fun executeReadDtc(
        vehicleCatalogContext: VehicleCatalogContext?,
        preferCatalogDescriptions: Boolean,
    ): DtcUiState {
        val useCase =
            ReadDtcUseCase(
                transportGateway = transportGatewayFactory(),
                dtcCatalogRepository = dtcCatalogRepository,
            )

        return useCase.execute(
            request =
                ReadDtcRequest(
                    ecuFamily = profile.ecuFamily,
                    endpointHint = profile.endpointHint,
                    vehicleCatalogContext = vehicleCatalogContext,
                    preferCatalogDescriptions = preferCatalogDescriptions,
                ),
            endpoint = profile.endpoint,
        )
    }

    private companion object {
        const val SCENARIO_UNAVAILABLE: String = "SCENARIO_UNAVAILABLE"
    }
}

/**
 * Immutable read-only diagnostics profile for transport-backed flows.
 *
 * @property ecuFamily ECU family identifier used by compatibility gates.
 * @property endpointHint Transport hint used by request payloads and compatibility parity checks.
 * @property endpoint Concrete transport endpoint used by transport gateway.
 */
data class DiagnosticsReadOnlyProfile(
    val ecuFamily: String,
    val endpointHint: String,
    val endpoint: TransportEndpoint,
)
