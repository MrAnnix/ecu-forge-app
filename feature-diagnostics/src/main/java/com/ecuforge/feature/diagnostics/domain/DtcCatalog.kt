package com.ecuforge.feature.diagnostics.domain

/**
 * Domain contract for loading a static DTC reference catalog.
 */
interface DtcCatalogRepository {
    /**
     * Loads the catalog dataset from the configured source.
     */
    fun loadCatalog(): DtcCatalogLoadResult

    /**
     * Loads the best matching catalog for a selected vehicle context.
     *
     * Implementations may fallback to [loadCatalog] when no specific match exists.
     */
    fun loadCatalog(context: VehicleCatalogContext): DtcCatalogLoadResult {
        return loadCatalog()
    }
}

/**
 * Vehicle selection context used to resolve a DTC reference catalog.
 *
 * @property make Vehicle brand selected by the user.
 * @property model Vehicle model selected by the user.
 * @property modelYear Optional vehicle model year.
 */
data class VehicleCatalogContext(
    val make: String,
    val model: String,
    val modelYear: Int?,
)

/**
 * Represents a versioned DTC catalog dataset with provenance metadata.
 *
 * @property catalogId Stable dataset identifier.
 * @property version Semantic version of the dataset.
 * @property source Provenance metadata used for auditability.
 * @property entries Immutable DTC entries available in the dataset.
 */
data class DtcCatalogDataset(
    val catalogId: String,
    val version: String,
    val source: DtcCatalogSource,
    val entries: List<DtcCatalogEntry>,
)

/**
 * Provenance metadata attached to a DTC catalog.
 *
 * @property type Origin category of the source material.
 * @property reference Human-readable source reference.
 * @property receivedAt ISO-8601 date when the data was ingested.
 */
data class DtcCatalogSource(
    val type: String,
    val reference: String,
    val receivedAt: String,
)

/**
 * Reference entry that maps one DTC code to a user-facing description.
 *
 * @property code DTC code in OBD-like format (for example, P0030).
 * @property platform Platform or family qualifier for the code mapping.
 * @property yearFrom First applicable model year.
 * @property yearTo Last applicable model year.
 * @property titleKey Stable key intended for i18n string lookup.
 * @property defaultDescription Fallback description when no localized text is available.
 */
data class DtcCatalogEntry(
    val code: String,
    val platform: String,
    val yearFrom: Int,
    val yearTo: Int,
    val titleKey: String,
    val defaultDescription: String,
)

/**
 * Result wrapper for deterministic catalog loading behavior.
 */
sealed interface DtcCatalogLoadResult {
    /**
     * Successful dataset load with validated content.
     *
     * @property dataset Parsed and validated DTC catalog dataset.
     */
    data class Success(
        val dataset: DtcCatalogDataset,
    ) : DtcCatalogLoadResult

    /**
     * Failed dataset load with stable error metadata.
     *
     * @property code Stable machine-readable failure code.
     * @property message Human-readable failure details for logs.
     */
    data class Failure(
        val code: String,
        val message: String,
    ) : DtcCatalogLoadResult
}
