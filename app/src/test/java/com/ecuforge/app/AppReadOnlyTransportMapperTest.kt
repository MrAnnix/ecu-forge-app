package com.ecuforge.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AppReadOnlyTransportMapperTest {
    @Test
    fun mapBluetoothReturnsBluetoothSelection() {
        val result = AppReadOnlyTransportMapper.map("Bluetooth")

        assertThat(result)
            .describedAs("Transport mapper should resolve Bluetooth selector value")
            .isEqualTo(AppReadOnlyTransport.BLUETOOTH)
    }

    @Test
    fun mapWifiAliasReturnsWifiSelection() {
        val result = AppReadOnlyTransportMapper.map("wi-fi")

        assertThat(result)
            .describedAs("Transport mapper should normalize Wi-Fi alias to WIFI")
            .isEqualTo(AppReadOnlyTransport.WIFI)
    }

    @Test
    fun mapUnknownFallsBackToUsbSelection() {
        val result = AppReadOnlyTransportMapper.map("unknown")

        assertThat(result)
            .describedAs("Transport mapper should fall back to USB for unsupported selector values")
            .isEqualTo(AppReadOnlyTransport.USB)
    }
}
