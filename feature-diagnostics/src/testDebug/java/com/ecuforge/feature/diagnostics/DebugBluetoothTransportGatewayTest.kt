package com.ecuforge.feature.diagnostics

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportOperationResult
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DebugBluetoothTransportGatewayTest {
    @Test
    fun connectRejectsUsbEndpointWithInvalidEndpointError() {
        runBlocking {
            val gateway = DebugBluetoothTransportGateway()

            val result = gateway.connect(TransportEndpoint.Usb(vendorId = 1027, productId = 48960))

            assertThat(result)
                .describedAs("Debug Bluetooth gateway should reject non-Bluetooth endpoints")
                .isInstanceOf(TransportOperationResult.Failure::class.java)
            val failure = result as TransportOperationResult.Failure
            assertThat(failure.error.code.name)
                .describedAs("Unsupported endpoint type should map to INVALID_ENDPOINT")
                .isEqualTo("INVALID_ENDPOINT")
        }
    }

    @Test
    fun connectFailureBehaviorReturnsConnectionFailedError() {
        runBlocking {
            val gateway =
                DebugBluetoothTransportGateway(
                    behavior = DebugBluetoothTransportGateway.Behavior.CONNECT_FAILURE,
                )

            val result = gateway.connect(TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"))

            assertThat(result)
                .describedAs("Connect-failure behavior should fail at connect boundary")
                .isInstanceOf(TransportOperationResult.Failure::class.java)
            val failure = result as TransportOperationResult.Failure
            assertThat(failure.error.code.name)
                .describedAs("Connect-failure behavior should map to CONNECTION_FAILED")
                .isEqualTo("CONNECTION_FAILED")
        }
    }

    @Test
    fun readTimeoutBehaviorReturnsTimeoutErrorAfterSuccessfulCommandWrite() {
        runBlocking {
            val gateway =
                DebugBluetoothTransportGateway(
                    behavior = DebugBluetoothTransportGateway.Behavior.READ_TIMEOUT,
                )

            gateway.connect(TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"))
            gateway.write("ID?;FAMILY=KEIHIN;HINT=BT".encodeToByteArray())
            val result = gateway.read(maxBytes = 512)

            assertThat(result)
                .describedAs("Read-timeout behavior should fail at read boundary after successful connect/write")
                .isInstanceOf(TransportOperationResult.Failure::class.java)
            val failure = result as TransportOperationResult.Failure
            assertThat(failure.error.code.name)
                .describedAs("Read-timeout behavior should map to TIMEOUT")
                .isEqualTo("TIMEOUT")
        }
    }
}
