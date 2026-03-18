package com.ecuforge.feature.telemetry.domain

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.transport.fake.FakeTransportGateway
import com.ecuforge.transport.fake.FakeTransportOperation
import com.ecuforge.transport.fake.FakeTransportScenario
import com.ecuforge.transport.fake.FakeTransportStep
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReadTelemetryUseCaseTest {
    @Test
    fun unsupportedFamilyReturnsErrorBeforeTransport() {
        runBlocking {
            val gateway = FakeTransportGateway(scenario = FakeTransportScenario.of())
            val useCase = ReadTelemetryUseCase(transportGateway = gateway)

            val result =
                useCase.execute(
                    request = ReadTelemetryRequest(ecuFamily = "UNKNOWN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Unsupported telemetry family should return Error state before transport operations")
                .isInstanceOf(TelemetryUiState.Error::class.java)
            val error = result as TelemetryUiState.Error
            assertThat(error.code)
                .describedAs("Unsupported telemetry family should map to ECU_UNSUPPORTED error code")
                .isEqualTo("ECU_UNSUPPORTED")
        }
    }

    @Test
    fun readTimeoutIsMappedToUiError() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
                            FakeTransportStep(
                                operation = FakeTransportOperation.READ,
                                success = false,
                                errorCode = TransportFailureCode.TIMEOUT,
                                errorMessage = "Read timeout",
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )
            val useCase = ReadTelemetryUseCase(transportGateway = gateway)

            val result =
                useCase.execute(
                    request = ReadTelemetryRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Telemetry read timeout should return Error state")
                .isInstanceOf(TelemetryUiState.Error::class.java)
            val error = result as TelemetryUiState.Error
            assertThat(error.code)
                .describedAs("Telemetry timeout should preserve TIMEOUT error code")
                .isEqualTo("TIMEOUT")
        }
    }

    @Test
    fun invalidPayloadReturnsParseError() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
                            FakeTransportStep(
                                operation = FakeTransportOperation.READ,
                                success = true,
                                readPayload = "INVALID_SAMPLE".encodeToByteArray(),
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )
            val useCase = ReadTelemetryUseCase(transportGateway = gateway)

            val result =
                useCase.execute(
                    request = ReadTelemetryRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Malformed telemetry payload should return Error state")
                .isInstanceOf(TelemetryUiState.Error::class.java)
            val error = result as TelemetryUiState.Error
            assertThat(error.code)
                .describedAs("Malformed telemetry payload should map to TELEMETRY_PARSE error code")
                .isEqualTo("TELEMETRY_PARSE")
        }
    }

    @Test
    fun nominalPayloadReturnsSuccessWithParsedSamples() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
                            FakeTransportStep(
                                operation = FakeTransportOperation.READ,
                                success = true,
                                readPayload = "RPM=1440|TPS=2.0|ECT=81.8".encodeToByteArray(),
                            ),
                            FakeTransportStep(
                                operation = FakeTransportOperation.READ,
                                success = true,
                                readPayload = "RPM=1450|TPS=2.1|ECT=82.0".encodeToByteArray(),
                            ),
                            FakeTransportStep(
                                operation = FakeTransportOperation.READ,
                                success = true,
                                readPayload = "RPM=1460|TPS=2.2|ECT=82.1".encodeToByteArray(),
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )
            val useCase = ReadTelemetryUseCase(transportGateway = gateway)

            val result =
                useCase.execute(
                    request = ReadTelemetryRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Nominal telemetry snapshot should return Success state")
                .isInstanceOf(TelemetryUiState.Success::class.java)
            val success = result as TelemetryUiState.Success
            assertThat(success.samples)
                .describedAs("Nominal telemetry snapshot should parse all telemetry signals")
                .hasSize(3)
            assertThat(success.capturedFrameCount)
                .describedAs("Nominal telemetry sampling should capture the configured buffered frame count")
                .isEqualTo(3)
            assertThat(success.bufferedFrames)
                .describedAs("Nominal telemetry sampling should preserve buffered frames for auditability")
                .hasSize(3)
            assertThat(success.samples.map { sample -> sample.signal })
                .describedAs("Parsed telemetry snapshot should include expected signal names")
                .containsExactly("RPM", "TPS", "ECT")
        }
    }

    @Test
    fun invalidBufferConfigurationReturnsValidationError() {
        runBlocking {
            val gateway = FakeTransportGateway(scenario = FakeTransportScenario.of())
            val useCase = ReadTelemetryUseCase(transportGateway = gateway)

            val result =
                useCase.execute(
                    request =
                        ReadTelemetryRequest(
                            ecuFamily = "KEIHIN",
                            endpointHint = "BT",
                            bufferFrameCount = 0,
                            requiredStableFrameCount = 1,
                        ),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Invalid telemetry buffer configuration should return Error before transport operations")
                .isInstanceOf(TelemetryUiState.Error::class.java)
            val error = result as TelemetryUiState.Error
            assertThat(error.code)
                .describedAs("Invalid telemetry buffer configuration should map to TELEMETRY_BUFFER_INVALID")
                .isEqualTo("TELEMETRY_BUFFER_INVALID")
        }
    }

    @Test
    fun unstableSignalSetReturnsUnstableError() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
                            FakeTransportStep(
                                operation = FakeTransportOperation.READ,
                                success = true,
                                readPayload = "RPM=1450|TPS=2.1|ECT=82.0".encodeToByteArray(),
                            ),
                            FakeTransportStep(
                                operation = FakeTransportOperation.READ,
                                success = true,
                                readPayload = "RPM=1455|TPS=2.2".encodeToByteArray(),
                            ),
                            FakeTransportStep(
                                operation = FakeTransportOperation.READ,
                                success = true,
                                readPayload = "RPM=1460|TPS=2.3|ECT=82.1".encodeToByteArray(),
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )
            val useCase = ReadTelemetryUseCase(transportGateway = gateway)

            val result =
                useCase.execute(
                    request = ReadTelemetryRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Inconsistent telemetry signal set across buffered frames should return Error")
                .isInstanceOf(TelemetryUiState.Error::class.java)
            val error = result as TelemetryUiState.Error
            assertThat(error.code)
                .describedAs("Inconsistent telemetry signal set should map to TELEMETRY_UNSTABLE")
                .isEqualTo("TELEMETRY_UNSTABLE")
        }
    }
}
