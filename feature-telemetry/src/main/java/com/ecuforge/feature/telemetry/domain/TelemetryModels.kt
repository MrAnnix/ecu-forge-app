package com.ecuforge.feature.telemetry.domain

/**
 * Normalized telemetry sample read from ECU payload.
 */
data class TelemetrySample(
    val signal: String,
    val value: Double,
    val unit: String,
)

/**
 * Input request for read-only telemetry snapshot retrieval.
 */
data class ReadTelemetryRequest(
    val ecuFamily: String,
    val endpointHint: String,
)
