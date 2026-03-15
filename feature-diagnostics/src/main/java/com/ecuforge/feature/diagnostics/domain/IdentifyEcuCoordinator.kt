package com.ecuforge.feature.diagnostics.domain

import com.ecuforge.core.transport.TransportEndpoint

class IdentifyEcuCoordinator(
    private val identifyEcuUseCase: IdentifyEcuUseCase
) {
    suspend fun run(
        request: IdentificationRequest,
        endpoint: TransportEndpoint
    ): List<IdentificationUiState> {
        val states = mutableListOf<IdentificationUiState>()
        states.add(IdentificationUiState.Loading)
        states.add(identifyEcuUseCase.execute(request, endpoint))
        return states
    }
}
