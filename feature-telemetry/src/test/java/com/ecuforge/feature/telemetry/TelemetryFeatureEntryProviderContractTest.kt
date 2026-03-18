package com.ecuforge.feature.telemetry

import com.ecuforge.feature.telemetry.domain.TelemetryUiState
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TelemetryFeatureEntryProviderContractTest {
    @Test
    fun installedProviderOverridesDefaultEntrypointBehavior() {
        runBlocking {
            val provider =
                object : TelemetryFlowProvider {
                    override suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState {
                        return TelemetryUiState.Error(code = "PROVIDER", message = "custom telemetry")
                    }

                    override suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState {
                        return TelemetryUiState.Error(code = "PROVIDER", message = "custom telemetry timeout")
                    }
                }

            TelemetryFeatureEntry.installProvider(provider)
            try {
                val telemetry = TelemetryFeatureEntry.readTelemetryReadOnlyDemo()

                assertThat(telemetry)
                    .describedAs("Installed telemetry provider should override telemetry flow implementation")
                    .isInstanceOf(TelemetryUiState.Error::class.java)
                assertThat((telemetry as TelemetryUiState.Error).code)
                    .describedAs("Installed telemetry provider should expose custom telemetry error code")
                    .isEqualTo("PROVIDER")
            } finally {
                TelemetryFeatureEntry.resetProvider()
            }
        }
    }
}
