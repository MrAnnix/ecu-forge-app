package com.ecuforge.app

import android.content.Context

/**
 * Persists read-only transport profiles in app-private shared preferences.
 */
internal class AppTransportProfileStore(context: Context) {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /**
     * Loads stored profile for the selected [transport], falling back to deterministic defaults.
     */
    fun load(transport: AppReadOnlyTransport): AppTransportProfile {
        return when (transport) {
            AppReadOnlyTransport.BLUETOOTH ->
                AppTransportProfile.Bluetooth(
                    connectTimeoutMs = preferences.getInt(KEY_CONNECT_TIMEOUT_MS, DEFAULT_CONNECT_TIMEOUT_MS),
                    readTimeoutMs = preferences.getInt(KEY_READ_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS),
                    macAddress =
                        preferences.getString(KEY_BLUETOOTH_MAC, DEFAULT_BLUETOOTH_MAC)
                            ?: DEFAULT_BLUETOOTH_MAC,
                )

            AppReadOnlyTransport.USB ->
                AppTransportProfile.Usb(
                    connectTimeoutMs = preferences.getInt(KEY_CONNECT_TIMEOUT_MS, DEFAULT_CONNECT_TIMEOUT_MS),
                    readTimeoutMs = preferences.getInt(KEY_READ_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS),
                    vendorId = preferences.getInt(KEY_USB_VENDOR_ID, DEFAULT_USB_VENDOR_ID),
                    productId = preferences.getInt(KEY_USB_PRODUCT_ID, DEFAULT_USB_PRODUCT_ID),
                )

            AppReadOnlyTransport.WIFI ->
                AppTransportProfile.Wifi(
                    connectTimeoutMs = preferences.getInt(KEY_CONNECT_TIMEOUT_MS, DEFAULT_CONNECT_TIMEOUT_MS),
                    readTimeoutMs = preferences.getInt(KEY_READ_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS),
                    host = preferences.getString(KEY_WIFI_HOST, DEFAULT_WIFI_HOST) ?: DEFAULT_WIFI_HOST,
                    port = preferences.getInt(KEY_WIFI_PORT, DEFAULT_WIFI_PORT),
                )
        }
    }

    /**
     * Saves [profile] and marks it as the active transport profile.
     */
    fun save(profile: AppTransportProfile) {
        preferences
            .edit()
            .putString(KEY_SELECTED_TRANSPORT, profile.transport.name)
            .putInt(KEY_CONNECT_TIMEOUT_MS, profile.connectTimeoutMs)
            .putInt(KEY_READ_TIMEOUT_MS, profile.readTimeoutMs)
            .apply {
                when (profile) {
                    is AppTransportProfile.Bluetooth -> {
                        putString(KEY_BLUETOOTH_MAC, profile.macAddress)
                    }

                    is AppTransportProfile.Usb -> {
                        putInt(KEY_USB_VENDOR_ID, profile.vendorId)
                        putInt(KEY_USB_PRODUCT_ID, profile.productId)
                    }

                    is AppTransportProfile.Wifi -> {
                        putString(KEY_WIFI_HOST, profile.host)
                        putInt(KEY_WIFI_PORT, profile.port)
                    }
                }
            }
            .apply()
    }

    /**
     * Returns last selected transport fallbacking to USB when no value is stored.
     */
    fun loadSelectedTransport(): AppReadOnlyTransport {
        val raw = preferences.getString(KEY_SELECTED_TRANSPORT, AppReadOnlyTransport.USB.name).orEmpty()
        return AppReadOnlyTransportMapper.map(raw)
    }

    private companion object {
        const val PREFERENCES_NAME: String = "ecu_forge_transport_profiles"

        const val KEY_SELECTED_TRANSPORT: String = "selected_transport"
        const val KEY_CONNECT_TIMEOUT_MS: String = "connect_timeout_ms"
        const val KEY_READ_TIMEOUT_MS: String = "read_timeout_ms"

        const val KEY_BLUETOOTH_MAC: String = "bluetooth_mac"
        const val KEY_USB_VENDOR_ID: String = "usb_vendor_id"
        const val KEY_USB_PRODUCT_ID: String = "usb_product_id"
        const val KEY_WIFI_HOST: String = "wifi_host"
        const val KEY_WIFI_PORT: String = "wifi_port"

        const val DEFAULT_CONNECT_TIMEOUT_MS: Int = 5000
        const val DEFAULT_READ_TIMEOUT_MS: Int = 3000

        const val DEFAULT_BLUETOOTH_MAC: String = "00:11:22:33:44:55"
        const val DEFAULT_USB_VENDOR_ID: Int = 1027
        const val DEFAULT_USB_PRODUCT_ID: Int = 48960
        const val DEFAULT_WIFI_HOST: String = "192.168.0.10"
        const val DEFAULT_WIFI_PORT: Int = 35000
    }
}
