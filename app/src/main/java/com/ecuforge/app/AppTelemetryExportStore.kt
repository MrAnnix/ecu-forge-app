package com.ecuforge.app

import com.ecuforge.feature.telemetry.domain.TelemetryExportStore
import com.ecuforge.feature.telemetry.domain.TelemetryExportStoreDeleteResult
import com.ecuforge.feature.telemetry.domain.TelemetryExportStoreListResult
import com.ecuforge.feature.telemetry.domain.TelemetryExportStoreWriteResult
import com.ecuforge.feature.telemetry.domain.TelemetryStoredExport
import java.io.File

/**
 * App-private file storage adapter for telemetry exports.
 */
class AppTelemetryExportStore(
    private val rootDirectory: File,
) : TelemetryExportStore {
    override fun writeExport(
        exportId: String,
        payloadJson: String,
    ): TelemetryExportStoreWriteResult {
        return runCatching {
            ensureDirectory()
            val targetFile = File(rootDirectory, exportId)
            targetFile.writeText(payloadJson)
            TelemetryExportStoreWriteResult.Success
        }.getOrElse { throwable ->
            TelemetryExportStoreWriteResult.Failure(
                code = "FILE_WRITE",
                message = throwable.message ?: "Unknown file write error",
            )
        }
    }

    override fun listExports(): TelemetryExportStoreListResult {
        return runCatching {
            ensureDirectory()
            val exports =
                rootDirectory
                    .listFiles()
                    ?.filter { file -> file.isFile }
                    ?.mapNotNull { file ->
                        val exportId = file.name
                        val exportedAt = parseExportEpochMillis(exportId)
                        if (exportedAt == null) {
                            null
                        } else {
                            TelemetryStoredExport(
                                exportId = exportId,
                                exportedAtEpochMillis = exportedAt,
                            )
                        }
                    }
                    .orEmpty()
            TelemetryExportStoreListResult.Success(exports)
        }.getOrElse { throwable ->
            TelemetryExportStoreListResult.Failure(
                code = "FILE_LIST",
                message = throwable.message ?: "Unknown file list error",
            )
        }
    }

    override fun deleteExports(exportIds: List<String>): TelemetryExportStoreDeleteResult {
        return runCatching {
            ensureDirectory()
            val failedDeletes =
                exportIds.filter { exportId ->
                    val targetFile = File(rootDirectory, exportId)
                    targetFile.exists() && !targetFile.delete()
                }
            if (failedDeletes.isEmpty()) {
                TelemetryExportStoreDeleteResult.Success
            } else {
                TelemetryExportStoreDeleteResult.Failure(
                    code = "FILE_DELETE",
                    message = "Failed to delete exports: ${failedDeletes.joinToString(",")}",
                )
            }
        }.getOrElse { throwable ->
            TelemetryExportStoreDeleteResult.Failure(
                code = "FILE_DELETE",
                message = throwable.message ?: "Unknown file delete error",
            )
        }
    }

    private fun ensureDirectory() {
        if (!rootDirectory.exists()) {
            check(rootDirectory.mkdirs()) { "Failed to create telemetry export directory" }
        }
        check(rootDirectory.isDirectory) { "Telemetry export path is not a directory" }
    }

    private fun parseExportEpochMillis(exportId: String): Long? {
        val match = EXPORT_FILE_PATTERN.matchEntire(exportId) ?: return null
        return match.groupValues[1].toLongOrNull()
    }

    private companion object {
        private val EXPORT_FILE_PATTERN = Regex("^telemetry-(\\d+)\\.json$")
    }
}
