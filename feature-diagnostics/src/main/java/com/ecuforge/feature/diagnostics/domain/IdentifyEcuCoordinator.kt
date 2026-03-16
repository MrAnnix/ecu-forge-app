package com.ecuforge.feature.diagnostics.domain

import com.ecuforge.core.transport.TransportEndpoint

/**
 * Produces UI-state progression for the identification flow.
 */
class IdentifyEcuCoordinator(
    private val identifyEcuUseCase: IdentifyEcuUseCase,
) {
    /**
     * Runs the use case and returns ordered UI states (loading then terminal state).
     */
    suspend fun run(
        request: IdentificationRequest,
        endpoint: TransportEndpoint,
    ): List<IdentificationUiState> {
        val states = mutableListOf<IdentificationUiState>()
        states.add(IdentificationUiState.Loading)
        states.add(identifyEcuUseCase.execute(request, endpoint))
        return states
    }
}
