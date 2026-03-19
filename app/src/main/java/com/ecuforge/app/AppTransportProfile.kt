package com.ecuforge.app

import com.ecuforge.feature.diagnostics.DiagnosticsReadOnlyConnectionSettings
import com.ecuforge.feature.diagnostics.DiagnosticsReadOnlyTransport
import com.ecuforge.feature.telemetry.TelemetryReadOnlyConnectionSettings
import com.ecuforge.feature.telemetry.TelemetryReadOnlyTransport

/**
 * User-configurable transport profile for read-only diagnostics and telemetry sessions.
 */
internal sealed interface AppTransportProfile {
    /**
     * User-selected transport type.
     */
    val transport: AppReadOnlyTransport

    /**
     * Connection timeout in milliseconds.
     */
    val connectTimeoutMs: Int

    /**
     * Read timeout in milliseconds.
     */
    val readTimeoutMs: Int

    /**
     * Bluetooth profile using adapter MAC address.
     */
    data class Bluetooth(
        override val connectTimeoutMs: Int,
        override val readTimeoutMs: Int,
        val macAddress: String,
    ) : AppTransportProfile {
        override val transport: AppReadOnlyTransport = AppReadOnlyTransport.BLUETOOTH
    }

    /**
     * USB profile using vendor and product identifiers.
     */
    data class Usb(
        override val connectTimeoutMs: Int,
        override val readTimeoutMs: Int,
        val vendorId: Int,
        val productId: Int,
    ) : AppTransportProfile {
        override val transport: AppReadOnlyTransport = AppReadOnlyTransport.USB
    }

    /**
     * WiFi profile using adapter host and port.
     */
    data class Wifi(
        override val connectTimeoutMs: Int,
        override val readTimeoutMs: Int,
        val host: String,
        val port: Int,
    ) : AppTransportProfile {
        override val transport: AppReadOnlyTransport = AppReadOnlyTransport.WIFI
    }
}

/**
 * Validation output for user-provided transport profile data.
 */
internal data class AppTransportProfileValidationResult(
    /**
     * True when all validation checks pass.
     */
    val isValid: Boolean,
    /**
     * Deterministic, user-visible validation error messages.
     */
    val errors: List<String>,
)

/**
 * Parses and validates transport profile values captured in the app transport form.
 */
internal object AppTransportProfileFactory {
    private const val MIN_TIMEOUT_MS: Int = 500
    private const val MAX_TIMEOUT_MS: Int = 30000

    /**
     * Builds a typed transport profile from raw form values.
     */
    fun build(
        transport: AppReadOnlyTransport,
        primaryValue: String,
        secondaryValue: String,
        connectTimeoutValue: String,
        readTimeoutValue: String,
    ): Result {
        val errors = mutableListOf<String>()

        val connectTimeoutMs = parseTimeout("Connection timeout", connectTimeoutValue, errors)
        val readTimeoutMs = parseTimeout("Read timeout", readTimeoutValue, errors)

        val profile =
            when (transport) {
                AppReadOnlyTransport.BLUETOOTH -> {
                    val mac = primaryValue.trim().uppercase()
                    if (!isValidMacAddress(mac)) {
                        errors.add("Bluetooth MAC address must match XX:XX:XX:XX:XX:XX")
                        null
                    } else if (connectTimeoutMs == null || readTimeoutMs == null) {
                        null
                    } else {
                        AppTransportProfile.Bluetooth(
                            connectTimeoutMs = connectTimeoutMs,
                            readTimeoutMs = readTimeoutMs,
                            macAddress = mac,
                        )
                    }
                }

                AppReadOnlyTransport.USB -> {
                    val vendorId = primaryValue.trim().toIntOrNull()
                    val productId = secondaryValue.trim().toIntOrNull()
                    if (vendorId == null || vendorId !in 1..65535) {
                        errors.add("USB vendor ID must be in range 1..65535")
                    }
                    if (productId == null || productId !in 1..65535) {
                        errors.add("USB product ID must be in range 1..65535")
                    }

                    if (connectTimeoutMs == null || readTimeoutMs == null || vendorId == null || productId == null) {
                        null
                    } else {
                        AppTransportProfile.Usb(
                            connectTimeoutMs = connectTimeoutMs,
                            readTimeoutMs = readTimeoutMs,
                            vendorId = vendorId,
                            productId = productId,
                        )
                    }
                }

                AppReadOnlyTransport.WIFI -> {
                    val host = primaryValue.trim()
                    val port = secondaryValue.trim().toIntOrNull()
                    if (host.isBlank()) {
                        errors.add("WiFi host is required")
                    }
                    if (port == null || port !in 1..65535) {
                        errors.add("WiFi port must be in range 1..65535")
                    }

                    if (connectTimeoutMs == null || readTimeoutMs == null || host.isBlank() || port == null) {
                        null
                    } else {
                        AppTransportProfile.Wifi(
                            connectTimeoutMs = connectTimeoutMs,
                            readTimeoutMs = readTimeoutMs,
                            host = host,
                            port = port,
                        )
                    }
                }
            }

        val validationResult =
            AppTransportProfileValidationResult(
                isValid = errors.isEmpty(),
                errors = errors.toList(),
            )

        return Result(
            profile = profile,
            validation = validationResult,
        )
    }

    /**
     * Typed build output for transport profile parsing and validation.
     */
    data class Result(
        /**
         * Built profile when validation succeeds.
         */
        val profile: AppTransportProfile?,
        /**
         * Validation result including explicit error messages.
         */
        val validation: AppTransportProfileValidationResult,
    )

    private fun parseTimeout(
        label: String,
        value: String,
        errors: MutableList<String>,
    ): Int? {
        val parsed = value.trim().toIntOrNull()
        if (parsed == null || parsed !in MIN_TIMEOUT_MS..MAX_TIMEOUT_MS) {
            errors.add("$label must be in range $MIN_TIMEOUT_MS..$MAX_TIMEOUT_MS ms")
            return null
        }

        return parsed
    }

    private fun isValidMacAddress(value: String): Boolean {
        val regex = Regex("^[0-9A-F]{2}(:[0-9A-F]{2}){5}$")
        return regex.matches(value)
    }
}

/**
 * Maps transport profile into diagnostics connection settings.
 */
internal fun AppTransportProfile.toDiagnosticsConnectionSettings(): DiagnosticsReadOnlyConnectionSettings {
    return when (this) {
        is AppTransportProfile.Bluetooth ->
            DiagnosticsReadOnlyConnectionSettings(
                transport = DiagnosticsReadOnlyTransport.BLUETOOTH,
                bluetoothMacAddress = macAddress,
            )

        is AppTransportProfile.Usb ->
            DiagnosticsReadOnlyConnectionSettings(
                transport = DiagnosticsReadOnlyTransport.USB,
                usbVendorId = vendorId,
                usbProductId = productId,
            )

        is AppTransportProfile.Wifi ->
            DiagnosticsReadOnlyConnectionSettings(
                transport = DiagnosticsReadOnlyTransport.WIFI,
                wifiHost = host,
                wifiPort = port,
            )
    }
}

/**
 * Maps transport profile into telemetry connection settings.
 */
internal fun AppTransportProfile.toTelemetryConnectionSettings(): TelemetryReadOnlyConnectionSettings {
    return when (this) {
        is AppTransportProfile.Bluetooth ->
            TelemetryReadOnlyConnectionSettings(
                transport = TelemetryReadOnlyTransport.BLUETOOTH,
                bluetoothMacAddress = macAddress,
            )

        is AppTransportProfile.Usb ->
            TelemetryReadOnlyConnectionSettings(
                transport = TelemetryReadOnlyTransport.USB,
                usbVendorId = vendorId,
                usbProductId = productId,
            )

        is AppTransportProfile.Wifi ->
            TelemetryReadOnlyConnectionSettings(
                transport = TelemetryReadOnlyTransport.WIFI,
                wifiHost = host,
                wifiPort = port,
            )
    }
}
