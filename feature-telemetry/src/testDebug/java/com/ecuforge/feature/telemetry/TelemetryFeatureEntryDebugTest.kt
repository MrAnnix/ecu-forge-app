package com.ecuforge.feature.telemetry

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.feature.telemetry.domain.TelemetryUiState
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test

class TelemetryFeatureEntryDebugTest {
    @After
    fun resetTransportSelection() {
        TelemetryFeatureEntry.configureReadOnlyTransport(TelemetryReadOnlyTransport.USB)
    }

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
            assertThat(success.capturedFrameCount)
                .describedAs("Debug telemetry demo should capture all buffered frames in nominal scenario")
                .isEqualTo(3)
            assertThat(success.bufferedFrames)
                .describedAs("Debug telemetry demo should expose buffered frame history for auditability")
                .hasSize(3)
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

    @Test
    fun configureTransportToBluetoothKeepsTelemetrySuccess() {
        runBlocking {
            TelemetryFeatureEntry.configureReadOnlyTransport(TelemetryReadOnlyTransport.BLUETOOTH)

            val result = TelemetryFeatureEntry.readTelemetryReadOnlyDemo()

            assertThat(result)
                .describedAs("Bluetooth transport selection should keep telemetry flow successful")
                .isInstanceOf(TelemetryUiState.Success::class.java)
            val success = result as TelemetryUiState.Success
            assertThat(success.samples)
                .describedAs("Bluetooth telemetry flow should return scripted samples")
                .isNotEmpty()
        }
    }

    @Test
    fun configureProfileOverrideAppliesCustomWifiEndpoint() {
        runBlocking {
            TelemetryFeatureEntry.configureReadOnlyProfile(
                TelemetryReadOnlyProfile(
                    ecuFamily = "KEIHIN",
                    endpointHint = "WIFI",
                    endpoint = TransportEndpoint.Wifi(host = "10.0.0.15", port = 35000),
                    bufferFrameCount = 3,
                    requiredStableFrameCount = 2,
                ),
            )

            val result = TelemetryFeatureEntry.readTelemetryReadOnlyDemo()

            assertThat(result)
                .describedAs("Explicit telemetry profile override should preserve successful snapshot retrieval")
                .isInstanceOf(TelemetryUiState.Success::class.java)
        }
    }
}
