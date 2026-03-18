package com.ecuforge.feature.telemetry.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PersistTelemetryExportUseCaseTest {
    @Test
    fun executePersistsExportAndDeletesExpiredArtifacts() {
        val store =
            FakeTelemetryExportStore(
                initialExports =
                    mutableListOf(
                        TelemetryStoredExport(exportId = "telemetry-1000.json", exportedAtEpochMillis = 1000),
                        TelemetryStoredExport(exportId = "telemetry-2000.json", exportedAtEpochMillis = 2000),
                    ),
            )
        val useCase =
            PersistTelemetryExportUseCase(
                exportStore = store,
                retentionPolicy = TelemetryRetentionPolicy(maxAgeDays = 1, maxExportCount = 1),
                nowEpochMillisProvider = { 3000 },
            )

        val result =
            useCase.execute(
                successState =
                    TelemetryUiState.Success(
                        samples = listOf(TelemetrySample(signal = "RPM", value = 1450.0, unit = "rpm")),
                        capturedFrameCount = 1,
                        bufferedFrames = listOf(emptyList()),
                    ),
            )

        assertThat(result)
            .describedAs("Valid telemetry snapshot should persist export successfully")
            .isInstanceOf(PersistTelemetryExportResult.Success::class.java)
        val success = result as PersistTelemetryExportResult.Success
        assertThat(success.receipt.exportId)
            .describedAs("Export id should be derived from current epoch millis for deterministic naming")
            .isEqualTo("telemetry-3000.json")
        assertThat(success.receipt.deletedExportIds)
            .describedAs("Retention should remove older exports beyond maxExportCount")
            .containsExactly("telemetry-2000.json", "telemetry-1000.json")
        assertThat(success.receipt.retentionApplied)
            .describedAs("Retention should be marked as applied when cleanup succeeds")
            .isTrue()
    }

    @Test
    fun executeFailsWhenTelemetryStateHasNoSamples() {
        val store = FakeTelemetryExportStore()
        val useCase = PersistTelemetryExportUseCase(exportStore = store, nowEpochMillisProvider = { 3000 })

        val result =
            useCase.execute(
                successState =
                    TelemetryUiState.Success(
                        samples = emptyList(),
                        capturedFrameCount = 1,
                        bufferedFrames = listOf(emptyList()),
                    ),
            )

        assertThat(result)
            .describedAs("Empty telemetry samples should be rejected before any storage operation")
            .isInstanceOf(PersistTelemetryExportResult.Failure::class.java)
        val failure = result as PersistTelemetryExportResult.Failure
        assertThat(failure.code)
            .describedAs("Empty telemetry export should return EXPORT_INVALID_STATE")
            .isEqualTo("EXPORT_INVALID_STATE")
    }

    @Test
    fun executeReturnsSuccessWithWarningWhenCleanupDeleteFails() {
        val store =
            FakeTelemetryExportStore(
                initialExports =
                    mutableListOf(
                        TelemetryStoredExport(exportId = "telemetry-1000.json", exportedAtEpochMillis = 1000),
                    ),
                deleteFailure = TelemetryExportStoreDeleteResult.Failure(code = "FILE_DELETE", message = "locked"),
            )
        val useCase =
            PersistTelemetryExportUseCase(
                exportStore = store,
                retentionPolicy = TelemetryRetentionPolicy(maxAgeDays = 1, maxExportCount = 1),
                nowEpochMillisProvider = { 3000 },
            )

        val result =
            useCase.execute(
                successState =
                    TelemetryUiState.Success(
                        samples = listOf(TelemetrySample(signal = "RPM", value = 1450.0, unit = "rpm")),
                        capturedFrameCount = 1,
                        bufferedFrames = listOf(emptyList()),
                    ),
            )

        assertThat(result)
            .describedAs("Export should still be successful when retention cleanup fails after write")
            .isInstanceOf(PersistTelemetryExportResult.Success::class.java)
        val success = result as PersistTelemetryExportResult.Success
        assertThat(success.receipt.retentionApplied)
            .describedAs("Failed retention cleanup should mark retention as not applied")
            .isFalse()
        assertThat(success.receipt.warning)
            .describedAs("Failed retention cleanup should expose a non-fatal warning")
            .contains("retention cleanup failed")
    }

    private class FakeTelemetryExportStore(
        initialExports: MutableList<TelemetryStoredExport> = mutableListOf(),
        private val writeFailure: TelemetryExportStoreWriteResult.Failure? = null,
        private val listFailure: TelemetryExportStoreListResult.Failure? = null,
        private val deleteFailure: TelemetryExportStoreDeleteResult.Failure? = null,
    ) : TelemetryExportStore {
        private val exports: MutableList<TelemetryStoredExport> = initialExports

        override fun writeExport(
            exportId: String,
            payloadJson: String,
        ): TelemetryExportStoreWriteResult {
            val failure = writeFailure
            if (failure != null) {
                return failure
            }
            val exportedAt = exportId.removePrefix("telemetry-").removeSuffix(".json").toLongOrNull() ?: -1
            exports += TelemetryStoredExport(exportId = exportId, exportedAtEpochMillis = exportedAt)
            return TelemetryExportStoreWriteResult.Success
        }

        override fun listExports(): TelemetryExportStoreListResult {
            val failure = listFailure
            if (failure != null) {
                return failure
            }
            return TelemetryExportStoreListResult.Success(exports.toList())
        }

        override fun deleteExports(exportIds: List<String>): TelemetryExportStoreDeleteResult {
            val failure = deleteFailure
            if (failure != null) {
                return failure
            }
            exports.removeAll { export -> export.exportId in exportIds }
            return TelemetryExportStoreDeleteResult.Success
        }
    }
}
