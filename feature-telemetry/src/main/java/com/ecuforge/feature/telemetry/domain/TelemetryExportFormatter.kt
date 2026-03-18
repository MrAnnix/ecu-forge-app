package com.ecuforge.feature.telemetry.domain

import java.math.BigDecimal

/**
 * Produces deterministic JSON telemetry export payloads for support diagnostics.
 */
class TelemetryExportFormatter {
    /**
     * Builds an export envelope from [successState] and [exportedAtEpochMillis].
     */
    fun toEnvelope(
        successState: TelemetryUiState.Success,
        exportedAtEpochMillis: Long,
    ): TelemetryExportEnvelope {
        val normalizedSamples =
            successState.samples
                .map { sample ->
                    sample.copy(
                        signal = sample.signal.trim(),
                        unit = sample.unit.trim(),
                    )
                }
                .sortedBy { sample -> sample.signal }

        return TelemetryExportEnvelope(
            schemaVersion = SCHEMA_VERSION,
            exportedAtEpochMillis = exportedAtEpochMillis,
            capturedFrameCount = successState.capturedFrameCount,
            sampleCount = normalizedSamples.size,
            samples = normalizedSamples,
        )
    }

    /**
     * Serializes [envelope] into deterministic JSON with stable key ordering.
     */
    fun toJson(envelope: TelemetryExportEnvelope): String {
        val samplesJson =
            envelope.samples.joinToString(separator = ",") { sample ->
                "{" +
                    "\"signal\":\"${escapeJson(sample.signal)}\"," +
                    "\"value\":${formatDouble(sample.value)}," +
                    "\"unit\":\"${escapeJson(sample.unit)}\"" +
                    "}"
            }

        return "{" +
            "\"schemaVersion\":\"${escapeJson(envelope.schemaVersion)}\"," +
            "\"exportedAtEpochMillis\":${envelope.exportedAtEpochMillis}," +
            "\"capturedFrameCount\":${envelope.capturedFrameCount}," +
            "\"sampleCount\":${envelope.sampleCount}," +
            "\"samples\":[$samplesJson]" +
            "}"
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }

    private fun formatDouble(value: Double): String {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    }

    private companion object {
        const val SCHEMA_VERSION: String = "telemetry-export.v1"
    }
}
