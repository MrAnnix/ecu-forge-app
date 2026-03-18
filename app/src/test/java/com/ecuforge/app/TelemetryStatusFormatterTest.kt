package com.ecuforge.app

import com.ecuforge.feature.telemetry.domain.TelemetrySample
import com.ecuforge.feature.telemetry.domain.TelemetryUiState
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TelemetryStatusFormatterTest {
    @Test
    fun formatterReturnsLoadingMessage() {
        val text = TelemetryStatusFormatter.format(TelemetryUiState.Loading)

        assertThat(text)
            .describedAs("Telemetry loading state should render a progress message")
            .isEqualTo("Reading telemetry snapshot...")
    }

    @Test
    fun formatterReturnsEmptySnapshotMessage() {
        val text = TelemetryStatusFormatter.format(TelemetryUiState.Success(samples = emptyList()))

        assertThat(text)
            .describedAs("Empty telemetry snapshot should explain that no samples were returned")
            .isEqualTo("Telemetry snapshot returned no samples.")
    }

    @Test
    fun formatterReturnsSampleSummary() {
        val state =
            TelemetryUiState.Success(
                samples =
                    listOf(
                        TelemetrySample(signal = "RPM", value = 1450.0, unit = "rpm"),
                        TelemetrySample(signal = "TPS", value = 2.1, unit = "%"),
                    ),
            )

        val text = TelemetryStatusFormatter.format(state)

        assertThat(text)
            .describedAs("Telemetry success text should include RPM sample")
            .contains("RPM=1450.0rpm")
        assertThat(text)
            .describedAs("Telemetry success text should include TPS sample")
            .contains("TPS=2.1%")
    }

    @Test
    fun formatterReturnsErrorMessage() {
        val text =
            TelemetryStatusFormatter.format(
                TelemetryUiState.Error(code = "TIMEOUT", message = "Read timeout"),
            )

        assertThat(text)
            .describedAs("Telemetry error state should include error code and message")
            .isEqualTo("Telemetry retrieval failed (TIMEOUT): Read timeout")
    }
}
