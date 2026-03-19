package com.ecuforge.feature.telemetry

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportOperationResult
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DebugWifiTelemetryTransportGatewayTest {
    @Test
    fun connectRejectsUsbEndpointWithInvalidEndpointError() {
        runBlocking {
            val gateway = DebugWifiTelemetryTransportGateway()

            val result = gateway.connect(TransportEndpoint.Usb(vendorId = 1027, productId = 48960))

            assertThat(result)
                .describedAs("Debug WiFi telemetry gateway should reject non-WiFi endpoints")
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
                DebugWifiTelemetryTransportGateway(
                    behavior = DebugWifiTelemetryTransportGateway.Behavior.CONNECT_FAILURE,
                )

            val result = gateway.connect(TransportEndpoint.Wifi(host = "192.168.0.10", port = 35000))

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
                DebugWifiTelemetryTransportGateway(
                    behavior = DebugWifiTelemetryTransportGateway.Behavior.READ_TIMEOUT,
                )

            gateway.connect(TransportEndpoint.Wifi(host = "192.168.0.10", port = 35000))
            gateway.write("TELEMETRY_SNAPSHOT?;FAMILY=KEIHIN;HINT=WIFI".encodeToByteArray())
            val result = gateway.read(maxBytes = 1024)

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
