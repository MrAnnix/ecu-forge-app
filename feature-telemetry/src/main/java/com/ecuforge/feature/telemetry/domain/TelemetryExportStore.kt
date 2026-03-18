package com.ecuforge.feature.telemetry.domain

/**
 * Storage contract for telemetry export artifacts.
 */
interface TelemetryExportStore {
    /**
     * Writes one export payload to storage.
     *
     * @param exportId Stable export artifact identifier.
     * @param payloadJson Deterministic JSON payload to persist.
     */
    fun writeExport(
        exportId: String,
        payloadJson: String,
    ): TelemetryExportStoreWriteResult

    /**
     * Lists currently stored exports with metadata required for retention decisions.
     */
    fun listExports(): TelemetryExportStoreListResult

    /**
     * Deletes stored exports by identifier.
     *
     * @param exportIds Export identifiers to remove.
     */
    fun deleteExports(exportIds: List<String>): TelemetryExportStoreDeleteResult
}

/**
 * Stored export metadata used by retention logic.
 *
 * @property exportId Stable stored export identifier.
 * @property exportedAtEpochMillis UTC epoch millis associated with the export artifact.
 */
data class TelemetryStoredExport(
    val exportId: String,
    val exportedAtEpochMillis: Long,
)

/**
 * Result of writing one telemetry export artifact.
 */
sealed interface TelemetryExportStoreWriteResult {
    /**
     * Successful write result.
     */
    data object Success : TelemetryExportStoreWriteResult

    /**
     * Failed write result.
     *
     * @property code Stable machine-readable error code.
     * @property message Human-readable diagnostics message.
     */
    data class Failure(
        val code: String,
        val message: String,
    ) : TelemetryExportStoreWriteResult
}

/**
 * Result of listing telemetry export artifacts.
 */
sealed interface TelemetryExportStoreListResult {
    /**
     * Successful list result.
     *
     * @property exports Deterministic snapshot of known stored exports.
     */
    data class Success(
        val exports: List<TelemetryStoredExport>,
    ) : TelemetryExportStoreListResult

    /**
     * Failed list result.
     *
     * @property code Stable machine-readable error code.
     * @property message Human-readable diagnostics message.
     */
    data class Failure(
        val code: String,
        val message: String,
    ) : TelemetryExportStoreListResult
}

/**
 * Result of deleting telemetry export artifacts.
 */
sealed interface TelemetryExportStoreDeleteResult {
    /**
     * Successful delete result.
     */
    data object Success : TelemetryExportStoreDeleteResult

    /**
     * Failed delete result.
     *
     * @property code Stable machine-readable error code.
     * @property message Human-readable diagnostics message.
     */
    data class Failure(
        val code: String,
        val message: String,
    ) : TelemetryExportStoreDeleteResult
}
