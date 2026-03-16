package com.ecuforge.feature.diagnostics.domain

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.transport.fake.FakeTransportGateway
import com.ecuforge.transport.fake.FakeTransportOperation
import com.ecuforge.transport.fake.FakeTransportScenario
import com.ecuforge.transport.fake.FakeTransportStep
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadDtcUseCaseTest {
    @Test
    fun unsupportedFamilyReturnsErrorBeforeTransport() =
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

            assertTrue(result is DtcUiState.Error)
            val error = result as DtcUiState.Error
            assertEquals("ECU_UNSUPPORTED", error.code)
        }

    @Test
    fun connectFailureIsMappedToUiError() =
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

            assertTrue(result is DtcUiState.Error)
            val error = result as DtcUiState.Error
            assertEquals("CONNECTION_FAILED", error.code)
        }

    @Test
    fun writeFailureIsMappedToUiError() =
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

            assertTrue(result is DtcUiState.Error)
            val error = result as DtcUiState.Error
            assertEquals("IO_ERROR", error.code)
        }

    @Test
    fun readTimeoutIsMappedToUiError() =
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

            assertTrue(result is DtcUiState.Error)
            val error = result as DtcUiState.Error
            assertEquals("TIMEOUT", error.code)
        }

    @Test
    fun invalidPayloadReturnsParseError() =
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

            assertTrue(result is DtcUiState.Error)
            val error = result as DtcUiState.Error
            assertEquals("DTC_PARSE", error.code)
        }

    @Test
    fun nonePayloadReturnsSuccessWithEmptyList() =
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

            assertTrue(result is DtcUiState.Success)
            val success = result as DtcUiState.Success
            assertTrue(success.dtcs.isEmpty())
        }
}
