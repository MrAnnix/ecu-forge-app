package com.ecuforge.feature.diagnostics.domain

/**
 * UI state model for read-only DTC retrieval.
 */
sealed interface DtcUiState {
    /**
     * Initial idle state before any action is triggered.
     */
    data object Idle : DtcUiState

    /**
     * State emitted while DTC retrieval is running.
     */
    data object Loading : DtcUiState

    /**
     * Terminal success state with zero or more DTC records.
     *
     * @property dtcs Deterministic list of parsed DTC records.
     */
    data class Success(
        val dtcs: List<DtcRecord>,
    ) : DtcUiState

    /**
     * Terminal failure state with stable error code and message.
     *
     * @property code Stable machine-readable error code.
     * @property message Human-readable error message for UI.
     */
    data class Error(
        val code: String,
        val message: String,
    ) : DtcUiState
}
