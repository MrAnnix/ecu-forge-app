package com.ecuforge.feature.telemetry

import com.ecuforge.feature.telemetry.domain.TelemetryUiState
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TelemetryFeatureEntryReleaseTest {
    @Test
    fun readTelemetryDemoIsDisabledInRelease() {
        runBlocking {
            val result = TelemetryFeatureEntry.readTelemetryReadOnlyDemo()

            assertThat(result)
                .describedAs("Release telemetry demo should be blocked and return Error state")
                .isInstanceOf(TelemetryUiState.Error::class.java)
            val error = result as TelemetryUiState.Error
            assertThat(error.code)
                .describedAs("Release telemetry demo should expose DEMO_DISABLED code")
                .isEqualTo("DEMO_DISABLED")
        }
    }

    @Test
    fun readTelemetryTimeoutDemoIsDisabledInRelease() {
        runBlocking {
            val result = TelemetryFeatureEntry.readTelemetryReadOnlyTimeoutDemo()

            assertThat(result)
                .describedAs("Release telemetry timeout demo should be blocked and return Error state")
                .isInstanceOf(TelemetryUiState.Error::class.java)
            val error = result as TelemetryUiState.Error
            assertThat(error.code)
                .describedAs("Release telemetry timeout demo should expose DEMO_DISABLED code")
                .isEqualTo("DEMO_DISABLED")
        }
    }
}
