package com.ecuforge.feature.diagnostics.domain

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.transport.fake.FakeTransportGateway
import com.ecuforge.transport.fake.FakeTransportOperation
import com.ecuforge.transport.fake.FakeTransportScenario
import com.ecuforge.transport.fake.FakeTransportStep
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IdentifyEcuUseCaseTest {
    @Test
    fun blankEndpointHintReturnsRequestValidationErrorBeforeTransport() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario = FakeTransportScenario.of(),
                )

            val useCase = IdentifyEcuUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = IdentificationRequest(ecuFamily = "KEIHIN", endpointHint = " "),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Blank endpoint hint should be rejected before transport operations")
                .isInstanceOf(IdentificationUiState.Error::class.java)
            val error = result as IdentificationUiState.Error
            assertThat(error.code)
                .describedAs("Blank endpoint hint should map to REQUEST_INVALID error code")
                .isEqualTo("REQUEST_INVALID")
        }
    }

    @Test
    fun unsupportedFamilyReturnsErrorBeforeTransport() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                        ),
                )

            val useCase = IdentifyEcuUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = IdentificationRequest(ecuFamily = "UNKNOWN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Unsupported ECU family should return Error before any transport read")
                .isInstanceOf(IdentificationUiState.Error::class.java)
            val error = result as IdentificationUiState.Error
            assertThat(error.code)
                .describedAs("Unsupported ECU family should map to ECU_UNSUPPORTED error code")
                .isEqualTo("ECU_UNSUPPORTED")
        }
    }

    @Test
    fun connectFailureIsMappedToUiError() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(
                                operation = FakeTransportOperation.CONNECT,
                                success = false,
                                errorCode = TransportFailureCode.CONNECTION_FAILED,
                                errorMessage = "Connection failed",
                            ),
                        ),
                )

            val useCase = IdentifyEcuUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = IdentificationRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Connect failure should be mapped to Error UI state")
                .isInstanceOf(IdentificationUiState.Error::class.java)
            val error = result as IdentificationUiState.Error
            assertThat(error.code)
                .describedAs("Connect failure should preserve CONNECTION_FAILED error code")
                .isEqualTo("CONNECTION_FAILED")
        }
    }

    @Test
    fun nominalIdentificationReturnsSuccess() {
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
                                readPayload = "MODEL=KM601EU|FW=2.10.4|SN=A1B2C3".encodeToByteArray(),
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )

            val useCase = IdentifyEcuUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = IdentificationRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Nominal identification scenario should return Success state")
                .isInstanceOf(IdentificationUiState.Success::class.java)
            val success = result as IdentificationUiState.Success
            assertThat(success.identification.model)
                .describedAs("Success state should include parsed ECU model")
                .isEqualTo("KM601EU")
            assertThat(success.identification.firmwareVersion)
                .describedAs("Success state should include parsed firmware version")
                .isEqualTo("2.10.4")
            assertThat(success.identification.serialNumber)
                .describedAs("Success state should include parsed serial number")
                .isEqualTo("A1B2C3")
        }
    }

    @Test
    fun readTimeoutReturnsError() {
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

            val useCase = IdentifyEcuUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = IdentificationRequest(ecuFamily = "WALBRO", endpointHint = "USB"),
                    endpoint = TransportEndpoint.Usb(vendorId = 1027, productId = 48960),
                )

            assertThat(result)
                .describedAs("Timeout during identification read should return Error state")
                .isInstanceOf(IdentificationUiState.Error::class.java)
            val error = result as IdentificationUiState.Error
            assertThat(error.code)
                .describedAs("Timeout during identification should preserve TIMEOUT error code")
                .isEqualTo("TIMEOUT")
        }
    }

    @Test
    fun unsupportedModelTransportCombinationReturnsModelUnsupportedError() {
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
                                readPayload = "MODEL=KM602EU|FW=2.10.4|SN=A1B2C3".encodeToByteArray(),
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )

            val useCase = IdentifyEcuUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = IdentificationRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Non-validated model/transport combinations should be blocked after identification parse")
                .isInstanceOf(IdentificationUiState.Error::class.java)
            val error = result as IdentificationUiState.Error
            assertThat(error.code)
                .describedAs("Unsupported model/transport combinations should return ECU_MODEL_UNSUPPORTED")
                .isEqualTo("ECU_MODEL_UNSUPPORTED")
        }
    }

    @Test
    fun malformedPayloadWithUnknownKeyReturnsParseError() {
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
                                readPayload = "MODEL=KM601EU|FW=2.10.4|EXTRA=A1B2C3".encodeToByteArray(),
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )

            val useCase = IdentifyEcuUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = IdentificationRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Unknown identification payload keys should return parse Error state")
                .isInstanceOf(IdentificationUiState.Error::class.java)
            val error = result as IdentificationUiState.Error
            assertThat(error.code)
                .describedAs("Unknown identification payload keys should map to IDENT_PARSE")
                .isEqualTo("IDENT_PARSE")
        }
    }

    @Test
    fun coordinatorEmitsLoadingThenTerminalState() {
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
                                readPayload = "MODEL=KM601EU|FW=2.10.4|SN=A1B2C3".encodeToByteArray(),
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )

            val useCase = IdentifyEcuUseCase(transportGateway = gateway)
            val coordinator = IdentifyEcuCoordinator(identifyEcuUseCase = useCase)

            val states =
                coordinator.run(
                    request = IdentificationRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(states)
                .describedAs("Coordinator should emit exactly loading and terminal states")
                .hasSize(2)
            assertThat(states[0])
                .describedAs("Coordinator should emit Loading as first state")
                .isEqualTo(IdentificationUiState.Loading)
            assertThat(states[1])
                .describedAs("Coordinator should emit Success as terminal state in nominal scenario")
                .isInstanceOf(IdentificationUiState.Success::class.java)
        }
    }
}
