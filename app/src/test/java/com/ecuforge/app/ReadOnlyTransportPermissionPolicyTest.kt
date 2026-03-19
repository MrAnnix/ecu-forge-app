package com.ecuforge.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReadOnlyTransportPermissionPolicyTest {
    @Test
    fun usbTransportDoesNotRequireRuntimePermissions() {
        val result =
            ReadOnlyTransportPermissionPolicy.evaluate(
                state =
                    ReadOnlyTransportPermissionPolicy.State(
                        selectedTransport = AppReadOnlyTransport.USB,
                        sdkInt = 35,
                        hasBluetoothConnectPermission = false,
                        hasBluetoothScanPermission = false,
                        hasFineLocationPermission = false,
                    ),
            )

        assertThat(result.isGranted)
            .describedAs("USB transport should not require bluetooth or location runtime permissions")
            .isTrue()
        assertThat(result.missingPermissions)
            .describedAs("USB transport should not report missing runtime permissions")
            .isEmpty()
    }

    @Test
    fun bluetoothOnAndroid12PlusRequiresConnectAndScanPermissions() {
        val result =
            ReadOnlyTransportPermissionPolicy.evaluate(
                state =
                    ReadOnlyTransportPermissionPolicy.State(
                        selectedTransport = AppReadOnlyTransport.BLUETOOTH,
                        sdkInt = 35,
                        hasBluetoothConnectPermission = false,
                        hasBluetoothScanPermission = false,
                        hasFineLocationPermission = true,
                    ),
            )

        assertThat(result.isGranted)
            .describedAs("Bluetooth transport should fail precheck when Android 12+ nearby-device permissions are missing")
            .isFalse()
        assertThat(result.missingPermissions)
            .describedAs("Bluetooth transport on Android 12+ should request both CONNECT and SCAN permissions")
            .containsExactly(
                ReadOnlyTransportPermissionPolicy.PERMISSION_BLUETOOTH_CONNECT,
                ReadOnlyTransportPermissionPolicy.PERMISSION_BLUETOOTH_SCAN,
            )
    }

    @Test
    fun bluetoothOnAndroid12PlusPassesWhenConnectAndScanAreGranted() {
        val result =
            ReadOnlyTransportPermissionPolicy.evaluate(
                state =
                    ReadOnlyTransportPermissionPolicy.State(
                        selectedTransport = AppReadOnlyTransport.BLUETOOTH,
                        sdkInt = 35,
                        hasBluetoothConnectPermission = true,
                        hasBluetoothScanPermission = true,
                        hasFineLocationPermission = false,
                    ),
            )

        assertThat(result.isGranted)
            .describedAs("Bluetooth transport should pass precheck on Android 12+ when CONNECT and SCAN are granted")
            .isTrue()
        assertThat(result.missingPermissions)
            .describedAs("No missing permissions should be reported after granting Android 12+ bluetooth permissions")
            .isEmpty()
    }

    @Test
    fun bluetoothOnAndroid11AndLowerRequiresFineLocationPermission() {
        val result =
            ReadOnlyTransportPermissionPolicy.evaluate(
                state =
                    ReadOnlyTransportPermissionPolicy.State(
                        selectedTransport = AppReadOnlyTransport.BLUETOOTH,
                        sdkInt = 30,
                        hasBluetoothConnectPermission = false,
                        hasBluetoothScanPermission = false,
                        hasFineLocationPermission = false,
                    ),
            )

        assertThat(result.isGranted)
            .describedAs("Bluetooth transport should fail precheck on Android 11 and lower when location permission is missing")
            .isFalse()
        assertThat(result.missingPermissions)
            .describedAs("Bluetooth transport on Android 11 and lower should request fine location permission")
            .containsExactly(ReadOnlyTransportPermissionPolicy.PERMISSION_ACCESS_FINE_LOCATION)
    }

    @Test
    fun bluetoothOnAndroid11AndLowerPassesWhenFineLocationPermissionIsGranted() {
        val result =
            ReadOnlyTransportPermissionPolicy.evaluate(
                state =
                    ReadOnlyTransportPermissionPolicy.State(
                        selectedTransport = AppReadOnlyTransport.BLUETOOTH,
                        sdkInt = 30,
                        hasBluetoothConnectPermission = false,
                        hasBluetoothScanPermission = false,
                        hasFineLocationPermission = true,
                    ),
            )

        assertThat(result.isGranted)
            .describedAs("Bluetooth transport should pass precheck on Android 11 and lower when location is granted")
            .isTrue()
        assertThat(result.missingPermissions)
            .describedAs("No permissions should be missing when Android 11 and lower location permission is granted")
            .isEmpty()
    }
}

