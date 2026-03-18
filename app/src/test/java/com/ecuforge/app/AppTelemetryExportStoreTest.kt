package com.ecuforge.app

import com.ecuforge.feature.telemetry.domain.TelemetryExportStoreDeleteResult
import com.ecuforge.feature.telemetry.domain.TelemetryExportStoreListResult
import com.ecuforge.feature.telemetry.domain.TelemetryExportStoreWriteResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Files

class AppTelemetryExportStoreTest {
    @Test
    fun writeListAndDeleteRoundTripUsesDeterministicIds() {
        val root = Files.createTempDirectory("telemetry-store-test").toFile()
        val store = AppTelemetryExportStore(root)

        val writeResult =
            store.writeExport(
                exportId = "telemetry-1710000000000.json",
                payloadJson = "{\"schemaVersion\":\"telemetry-export.v1\"}",
            )
        val listResult = store.listExports()
        val deleteResult = store.deleteExports(listOf("telemetry-1710000000000.json"))
        val listAfterDelete = store.listExports()

        assertThat(writeResult)
            .describedAs("Telemetry export file should be persisted in app-private storage")
            .isEqualTo(TelemetryExportStoreWriteResult.Success)
        assertThat(listResult)
            .describedAs("Listing exports should include files with deterministic telemetry file names")
            .isInstanceOf(TelemetryExportStoreListResult.Success::class.java)
        val listed = (listResult as TelemetryExportStoreListResult.Success).exports
        assertThat(listed)
            .describedAs("Telemetry list should contain the newly written export id")
            .anySatisfy { export ->
                assertThat(export.exportId)
                    .describedAs("Stored export id should remain stable")
                    .isEqualTo("telemetry-1710000000000.json")
                assertThat(export.exportedAtEpochMillis)
                    .describedAs("Stored export timestamp should be parsed from file naming convention")
                    .isEqualTo(1710000000000L)
            }
        assertThat(deleteResult)
            .describedAs("Delete should remove persisted telemetry export artifacts")
            .isEqualTo(TelemetryExportStoreDeleteResult.Success)
        assertThat(listAfterDelete)
            .describedAs("Listing after delete should no longer include removed exports")
            .isInstanceOf(TelemetryExportStoreListResult.Success::class.java)
        assertThat((listAfterDelete as TelemetryExportStoreListResult.Success).exports)
            .describedAs("Telemetry export store should be empty after deleting the only artifact")
            .isEmpty()
    }
}
