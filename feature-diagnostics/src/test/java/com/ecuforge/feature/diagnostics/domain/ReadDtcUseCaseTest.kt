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

class ReadDtcUseCaseTest {
    @Test
    fun unsupportedFamilyReturnsErrorBeforeTransport() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario = FakeTransportScenario.of(),
                )

            val useCase = ReadDtcUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = ReadDtcRequest(ecuFamily = "UNKNOWN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Unsupported ECU family should return DTC Error state before transport interaction")
                .isInstanceOf(DtcUiState.Error::class.java)
            val error = result as DtcUiState.Error
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

            val useCase = ReadDtcUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = ReadDtcRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Connection failure in DTC flow should return Error state")
                .isInstanceOf(DtcUiState.Error::class.java)
            val error = result as DtcUiState.Error
            assertThat(error.code)
                .describedAs("Connection failure should preserve CONNECTION_FAILED error code")
                .isEqualTo("CONNECTION_FAILED")
        }
    }

    @Test
    fun writeFailureIsMappedToUiError() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                            FakeTransportStep(
                                operation = FakeTransportOperation.WRITE,
                                success = false,
                                errorCode = TransportFailureCode.IO_ERROR,
                                errorMessage = "Write failed",
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )

            val useCase = ReadDtcUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = ReadDtcRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Write failure in DTC flow should return Error state")
                .isInstanceOf(DtcUiState.Error::class.java)
            val error = result as DtcUiState.Error
            assertThat(error.code)
                .describedAs("Write failure should preserve IO_ERROR error code")
                .isEqualTo("IO_ERROR")
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

            val useCase = ReadDtcUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = ReadDtcRequest(ecuFamily = "WALBRO", endpointHint = "USB"),
                    endpoint = TransportEndpoint.Usb(vendorId = 1027, productId = 48960),
                )

            assertThat(result)
                .describedAs("Read timeout in DTC flow should return Error state")
                .isInstanceOf(DtcUiState.Error::class.java)
            val error = result as DtcUiState.Error
            assertThat(error.code)
                .describedAs("Read timeout should preserve TIMEOUT error code")
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
                                readPayload = "BROKEN_PAYLOAD".encodeToByteArray(),
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )

            val useCase = ReadDtcUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = ReadDtcRequest(ecuFamily = "MARELLI", endpointHint = "BT"),
                    endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                )

            assertThat(result)
                .describedAs("Invalid DTC payload should return Error state")
                .isInstanceOf(DtcUiState.Error::class.java)
            val error = result as DtcUiState.Error
            assertThat(error.code)
                .describedAs("Invalid DTC payload should map to DTC_PARSE error code")
                .isEqualTo("DTC_PARSE")
        }
    }

    @Test
    fun nonePayloadReturnsSuccessWithEmptyList() {
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
                                readPayload = "NONE".encodeToByteArray(),
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )

            val useCase = ReadDtcUseCase(transportGateway = gateway)
            val result =
                useCase.execute(
                    request = ReadDtcRequest(ecuFamily = "SIEMENS", endpointHint = "USB"),
                    endpoint = TransportEndpoint.Usb(vendorId = 1027, productId = 48960),
                )

            assertThat(result)
                .describedAs("NONE payload should return Success state with no DTC records")
                .isInstanceOf(DtcUiState.Success::class.java)
            val success = result as DtcUiState.Success
            assertThat(success.dtcs)
                .describedAs("NONE payload should map to an empty DTC list")
                .isEmpty()
        }
    }
}
