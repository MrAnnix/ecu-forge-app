package com.ecuforge.feature.telemetry

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.feature.telemetry.domain.TelemetryUiState
import com.ecuforge.transport.fake.FakeTransportGateway
import com.ecuforge.transport.fake.FakeTransportOperation
import com.ecuforge.transport.fake.FakeTransportScenario
import com.ecuforge.transport.fake.FakeTransportStep
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TransportBackedTelemetryFlowProviderTest {
    @Test
    fun readTelemetryDemoDelegatesToTransportUseCase() {
        runBlocking {
            val provider =
                TransportBackedTelemetryFlowProvider(
                    transportGatewayFactory = {
                        FakeTransportGateway(
                            scenario =
                                FakeTransportScenario.of(
                                    FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                                    FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
                                    FakeTransportStep(
                                        operation = FakeTransportOperation.READ,
                                        success = true,
                                        readPayload = "RPM=1440|TPS=2.0|ECT=81.8|VBAT=13.8".encodeToByteArray(),
                                    ),
                                    FakeTransportStep(
                                        operation = FakeTransportOperation.READ,
                                        success = true,
                                        readPayload = "RPM=1450|TPS=2.1|ECT=82.0|VBAT=13.9".encodeToByteArray(),
                                    ),
                                    FakeTransportStep(
                                        operation = FakeTransportOperation.READ,
                                        success = true,
                                        readPayload = "RPM=1460|TPS=2.2|ECT=82.1|VBAT=13.9".encodeToByteArray(),
                                    ),
                                    FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                                ),
                        )
                    },
                    profile =
                        TelemetryReadOnlyProfile(
                            ecuFamily = "KEIHIN",
                            endpointHint = "USB",
                            endpoint = TransportEndpoint.Usb(vendorId = 1027, productId = 48960),
                            bufferFrameCount = 3,
                            requiredStableFrameCount = 2,
                        ),
                )

            val result = provider.readTelemetryReadOnlyDemo()

            assertThat(result)
                .describedAs("Transport-backed telemetry provider should return Success state for valid buffered payloads")
                .isInstanceOf(TelemetryUiState.Success::class.java)
            val success = result as TelemetryUiState.Success
            assertThat(success.capturedFrameCount)
                .describedAs("Transport-backed telemetry provider should preserve buffered frame count")
                .isEqualTo(3)
        }
    }

    @Test
    fun readTelemetryTimeoutDemoReturnsScenarioUnavailableError() {
        runBlocking {
            val provider =
                TransportBackedTelemetryFlowProvider(
                    transportGatewayFactory = {
                        FakeTransportGateway(scenario = FakeTransportScenario.of())
                    },
                    profile =
                        TelemetryReadOnlyProfile(
                            ecuFamily = "KEIHIN",
                            endpointHint = "USB",
                            endpoint = TransportEndpoint.Usb(vendorId = 1027, productId = 48960),
                        ),
                )

            val result = provider.readTelemetryReadOnlyTimeoutDemo()

            assertThat(result)
                .describedAs("Transport-backed telemetry provider should not simulate timeout demo scenarios")
                .isInstanceOf(TelemetryUiState.Error::class.java)
            val error = result as TelemetryUiState.Error
            assertThat(error.code)
                .describedAs("Timeout demo calls should return SCENARIO_UNAVAILABLE code")
                .isEqualTo("SCENARIO_UNAVAILABLE")
        }
    }
}
