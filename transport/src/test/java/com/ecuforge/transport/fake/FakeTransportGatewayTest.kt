package com.ecuforge.transport.fake

import com.ecuforge.core.transport.TransportConnectionState
import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.core.transport.TransportOperationResult
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FakeTransportGatewayTest {
    @Test
    fun nominalScenarioConnectWriteReadDisconnect() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true, bytesWritten = 4),
                            FakeTransportStep(operation = FakeTransportOperation.READ, success = true, readPayload = byteArrayOf(1, 2, 3)),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )

            val connect = gateway.connect(TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"))
            assertThat(connect)
                .describedAs("Connect step should succeed in nominal scripted scenario")
                .isInstanceOf(TransportOperationResult.Success::class.java)
            assertThat(gateway.currentState())
                .describedAs("Gateway state should be CONNECTED after successful connect")
                .isEqualTo(TransportConnectionState.CONNECTED)

            val write = gateway.write(byteArrayOf(9, 8, 7, 6))
            assertThat((write as TransportOperationResult.Success).value)
                .describedAs("Write result should expose scripted bytes-written count")
                .isEqualTo(4)

            val read = gateway.read()
            assertThat((read as TransportOperationResult.Success).value)
                .describedAs("Read result should expose scripted payload bytes")
                .containsExactly(1, 2, 3)

            val disconnect = gateway.disconnect()
            assertThat(disconnect)
                .describedAs("Disconnect step should succeed in nominal scripted scenario")
                .isInstanceOf(TransportOperationResult.Success::class.java)
            assertThat(gateway.currentState())
                .describedAs("Gateway state should be DISCONNECTED after successful disconnect")
                .isEqualTo(TransportConnectionState.DISCONNECTED)
        }
    }

    @Test
    fun connectFailureReturnsScriptedError() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(
                                operation = FakeTransportOperation.CONNECT,
                                success = false,
                                errorCode = TransportFailureCode.CONNECTION_FAILED,
                                errorMessage = "Adapter busy",
                                recoverable = true,
                            ),
                        ),
                )

            val result = gateway.connect(TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"))
            val failure = result as TransportOperationResult.Failure

            assertThat(failure.error.code)
                .describedAs("Connection failure should preserve scripted error code")
                .isEqualTo(TransportFailureCode.CONNECTION_FAILED)
            assertThat(failure.error.message)
                .describedAs("Connection failure should preserve scripted error message")
                .isEqualTo("Adapter busy")
            assertThat(gateway.currentState())
                .describedAs("Gateway should transition to ERROR state after failed connect")
                .isEqualTo(TransportConnectionState.ERROR)
        }
    }

    @Test
    fun invalidEndpointFailsFast() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                        ),
                )

            val result = gateway.connect(TransportEndpoint.Bluetooth(""))
            val failure = result as TransportOperationResult.Failure

            assertThat(failure.error.code)
                .describedAs("Invalid endpoint should be rejected with INVALID_ENDPOINT error code")
                .isEqualTo(TransportFailureCode.INVALID_ENDPOINT)
            assertThat(gateway.currentState())
                .describedAs("Invalid endpoint should move gateway state to ERROR")
                .isEqualTo(TransportConnectionState.ERROR)
        }
    }

    @Test
    fun readFailureCanRepresentTimeoutAndRemainRecoverable() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                            FakeTransportStep(
                                operation = FakeTransportOperation.READ,
                                success = false,
                                errorCode = TransportFailureCode.TIMEOUT,
                                errorMessage = "Read timeout",
                                recoverable = true,
                            ),
                            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                        ),
                )

            val connect = gateway.connect(TransportEndpoint.Usb(vendorId = 1027, productId = 48960))
            assertThat(connect)
                .describedAs("Connect should succeed before timeout read simulation")
                .isInstanceOf(TransportOperationResult.Success::class.java)

            val read = gateway.read()
            val failure = read as TransportOperationResult.Failure
            assertThat(failure.error.code)
                .describedAs("Read failure should map to scripted TIMEOUT code")
                .isEqualTo(TransportFailureCode.TIMEOUT)
            assertThat(failure.error.recoverable)
                .describedAs("Scripted timeout should remain recoverable for retry flows")
                .isTrue()

            val disconnect = gateway.disconnect()
            assertThat(disconnect)
                .describedAs("Disconnect should still succeed after recoverable read timeout")
                .isInstanceOf(TransportOperationResult.Success::class.java)
        }
    }

    @Test
    fun wifiEndpointConnectsWhenHostAndPortAreValid() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                        ),
                )

            val result = gateway.connect(TransportEndpoint.Wifi(host = "192.168.0.10", port = 35000))

            assertThat(result)
                .describedAs("Valid WiFi endpoint should be accepted by fake gateway endpoint validation")
                .isInstanceOf(TransportOperationResult.Success::class.java)
        }
    }

    @Test
    fun wifiEndpointFailsFastWhenPortIsOutOfRange() {
        runBlocking {
            val gateway =
                FakeTransportGateway(
                    scenario =
                        FakeTransportScenario.of(
                            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                        ),
                )

            val result = gateway.connect(TransportEndpoint.Wifi(host = "192.168.0.10", port = 0))
            val failure = result as TransportOperationResult.Failure

            assertThat(failure.error.code)
                .describedAs("Invalid WiFi endpoint should be rejected with INVALID_ENDPOINT")
                .isEqualTo(TransportFailureCode.INVALID_ENDPOINT)
            assertThat(gateway.currentState())
                .describedAs("Invalid WiFi endpoint should move gateway state to ERROR")
                .isEqualTo(TransportConnectionState.ERROR)
        }
    }
}
