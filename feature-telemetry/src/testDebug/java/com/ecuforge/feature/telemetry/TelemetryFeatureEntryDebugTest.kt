package com.ecuforge.feature.telemetry

import com.ecuforge.feature.telemetry.domain.TelemetryUiState
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TelemetryFeatureEntryDebugTest {
    @Test
    fun readTelemetryDemoReturnsSuccess() {
        runBlocking {
            val result = TelemetryFeatureEntry.readTelemetryReadOnlyDemo()

            assertThat(result)
                .describedAs("Debug telemetry demo should return Success state")
                .isInstanceOf(TelemetryUiState.Success::class.java)
            val success = result as TelemetryUiState.Success
            assertThat(success.samples)
                .describedAs("Debug telemetry demo should include scripted sample values")
                .isNotEmpty()
        }
    }

    @Test
    fun readTelemetryTimeoutDemoReturnsTimeoutError() {
        runBlocking {
            val result = TelemetryFeatureEntry.readTelemetryReadOnlyTimeoutDemo()

            assertThat(result)
                .describedAs("Debug telemetry timeout demo should return Error state")
                .isInstanceOf(TelemetryUiState.Error::class.java)
            val error = result as TelemetryUiState.Error
            assertThat(error.code)
                .describedAs("Debug telemetry timeout demo should expose TIMEOUT code")
                .isEqualTo("TIMEOUT")
        }
    }
}
