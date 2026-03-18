package com.ecuforge.feature.diagnostics.domain

/**
 * Validates whether a request belongs to the supported ECU family baseline.
 */
class EcuCompatibilityGate(
    private val supportedFamilies: Set<String> = DEFAULT_SUPPORTED_FAMILIES,
    private val supportedModelsByFamily: Map<String, Set<String>> = DEFAULT_SUPPORTED_MODELS_BY_FAMILY,
) {
    /**
     * Returns true when the request family is non-blank and supported.
     */
    fun isSupported(request: IdentificationRequest): Boolean {
        if (request.ecuFamily.isBlank()) {
            return false
        }
        return supportedFamilies.contains(request.ecuFamily.uppercase())
    }

    /**
     * Returns true when [model] is part of the validated baseline for the [family].
     */
    fun isModelSupported(
        family: String,
        model: String,
    ): Boolean {
        if (family.isBlank() || model.isBlank()) {
            return false
        }

        val familyKey = family.uppercase().trim()
        val modelKey = model.uppercase().trim()
        val supportedModels = supportedModelsByFamily[familyKey] ?: return false
        return supportedModels.contains(modelKey)
    }

    /**
     * Default compatibility constants for read-only diagnostics bootstrap.
     */
    companion object {
        /**
         * Default supported ECU family set used by diagnostics baseline flows.
         */
        val DEFAULT_SUPPORTED_FAMILIES: Set<String> =
            setOf(
                "KEIHIN",
                "SIEMENS",
                "MARELLI",
                "WALBRO",
            )

        /**
         * Model-level baseline evidence map used for compatibility traceability.
         */
        val DEFAULT_SUPPORTED_MODELS_BY_FAMILY: Map<String, Set<String>> =
            mapOf(
                "KEIHIN" to setOf("KM601EU", "KM602EU"),
                "SIEMENS" to setOf("SIE-ECU-01"),
                "MARELLI" to setOf("IAW5AM"),
                "WALBRO" to setOf("WB-ECU-01"),
            )
    }
}
