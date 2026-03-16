package com.ecuforge.feature.diagnostics.domain

/**
 * Normalized DTC item read from ECU diagnostics payload.
 */
data class DtcRecord(
    val code: String,
    val description: String,
)

/**
 * Input request for read-only DTC retrieval.
 */
data class ReadDtcRequest(
    val ecuFamily: String,
    val endpointHint: String,
)
