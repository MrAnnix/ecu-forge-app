package com.ecuforge.feature.diagnostics.domain

/**
 * Normalized identification payload extracted from ECU response.
 */
data class EcuIdentification(
    val model: String,
    val firmwareVersion: String,
    val serialNumber: String,
)

/**
 * Input request for read-only ECU identification.
 */
data class IdentificationRequest(
    val ecuFamily: String,
    val endpointHint: String,
)
