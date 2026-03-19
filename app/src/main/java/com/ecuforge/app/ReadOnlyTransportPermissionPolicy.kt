package com.ecuforge.app

/**
 * Evaluates transport permission requirements for read-only actions.
 */
internal object ReadOnlyTransportPermissionPolicy {
    /**
     * Resolves missing runtime permissions for the provided [state].
     */
    fun evaluate(state: State): Result {
        if (state.selectedTransport != AppReadOnlyTransport.BLUETOOTH) {
            return Result(missingPermissions = emptyList())
        }

        val missingPermissions = mutableListOf<String>()
        if (state.sdkInt >= SDK_INT_ANDROID_12) {
            if (!state.hasBluetoothConnectPermission) {
                missingPermissions += PERMISSION_BLUETOOTH_CONNECT
            }
            if (!state.hasBluetoothScanPermission) {
                missingPermissions += PERMISSION_BLUETOOTH_SCAN
            }
        } else if (!state.hasFineLocationPermission) {
            missingPermissions += PERMISSION_ACCESS_FINE_LOCATION
        }

        return Result(missingPermissions = missingPermissions)
    }

    /**
     * Input state required to evaluate transport permissions.
     *
     * @property selectedTransport Selected app transport.
     * @property sdkInt Device SDK integer used for permission branching.
     * @property hasBluetoothConnectPermission Whether bluetooth connect permission is granted.
     * @property hasBluetoothScanPermission Whether bluetooth scan permission is granted.
     * @property hasFineLocationPermission Whether fine location permission is granted.
     */
    data class State(
        val selectedTransport: AppReadOnlyTransport,
        val sdkInt: Int,
        val hasBluetoothConnectPermission: Boolean,
        val hasBluetoothScanPermission: Boolean,
        val hasFineLocationPermission: Boolean,
    )

    /**
     * Permission-evaluation result.
     *
     * @property missingPermissions Runtime permissions that must be granted.
     */
    data class Result(
        val missingPermissions: List<String>,
    ) {
        /**
         * Whether all required runtime permissions are already granted.
         */
        val isGranted: Boolean
            get() = missingPermissions.isEmpty()
    }

    const val PRECHECK_TRANSPORT_PERMISSION_REQUIRED: String = "PRECHECK_TRANSPORT_PERMISSION_REQUIRED"
    const val PERMISSION_BLUETOOTH_CONNECT: String = "android.permission.BLUETOOTH_CONNECT"
    const val PERMISSION_BLUETOOTH_SCAN: String = "android.permission.BLUETOOTH_SCAN"
    const val PERMISSION_ACCESS_FINE_LOCATION: String = "android.permission.ACCESS_FINE_LOCATION"
    private const val SDK_INT_ANDROID_12: Int = 31
}
