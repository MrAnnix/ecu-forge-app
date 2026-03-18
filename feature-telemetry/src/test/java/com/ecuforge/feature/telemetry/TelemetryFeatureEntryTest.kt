package com.ecuforge.feature.telemetry

import com.ecuforge.feature.telemetry.domain.TelemetryUiState
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TelemetryFeatureEntryTest {
    @Test
    fun readTelemetryDemoReturnsSupportedStateForActiveVariant() {
        runBlocking {
            val result = TelemetryFeatureEntry.readTelemetryReadOnlyDemo()

            val isExpectedDebugState = result is TelemetryUiState.Success
            val isExpectedReleaseState = result is TelemetryUiState.Error && result.code == "DEMO_DISABLED"
            assertThat(isExpectedDebugState || isExpectedReleaseState)
                .describedAs("Telemetry demo should return Success in debug or DEMO_DISABLED in release")
                .isTrue()
        }
    }

    @Test
    fun readTelemetryTimeoutDemoReturnsSupportedStateForActiveVariant() {
        runBlocking {
            val result = TelemetryFeatureEntry.readTelemetryReadOnlyTimeoutDemo()

            val isExpectedDebugState = result is TelemetryUiState.Error && result.code == "TIMEOUT"
            val isExpectedReleaseState = result is TelemetryUiState.Error && result.code == "DEMO_DISABLED"
            assertThat(isExpectedDebugState || isExpectedReleaseState)
                .describedAs("Telemetry timeout demo should return TIMEOUT in debug or DEMO_DISABLED in release")
                .isTrue()
        }
    }
}
