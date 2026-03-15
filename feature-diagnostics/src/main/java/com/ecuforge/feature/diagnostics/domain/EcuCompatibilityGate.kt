package com.ecuforge.feature.diagnostics.domain

class EcuCompatibilityGate(
    private val supportedFamilies: Set<String> = DEFAULT_SUPPORTED_FAMILIES
) {
    fun isSupported(request: IdentificationRequest): Boolean {
        if (request.ecuFamily.isBlank()) {
            return false
        }
        return supportedFamilies.contains(request.ecuFamily.uppercase())
    }

    companion object {
        val DEFAULT_SUPPORTED_FAMILIES: Set<String> = setOf(
            "KEIHIN",
            "SIEMENS",
            "MARELLI",
            "WALBRO"
        )
    }
}
