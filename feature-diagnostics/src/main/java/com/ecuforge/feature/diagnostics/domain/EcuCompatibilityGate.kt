package com.ecuforge.feature.diagnostics.domain

/**
 * Validates whether a request belongs to the supported ECU family baseline.
 */
class EcuCompatibilityGate(
    private val supportedFamilies: Set<String> = DEFAULT_SUPPORTED_FAMILIES,
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
    }
}
