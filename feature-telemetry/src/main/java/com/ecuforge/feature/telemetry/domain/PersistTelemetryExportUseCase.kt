package com.ecuforge.feature.telemetry.domain

/**
 * Persists telemetry export artifacts using the telemetry-export.v1 formatter and retention policy.
 */
class PersistTelemetryExportUseCase(
    private val exportStore: TelemetryExportStore,
    private val formatter: TelemetryExportFormatter = TelemetryExportFormatter(),
    private val retentionPolicy: TelemetryRetentionPolicy = TelemetryRetentionPolicy(),
    private val nowEpochMillisProvider: () -> Long = { System.currentTimeMillis() },
) {
    /**
     * Exports telemetry [successState] to storage and applies retention cleanup.
     */
    fun execute(successState: TelemetryUiState.Success): PersistTelemetryExportResult {
        if (successState.samples.isEmpty()) {
            return PersistTelemetryExportResult.Failure(
                code = "EXPORT_INVALID_STATE",
                message = "Telemetry export requires at least one sample",
            )
        }

        val exportedAtEpochMillis = nowEpochMillisProvider()
        val envelope = formatter.toEnvelope(successState, exportedAtEpochMillis = exportedAtEpochMillis)
        val payloadJson = formatter.toJson(envelope)
        val exportId = buildExportId(exportedAtEpochMillis)

        when (val writeResult = exportStore.writeExport(exportId = exportId, payloadJson = payloadJson)) {
            is TelemetryExportStoreWriteResult.Failure -> {
                return PersistTelemetryExportResult.Failure(
                    code = "EXPORT_IO",
                    message = "Telemetry export write failed (${writeResult.code}): ${writeResult.message}",
                )
            }

            TelemetryExportStoreWriteResult.Success -> {
                // Continue with retention enforcement.
            }
        }

        val listResult = exportStore.listExports()
        if (listResult is TelemetryExportStoreListResult.Failure) {
            return PersistTelemetryExportResult.Success(
                receipt =
                    TelemetryExportReceipt(
                        exportId = exportId,
                        exportedAtEpochMillis = exportedAtEpochMillis,
                        deletedExportIds = emptyList(),
                        retentionApplied = false,
                        warning =
                            "Telemetry export saved, but retention list failed (${listResult.code}): ${listResult.message}",
                    ),
            )
        }

        val exports = (listResult as TelemetryExportStoreListResult.Success).exports
        val expiredExportIds = findExpiredExportIds(exports, nowEpochMillis = exportedAtEpochMillis)
        if (expiredExportIds.isEmpty()) {
            return PersistTelemetryExportResult.Success(
                receipt =
                    TelemetryExportReceipt(
                        exportId = exportId,
                        exportedAtEpochMillis = exportedAtEpochMillis,
                        deletedExportIds = emptyList(),
                        retentionApplied = true,
                        warning = null,
                    ),
            )
        }

        return when (val deleteResult = exportStore.deleteExports(expiredExportIds)) {
            is TelemetryExportStoreDeleteResult.Failure -> {
                PersistTelemetryExportResult.Success(
                    receipt =
                        TelemetryExportReceipt(
                            exportId = exportId,
                            exportedAtEpochMillis = exportedAtEpochMillis,
                            deletedExportIds = emptyList(),
                            retentionApplied = false,
                            warning =
                                "Telemetry export saved, but retention cleanup failed (${deleteResult.code}): ${deleteResult.message}",
                        ),
                )
            }

            TelemetryExportStoreDeleteResult.Success -> {
                PersistTelemetryExportResult.Success(
                    receipt =
                        TelemetryExportReceipt(
                            exportId = exportId,
                            exportedAtEpochMillis = exportedAtEpochMillis,
                            deletedExportIds = expiredExportIds,
                            retentionApplied = true,
                            warning = null,
                        ),
                )
            }
        }
    }

    private fun findExpiredExportIds(
        exports: List<TelemetryStoredExport>,
        nowEpochMillis: Long,
    ): List<String> {
        val ordered = exports.sortedByDescending { export -> export.exportedAtEpochMillis }
        return ordered
            .mapIndexedNotNull { index, export ->
                val shouldRetain =
                    retentionPolicy.shouldRetain(
                        exportedAtEpochMillis = export.exportedAtEpochMillis,
                        nowEpochMillis = nowEpochMillis,
                        newerExportCount = index,
                    )
                if (shouldRetain) {
                    null
                } else {
                    export.exportId
                }
            }
    }

    private fun buildExportId(exportedAtEpochMillis: Long): String {
        return "telemetry-$exportedAtEpochMillis.json"
    }
}

/**
 * Outcome for telemetry export persistence flow.
 */
sealed interface PersistTelemetryExportResult {
    /**
     * Successful export persistence.
     *
     * @property receipt Export persistence and retention details.
     */
    data class Success(
        val receipt: TelemetryExportReceipt,
    ) : PersistTelemetryExportResult

    /**
     * Failed export persistence.
     *
     * @property code Stable machine-readable error code.
     * @property message Human-readable diagnostics message.
     */
    data class Failure(
        val code: String,
        val message: String,
    ) : PersistTelemetryExportResult
}

/**
 * Receipt metadata produced after a telemetry export persistence attempt.
 *
 * @property exportId Stable persisted artifact identifier.
 * @property exportedAtEpochMillis UTC epoch millis used for the saved export.
 * @property deletedExportIds Exports removed during retention cleanup.
 * @property retentionApplied Whether retention steps completed successfully.
 * @property warning Optional non-fatal warning when retention could not complete.
 */
data class TelemetryExportReceipt(
    val exportId: String,
    val exportedAtEpochMillis: Long,
    val deletedExportIds: List<String>,
    val retentionApplied: Boolean,
    val warning: String?,
)
