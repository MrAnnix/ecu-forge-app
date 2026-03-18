package com.ecuforge.feature.diagnostics.domain

/**
 * UI state model for the read-only identification flow.
 */
sealed interface IdentificationUiState {
    /**
     * Initial idle state before any action is triggered.
     */
    data object Idle : IdentificationUiState

    /**
     * State emitted while the use case is running.
     */
    data object Loading : IdentificationUiState

    /**
     * Terminal success state with parsed ECU identification data.
     *
     * @property identification Parsed ECU identification payload.
     */
    data class Success(
        val identification: EcuIdentification,
    ) : IdentificationUiState

    /**
     * Terminal failure state with stable code and message.
     *
     * @property code Stable machine-readable error code.
     * @property message Human-readable error message for UI.
     */
    data class Error(
        val code: String,
        val message: String,
    ) : IdentificationUiState
}
