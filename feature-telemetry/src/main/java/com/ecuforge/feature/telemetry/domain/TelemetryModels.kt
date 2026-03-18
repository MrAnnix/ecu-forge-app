package com.ecuforge.feature.telemetry.domain

/**
 * Normalized telemetry sample read from ECU payload.
 *
 * @property signal Signal identifier reported by ECU.
 * @property value Numeric signal value.
 * @property unit Engineering unit associated with [value].
 */
data class TelemetrySample(
    val signal: String,
    val value: Double,
    val unit: String,
)

/**
 * Input request for read-only telemetry retrieval with buffered sampling.
 *
 * @property ecuFamily ECU family identifier used for compatibility checks.
 * @property endpointHint Transport endpoint hint used by telemetry adapter.
 * @property bufferFrameCount Number of frame snapshots to retain in the rolling buffer.
 * @property requiredStableFrameCount Number of stable frames required before reporting success.
 */
data class ReadTelemetryRequest(
    val ecuFamily: String,
    val endpointHint: String,
    val bufferFrameCount: Int = 3,
    val requiredStableFrameCount: Int = 2,
)
