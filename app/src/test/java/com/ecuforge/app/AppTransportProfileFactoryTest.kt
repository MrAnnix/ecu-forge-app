package com.ecuforge.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AppTransportProfileFactoryTest {
    @Test
    fun buildBluetoothProfileReturnsTypedProfileWhenInputsAreValid() {
        val result =
            AppTransportProfileFactory.build(
                transport = AppReadOnlyTransport.BLUETOOTH,
                primaryValue = "00:11:22:33:44:55",
                secondaryValue = "",
                connectTimeoutValue = "5000",
                readTimeoutValue = "3000",
            )

        assertThat(result.validation.isValid)
            .describedAs("Bluetooth profile validation should pass for valid MAC and timeout values")
            .isTrue()
        assertThat(result.profile)
            .describedAs("Bluetooth profile build should return typed profile instance")
            .isInstanceOf(AppTransportProfile.Bluetooth::class.java)
    }

    @Test
    fun buildWifiProfileRejectsInvalidPort() {
        val result =
            AppTransportProfileFactory.build(
                transport = AppReadOnlyTransport.WIFI,
                primaryValue = "192.168.0.10",
                secondaryValue = "70000",
                connectTimeoutValue = "5000",
                readTimeoutValue = "3000",
            )

        assertThat(result.validation.isValid)
            .describedAs("WiFi profile validation should fail when TCP port is outside valid range")
            .isFalse()
        assertThat(result.validation.errors)
            .describedAs("WiFi profile validation should include actionable port validation message")
            .anySatisfy { error ->
                assertThat(error)
                    .describedAs("Port validation error should explicitly mention range requirement")
                    .contains("1..65535")
            }
    }

    @Test
    fun buildUsbProfileRejectsMissingVendorId() {
        val result =
            AppTransportProfileFactory.build(
                transport = AppReadOnlyTransport.USB,
                primaryValue = "",
                secondaryValue = "48960",
                connectTimeoutValue = "5000",
                readTimeoutValue = "3000",
            )

        assertThat(result.validation.isValid)
            .describedAs("USB profile validation should fail when vendor ID is missing")
            .isFalse()
        assertThat(result.validation.errors)
            .describedAs("USB profile validation should include vendor ID validation message")
            .anySatisfy { error ->
                assertThat(error)
                    .describedAs("Vendor validation error should explicitly mention vendor ID range")
                    .contains("vendor ID")
            }
    }
}
