package com.ecuforge.feature.telemetry.domain

/**
 * Export envelope for telemetry snapshots used in support diagnostics.
 */
data class TelemetryExportEnvelope(
    val schemaVersion: String,
    val exportedAtEpochMillis: Long,
    val capturedFrameCount: Int,
    val sampleCount: Int,
    val samples: List<TelemetrySample>,
)
