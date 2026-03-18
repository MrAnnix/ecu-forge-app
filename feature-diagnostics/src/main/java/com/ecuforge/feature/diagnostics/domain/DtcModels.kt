package com.ecuforge.feature.diagnostics.domain

/**
 * Normalized DTC item read from ECU diagnostics payload.
 *
 * @property code Stable DTC code emitted by the ECU.
 * @property description Human-readable description associated with [code].
 */
data class DtcRecord(
    val code: String,
    val description: String,
)

/**
 * Input request for read-only DTC retrieval.
 *
 * @property ecuFamily ECU family identifier used for compatibility checks.
 * @property endpointHint Transport endpoint hint used by the diagnostics adapter.
 * @property vehicleCatalogContext Optional user-selected vehicle context for DTC catalog resolution.
 * @property preferCatalogDescriptions Whether to prefer catalog descriptions when code mappings exist.
 */
data class ReadDtcRequest(
    val ecuFamily: String,
    val endpointHint: String,
    val vehicleCatalogContext: VehicleCatalogContext? = null,
    val preferCatalogDescriptions: Boolean = false,
)
