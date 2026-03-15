package com.ecuforge.feature.diagnostics.domain

sealed interface IdentificationUiState {
    data object Idle : IdentificationUiState
    data object Loading : IdentificationUiState

    data class Success(
        val identification: EcuIdentification
    ) : IdentificationUiState

    data class Error(
        val code: String,
        val message: String
    ) : IdentificationUiState
}
