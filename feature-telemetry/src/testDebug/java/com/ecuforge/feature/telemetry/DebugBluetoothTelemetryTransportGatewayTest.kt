package com.ecuforge.feature.telemetry

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.core.transport.TransportOperationResult
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DebugBluetoothTelemetryTransportGatewayTest {
    @Test
    fun connectRejectsNonBluetoothEndpoint() {
        runBlocking {
            val gateway = DebugBluetoothTelemetryTransportGateway()

            val result =
                gateway.connect(
                    TransportEndpoint.Usb(
                        vendorId = 1027,
                        productId = 48960,
                    ),
                )

            assertThat(result)
                .describedAs("Bluetooth telemetry gateway should reject non-Bluetooth endpoint")
                .isInstanceOf(TransportOperationResult.Failure::class.java)
            val failure = result as TransportOperationResult.Failure
            assertThat(failure.error.code)
                .describedAs("Rejected endpoint should return INVALID_ENDPOINT code")
                .isEqualTo(TransportFailureCode.INVALID_ENDPOINT)
        }
    }

    @Test
    fun connectFailureBehaviorReturnsConnectionFailed() {
        runBlocking {
            val gateway =
                DebugBluetoothTelemetryTransportGateway(
                    behavior = DebugBluetoothTelemetryTransportGateway.Behavior.CONNECT_FAILURE,
                )

            val result =
                gateway.connect(
                    TransportEndpoint.Bluetooth(macAddress = "00:11:22:33:44:55"),
                )

            assertThat(result)
                .describedAs("Connect-failure behavior should return failure result")
                .isInstanceOf(TransportOperationResult.Failure::class.java)
            val failure = result as TransportOperationResult.Failure
            assertThat(failure.error.code)
                .describedAs("Connect-failure behavior should expose CONNECTION_FAILED code")
                .isEqualTo(TransportFailureCode.CONNECTION_FAILED)
        }
    }

    @Test
    fun readTimeoutBehaviorReturnsTimeoutFailure() {
        runBlocking {
            val gateway =
                DebugBluetoothTelemetryTransportGateway(
                    behavior = DebugBluetoothTelemetryTransportGateway.Behavior.READ_TIMEOUT,
                )
            gateway.connect(TransportEndpoint.Bluetooth(macAddress = "00:11:22:33:44:55"))
            gateway.write("TELEMETRY_SNAPSHOT?;FAMILY=KEIHIN;HINT=BLUETOOTH".encodeToByteArray())

            val result = gateway.read(maxBytes = 128)

            assertThat(result)
                .describedAs("Read-timeout behavior should return failure result")
                .isInstanceOf(TransportOperationResult.Failure::class.java)
            val failure = result as TransportOperationResult.Failure
            assertThat(failure.error.code)
                .describedAs("Read-timeout behavior should expose TIMEOUT code")
                .isEqualTo(TransportFailureCode.TIMEOUT)
        }
    }
}
