package com.ecuforge.feature.telemetry.domain

/**
 * Export envelope for telemetry snapshots used in support diagnostics.
 *
 * @property schemaVersion Version identifier for the export schema contract.
 * @property exportedAtEpochMillis UTC epoch time when the export payload is generated.
 * @property capturedFrameCount Number of buffered telemetry frames captured in the session.
 * @property sampleCount Number of consolidated telemetry samples included in [samples].
 * @property samples Deterministic list of consolidated telemetry samples in the export.
 */
data class TelemetryExportEnvelope(
    val schemaVersion: String,
    val exportedAtEpochMillis: Long,
    val capturedFrameCount: Int,
    val sampleCount: Int,
    val samples: List<TelemetrySample>,
)
