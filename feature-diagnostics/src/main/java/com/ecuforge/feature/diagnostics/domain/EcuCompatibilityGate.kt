package com.ecuforge.feature.diagnostics.domain

import org.json.JSONObject

/**
 * Validates whether a request belongs to the supported ECU family baseline.
 */
class EcuCompatibilityGate(
    private val supportedFamilies: Set<String> = DEFAULT_SUPPORTED_FAMILIES,
    private val supportedModelsByFamily: Map<String, Set<String>> = DEFAULT_SUPPORTED_MODELS_BY_FAMILY,
    private val supportedModelTransports: Set<ModelTransportSupportKey> = DEFAULT_SUPPORTED_MODEL_TRANSPORTS,
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
     * Returns true when [model] is supported for the requested transport hint.
     */
    fun isModelSupportedForTransport(
        family: String,
        model: String,
        endpointHint: String,
    ): Boolean {
        if (!isModelSupported(family = family, model = model)) {
            return false
        }

        val normalizedTransport = normalizeTransport(endpointHint) ?: return false
        val key =
            ModelTransportSupportKey(
                family = family.uppercase().trim(),
                model = model.uppercase().trim(),
                transport = normalizedTransport,
            )
        return supportedModelTransports.contains(key)
    }

    private fun normalizeTransport(endpointHint: String): String? {
        return when (endpointHint.uppercase().trim()) {
            "BT", "BLUETOOTH" -> "BLUETOOTH"
            "USB" -> "USB"
            else -> null
        }
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

        /**
         * Transport-level evidence map loaded from versioned compatibility resources.
         */
        val DEFAULT_SUPPORTED_MODEL_TRANSPORTS: Set<ModelTransportSupportKey> =
            loadTransportEvidence(resourcePath = DEFAULT_TRANSPORT_EVIDENCE_RESOURCE_PATH)

        private const val DEFAULT_TRANSPORT_EVIDENCE_RESOURCE_PATH: String =
            "compatibility/model_transport_parity.v1.json"

        private fun loadTransportEvidence(resourcePath: String): Set<ModelTransportSupportKey> {
            val inputStream =
                EcuCompatibilityGate::class.java.classLoader?.getResourceAsStream(resourcePath)
                    ?: return emptySet()
            val jsonText = inputStream.bufferedReader(Charsets.UTF_8).use { reader -> reader.readText() }
            val root = runCatching { JSONObject(jsonText) }.getOrElse { return emptySet() }
            val entries = root.optJSONArray("entries") ?: return emptySet()

            val keys = mutableSetOf<ModelTransportSupportKey>()
            for (index in 0 until entries.length()) {
                val entry = entries.optJSONObject(index) ?: continue
                val status = entry.optString("status").trim().uppercase()
                if (status != "VALIDATED") {
                    continue
                }

                val family = entry.optString("family").trim().uppercase()
                val model = entry.optString("model").trim().uppercase()
                val transport = entry.optString("transport").trim().uppercase()
                if (family.isEmpty() || model.isEmpty() || transport.isEmpty()) {
                    continue
                }

                keys +=
                    ModelTransportSupportKey(
                        family = family,
                        model = model,
                        transport = transport,
                    )
            }

            return keys
        }
    }
}

/**
 * Normalized compatibility key for one validated family/model/transport tuple.
 *
 * @property family ECU family identifier.
 * @property model ECU model identifier.
 * @property transport Transport identifier (`BLUETOOTH` or `USB`).
 */
data class ModelTransportSupportKey(
    val family: String,
    val model: String,
    val transport: String,
)
