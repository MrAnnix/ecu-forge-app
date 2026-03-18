package com.ecuforge.feature.telemetry

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.feature.telemetry.domain.ReadTelemetryRequest
import com.ecuforge.feature.telemetry.domain.ReadTelemetryUseCase
import com.ecuforge.feature.telemetry.domain.TelemetryUiState
import com.ecuforge.transport.TransportGateway

/**
 * Transport-backed implementation for read-only telemetry flows.
 *
 * This provider is opt-in through [TelemetryFeatureEntry.installProvider] so variant defaults remain safe.
 *
 * @property transportGatewayFactory Factory that provides a fresh gateway instance per flow execution.
 * @property profile Read-only telemetry profile used for request and endpoint values.
 */
class TransportBackedTelemetryFlowProvider(
    private val transportGatewayFactory: () -> TransportGateway,
    private val profile: TelemetryReadOnlyProfile,
) : TelemetryFlowProvider {
    /**
     * Executes read-only telemetry retrieval using transport-backed use case wiring.
     */
    override suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState {
        val useCase = ReadTelemetryUseCase(transportGateway = transportGatewayFactory())
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

    /**
     * Returns deterministic error because timeout simulation remains demo-only.
     */
    override suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState {
        return TelemetryUiState.Error(
            code = SCENARIO_UNAVAILABLE,
            message = "Timeout demo scenario is unavailable for transport-backed telemetry provider",
        )
    }

    private companion object {
        const val SCENARIO_UNAVAILABLE: String = "SCENARIO_UNAVAILABLE"
    }
}

/**
 * Immutable read-only telemetry profile for transport-backed flows.
 *
 * @property ecuFamily ECU family identifier used by telemetry compatibility checks.
 * @property endpointHint Transport hint used by request payloads.
 * @property endpoint Concrete transport endpoint used by transport gateway.
 * @property bufferFrameCount Buffered frame count used by telemetry sampling.
 * @property requiredStableFrameCount Required stable frame count for signal-set validation.
 */
data class TelemetryReadOnlyProfile(
    val ecuFamily: String,
    val endpointHint: String,
    val endpoint: TransportEndpoint,
    val bufferFrameCount: Int = 3,
    val requiredStableFrameCount: Int = 2,
)
