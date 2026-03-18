package com.ecuforge.feature.diagnostics.domain

/**
 * Normalized identification payload extracted from ECU response.
 *
 * @property model ECU model identifier.
 * @property firmwareVersion ECU firmware version string.
 * @property serialNumber ECU serial number.
 */
data class EcuIdentification(
    val model: String,
    val firmwareVersion: String,
    val serialNumber: String,
)

/**
 * Input request for read-only ECU identification.
 *
 * @property ecuFamily ECU family identifier used for compatibility checks.
 * @property endpointHint Transport endpoint hint used by the diagnostics adapter.
 */
data class IdentificationRequest(
    val ecuFamily: String,
    val endpointHint: String,
)
