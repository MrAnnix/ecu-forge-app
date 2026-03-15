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

class IdentifyEcuUseCaseTest {

    @Test
    fun unsupportedFamilyReturnsErrorBeforeTransport() = runBlocking {
        val gateway = FakeTransportGateway(
            scenario = FakeTransportScenario.of(
                FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true)
            )
        )

        val useCase = IdentifyEcuUseCase(transportGateway = gateway)
        val result = useCase.execute(
            request = IdentificationRequest(ecuFamily = "UNKNOWN", endpointHint = "BT"),
            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF")
        )

        assertTrue(result is IdentificationUiState.Error)
        val error = result as IdentificationUiState.Error
        assertEquals("ECU_UNSUPPORTED", error.code)
    }

    @Test
    fun connectFailureIsMappedToUiError() = runBlocking {
        val gateway = FakeTransportGateway(
            scenario = FakeTransportScenario.of(
                FakeTransportStep(
                    operation = FakeTransportOperation.CONNECT,
                    success = false,
                    errorCode = TransportFailureCode.CONNECTION_FAILED,
                    errorMessage = "Connection failed"
                )
            )
        )

        val useCase = IdentifyEcuUseCase(transportGateway = gateway)
        val result = useCase.execute(
            request = IdentificationRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF")
        )

        assertTrue(result is IdentificationUiState.Error)
        val error = result as IdentificationUiState.Error
        assertEquals("CONNECTION_FAILED", error.code)
    }

    @Test
    fun nominalIdentificationReturnsSuccess() = runBlocking {
        val gateway = FakeTransportGateway(
            scenario = FakeTransportScenario.of(
                FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
                FakeTransportStep(
                    operation = FakeTransportOperation.READ,
                    success = true,
                    readPayload = "MODEL=KM601EU|FW=2.10.4|SN=A1B2C3".encodeToByteArray()
                ),
                FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true)
            )
        )

        val useCase = IdentifyEcuUseCase(transportGateway = gateway)
        val result = useCase.execute(
            request = IdentificationRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF")
        )

        assertTrue(result is IdentificationUiState.Success)
        val success = result as IdentificationUiState.Success
        assertEquals("KM601EU", success.identification.model)
        assertEquals("2.10.4", success.identification.firmwareVersion)
        assertEquals("A1B2C3", success.identification.serialNumber)
    }

    @Test
    fun readTimeoutReturnsError() = runBlocking {
        val gateway = FakeTransportGateway(
            scenario = FakeTransportScenario.of(
                FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
                FakeTransportStep(
                    operation = FakeTransportOperation.READ,
                    success = false,
                    errorCode = TransportFailureCode.TIMEOUT,
                    errorMessage = "Read timeout"
                ),
                FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true)
            )
        )

        val useCase = IdentifyEcuUseCase(transportGateway = gateway)
        val result = useCase.execute(
            request = IdentificationRequest(ecuFamily = "WALBRO", endpointHint = "USB"),
            endpoint = TransportEndpoint.Usb(vendorId = 1027, productId = 48960)
        )

        assertTrue(result is IdentificationUiState.Error)
        val error = result as IdentificationUiState.Error
        assertEquals("TIMEOUT", error.code)
    }

    @Test
    fun coordinatorEmitsLoadingThenTerminalState() = runBlocking {
        val gateway = FakeTransportGateway(
            scenario = FakeTransportScenario.of(
                FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
                FakeTransportStep(
                    operation = FakeTransportOperation.READ,
                    success = true,
                    readPayload = "MODEL=KM601EU|FW=2.10.4|SN=A1B2C3".encodeToByteArray()
                ),
                FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true)
            )
        )

        val useCase = IdentifyEcuUseCase(transportGateway = gateway)
        val coordinator = IdentifyEcuCoordinator(identifyEcuUseCase = useCase)

        val states = coordinator.run(
            request = IdentificationRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF")
        )

        assertEquals(2, states.size)
        assertEquals(IdentificationUiState.Loading, states[0])
        assertTrue(states[1] is IdentificationUiState.Success)
    }
}
