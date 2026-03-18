package com.ecuforge.app

import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext

/**
 * Maps raw vehicle selector inputs into a catalog-aware diagnostics selection.
 */
internal object DtcCatalogSelectionMapper {
    /**
     * Builds a normalized selection from UI inputs.
     */
    fun map(
        makeInput: String,
        modelInput: String,
        yearInput: String,
        catalogOptIn: Boolean,
    ): DtcCatalogSelection {
        if (!catalogOptIn) {
            return DtcCatalogSelection(
                vehicleCatalogContext = null,
                preferCatalogDescriptions = false,
            )
        }

        val make = makeInput.trim()
        val model = modelInput.trim()
        if (make.isEmpty() || model.isEmpty()) {
            return DtcCatalogSelection(
                vehicleCatalogContext = null,
                preferCatalogDescriptions = false,
            )
        }

        val parsedYear = yearInput.trim().toIntOrNull()
        return DtcCatalogSelection(
            vehicleCatalogContext =
                VehicleCatalogContext(
                    make = make,
                    model = model,
                    modelYear = parsedYear,
                ),
            preferCatalogDescriptions = true,
        )
    }
}

/**
 * Catalog-aware DTC selection produced from UI inputs.
 *
 * @property vehicleCatalogContext Optional selected vehicle context for catalog resolution.
 * @property preferCatalogDescriptions Whether catalog descriptions should be preferred over ECU text.
 */
data class DtcCatalogSelection(
    val vehicleCatalogContext: VehicleCatalogContext?,
    val preferCatalogDescriptions: Boolean,
)
