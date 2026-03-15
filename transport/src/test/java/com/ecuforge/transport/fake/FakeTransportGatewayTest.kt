package com.ecuforge.transport.fake

import com.ecuforge.core.transport.TransportConnectionState
import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.core.transport.TransportOperationResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeTransportGatewayTest {

    @Test
    fun nominalScenarioConnectWriteReadDisconnect() = runBlocking {
        val gateway = FakeTransportGateway(
            scenario = FakeTransportScenario.of(
                FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true, bytesWritten = 4),
                FakeTransportStep(operation = FakeTransportOperation.READ, success = true, readPayload = byteArrayOf(1, 2, 3)),
                FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true)
            )
        )

        val connect = gateway.connect(TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"))
        assertTrue(connect is TransportOperationResult.Success)
        assertEquals(TransportConnectionState.CONNECTED, gateway.currentState())

        val write = gateway.write(byteArrayOf(9, 8, 7, 6))
        assertEquals(4, (write as TransportOperationResult.Success).value)

        val read = gateway.read()
        assertArrayEquals(byteArrayOf(1, 2, 3), (read as TransportOperationResult.Success).value)

        val disconnect = gateway.disconnect()
        assertTrue(disconnect is TransportOperationResult.Success)
        assertEquals(TransportConnectionState.DISCONNECTED, gateway.currentState())
    }

    @Test
    fun connectFailureReturnsScriptedError() = runBlocking {
        val gateway = FakeTransportGateway(
            scenario = FakeTransportScenario.of(
                FakeTransportStep(
                    operation = FakeTransportOperation.CONNECT,
                    success = false,
                    errorCode = TransportFailureCode.CONNECTION_FAILED,
                    errorMessage = "Adapter busy",
                    recoverable = true
                )
            )
        )

        val result = gateway.connect(TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"))
        val failure = result as TransportOperationResult.Failure

        assertEquals(TransportFailureCode.CONNECTION_FAILED, failure.error.code)
        assertEquals("Adapter busy", failure.error.message)
        assertEquals(TransportConnectionState.ERROR, gateway.currentState())
    }

    @Test
    fun invalidEndpointFailsFast() = runBlocking {
        val gateway = FakeTransportGateway(
            scenario = FakeTransportScenario.of(
                FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true)
            )
        )

        val result = gateway.connect(TransportEndpoint.Bluetooth(""))
        val failure = result as TransportOperationResult.Failure

        assertEquals(TransportFailureCode.INVALID_ENDPOINT, failure.error.code)
        assertEquals(TransportConnectionState.ERROR, gateway.currentState())
    }

    @Test
    fun readFailureCanRepresentTimeoutAndRemainRecoverable() = runBlocking {
        val gateway = FakeTransportGateway(
            scenario = FakeTransportScenario.of(
                FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                FakeTransportStep(
                    operation = FakeTransportOperation.READ,
                    success = false,
                    errorCode = TransportFailureCode.TIMEOUT,
                    errorMessage = "Read timeout",
                    recoverable = true
                ),
                FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true)
            )
        )

        val connect = gateway.connect(TransportEndpoint.Usb(vendorId = 1027, productId = 48960))
        assertTrue(connect is TransportOperationResult.Success)

        val read = gateway.read()
        val failure = read as TransportOperationResult.Failure
        assertEquals(TransportFailureCode.TIMEOUT, failure.error.code)
        assertTrue(failure.error.recoverable)

        val disconnect = gateway.disconnect()
        assertTrue(disconnect is TransportOperationResult.Success)
    }
}
