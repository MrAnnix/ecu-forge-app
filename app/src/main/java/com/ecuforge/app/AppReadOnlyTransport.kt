package com.ecuforge.app

import com.ecuforge.feature.diagnostics.DiagnosticsReadOnlyTransport
import com.ecuforge.feature.telemetry.TelemetryReadOnlyTransport

/**
 * App-level transport selection shared by diagnostics and telemetry actions.
 */
internal enum class AppReadOnlyTransport {
    BLUETOOTH,
    USB,
    WIFI,
}

/**
 * Maps transport selector input into a supported app transport with predictable fallback.
 */
internal object AppReadOnlyTransportMapper {
    /**
     * Maps raw selector text to [AppReadOnlyTransport] using case-insensitive matching.
     */
    fun map(rawSelection: String): AppReadOnlyTransport {
        return when (rawSelection.trim().uppercase()) {
            "BLUETOOTH" -> AppReadOnlyTransport.BLUETOOTH
            "WIFI", "WI-FI" -> AppReadOnlyTransport.WIFI
            else -> AppReadOnlyTransport.USB
        }
    }
}

/**
 * Converts app transport selection into diagnostics feature transport enum.
 */
internal fun AppReadOnlyTransport.toDiagnosticsTransport(): DiagnosticsReadOnlyTransport {
    return when (this) {
        AppReadOnlyTransport.BLUETOOTH -> DiagnosticsReadOnlyTransport.BLUETOOTH
        AppReadOnlyTransport.USB -> DiagnosticsReadOnlyTransport.USB
        AppReadOnlyTransport.WIFI -> DiagnosticsReadOnlyTransport.WIFI
    }
}

/**
 * Converts app transport selection into telemetry feature transport enum.
 */
internal fun AppReadOnlyTransport.toTelemetryTransport(): TelemetryReadOnlyTransport {
    return when (this) {
        AppReadOnlyTransport.BLUETOOTH -> TelemetryReadOnlyTransport.BLUETOOTH
        AppReadOnlyTransport.USB -> TelemetryReadOnlyTransport.USB
        AppReadOnlyTransport.WIFI -> TelemetryReadOnlyTransport.WIFI
    }
}
