package com.ecuforge.feature.telemetry.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TelemetryExportFormatterTest {
    private val formatter = TelemetryExportFormatter()

    @Test
    fun toEnvelopeNormalizesAndSortsSamplesDeterministically() {
        val successState =
            TelemetryUiState.Success(
                samples =
                    listOf(
                        TelemetrySample(signal = " TPS ", value = 2.1, unit = " % "),
                        TelemetrySample(signal = "RPM", value = 1450.0, unit = "rpm"),
                    ),
                capturedFrameCount = 3,
                bufferedFrames = emptyList(),
            )

        val envelope = formatter.toEnvelope(successState, exportedAtEpochMillis = 1_710_000_000_000)

        assertThat(envelope.schemaVersion)
            .describedAs("Telemetry export envelope should use the v1 schema identifier")
            .isEqualTo("telemetry-export.v1")
        assertThat(envelope.samples.map { sample -> sample.signal })
            .describedAs("Telemetry export envelope should sort samples by signal for deterministic output")
            .containsExactly("RPM", "TPS")
        assertThat(envelope.samples[1].unit)
            .describedAs("Telemetry export envelope should trim unit values")
            .isEqualTo("%")
    }

    @Test
    fun toJsonProducesDeterministicPayloadAndEscapesStrings() {
        val envelope =
            TelemetryExportEnvelope(
                schemaVersion = "telemetry-export.v1",
                exportedAtEpochMillis = 1_710_000_000_000,
                capturedFrameCount = 2,
                sampleCount = 1,
                samples =
                    listOf(
                        TelemetrySample(signal = "TPS\"A", value = 2.1000, unit = "%"),
                    ),
            )

        val json = formatter.toJson(envelope)

        assertThat(json)
            .describedAs("Telemetry export JSON should contain the schema version field")
            .contains("\"schemaVersion\":\"telemetry-export.v1\"")
        assertThat(json)
            .describedAs("Telemetry export JSON should include captured frame metadata")
            .contains("\"capturedFrameCount\":2")
        assertThat(json)
            .describedAs("Telemetry export JSON should escape quotes in string fields")
            .contains("\"signal\":\"TPS\\\"A\"")
        assertThat(json)
            .describedAs("Telemetry export JSON should serialize doubles without trailing zeros")
            .contains("\"value\":2.1")
    }
}
