package com.ecuforge.app

/**
 * Validates non-destructive prechecks before executing read-only diagnostics actions.
 */
internal object ReadOnlyFlowPreflightValidator {
    /**
     * Validates precheck requirements for [request].
     */
    fun validate(request: Request): Result {
        return when (request.action) {
            Action.IDENTIFICATION -> Result.Success
            Action.DTC -> validateDtc(request)
            Action.TELEMETRY -> validateTelemetry(request)
        }
    }

    private fun validateDtc(request: Request): Result {
        if (!request.hasSuccessfulIdentification) {
            return Result.Failure(code = PRECHECK_IDENTIFICATION_REQUIRED)
        }

        if (request.catalogOptIn && (request.vehicleMake.isBlank() || request.vehicleModel.isBlank())) {
            return Result.Failure(code = PRECHECK_VEHICLE_SELECTION_REQUIRED)
        }

        return Result.Success
    }

    private fun validateTelemetry(request: Request): Result {
        return if (!request.hasSuccessfulIdentification) {
            Result.Failure(code = PRECHECK_IDENTIFICATION_REQUIRED)
        } else {
            Result.Success
        }
    }

    /**
     * Request data required to evaluate prechecks.
     *
     * @property action Requested read-only action.
     * @property hasSuccessfulIdentification Whether identification succeeded in the current flow.
     * @property catalogOptIn Whether user opted in to catalog-based DTC descriptions.
     * @property vehicleMake Current vehicle make input.
     * @property vehicleModel Current vehicle model input.
     */
    data class Request(
        val action: Action,
        val hasSuccessfulIdentification: Boolean,
        val catalogOptIn: Boolean,
        val vehicleMake: String,
        val vehicleModel: String,
    )

    /**
     * Supported read-only actions.
     */
    enum class Action {
        IDENTIFICATION,
        DTC,
        TELEMETRY,
    }

    /**
     * Precheck validation result.
     */
    sealed interface Result {
        /**
         * Validation succeeded.
         */
        data object Success : Result

        /**
         * Validation failed with deterministic error code.
         *
         * @property code Stable precheck error code.
         */
        data class Failure(val code: String) : Result
    }

    const val PRECHECK_IDENTIFICATION_REQUIRED: String = "PRECHECK_IDENTIFICATION_REQUIRED"
    const val PRECHECK_VEHICLE_SELECTION_REQUIRED: String = "PRECHECK_VEHICLE_SELECTION_REQUIRED"
}
