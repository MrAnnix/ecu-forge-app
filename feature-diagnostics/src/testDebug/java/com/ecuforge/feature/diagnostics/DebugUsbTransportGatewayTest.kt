package com.ecuforge.feature.diagnostics

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportOperationResult
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DebugUsbTransportGatewayTest {
    @Test
    fun connectRejectsBluetoothEndpointWithInvalidEndpointError() {
        runBlocking {
            val gateway = DebugUsbTransportGateway()

            val result = gateway.connect(TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"))

            assertThat(result)
                .describedAs("Debug USB gateway should reject non-USB endpoints")
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
                DebugUsbTransportGateway(
                    behavior = DebugUsbTransportGateway.Behavior.CONNECT_FAILURE,
                )

            val result = gateway.connect(TransportEndpoint.Usb(vendorId = 1027, productId = 48960))

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
                DebugUsbTransportGateway(
                    behavior = DebugUsbTransportGateway.Behavior.READ_TIMEOUT,
                )

            gateway.connect(TransportEndpoint.Usb(vendorId = 1027, productId = 48960))
            gateway.write("ID?;FAMILY=KEIHIN;HINT=USB".encodeToByteArray())
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
