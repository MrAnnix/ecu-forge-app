package com.ecuforge.feature.diagnostics.data

import com.ecuforge.feature.diagnostics.domain.DtcCatalogDataset
import com.ecuforge.feature.diagnostics.domain.DtcCatalogEntry
import com.ecuforge.feature.diagnostics.domain.DtcCatalogLoadResult
import com.ecuforge.feature.diagnostics.domain.DtcCatalogRepository
import com.ecuforge.feature.diagnostics.domain.DtcCatalogSource
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * Loads a versioned DTC catalog from a JSON resource and validates the dataset.
 */
class VersionedJsonDtcCatalogRepository(
    private val resourcePath: String = DEFAULT_RESOURCE_PATH,
    private val readResource: (String) -> String = ::readResourceText,
) : DtcCatalogRepository {
    override fun loadCatalog(): DtcCatalogLoadResult {
        val rawJson =
            runCatching { readResource(resourcePath) }
                .getOrElse { throwable ->
                    return DtcCatalogLoadResult.Failure(
                        code = "DTC_CATALOG_RESOURCE",
                        message = "Failed to read DTC catalog resource '$resourcePath': ${throwable.message}",
                    )
                }

        return parseAndValidate(rawJson)
    }

    private fun parseAndValidate(rawJson: String): DtcCatalogLoadResult {
        val root =
            runCatching { JSONObject(rawJson) }
                .getOrElse { throwable ->
                    return DtcCatalogLoadResult.Failure(
                        code = "DTC_CATALOG_JSON",
                        message = "Invalid JSON format for DTC catalog: ${throwable.message}",
                    )
                }

        val catalogId = root.optString("catalogId").trim()
        val version = root.optString("version").trim()
        if (catalogId.isEmpty() || version.isEmpty()) {
            return DtcCatalogLoadResult.Failure(
                code = "DTC_CATALOG_METADATA",
                message = "Catalog metadata requires non-empty catalogId and version",
            )
        }

        val sourceJson = root.optJSONObject("source")
        if (sourceJson == null) {
            return DtcCatalogLoadResult.Failure(
                code = "DTC_CATALOG_SOURCE",
                message = "Catalog source metadata is required",
            )
        }

        val source =
            parseSource(sourceJson)
                ?: return DtcCatalogLoadResult.Failure(
                    code = "DTC_CATALOG_SOURCE",
                    message = "Catalog source metadata must include type, reference, and receivedAt (YYYY-MM-DD)",
                )

        val entriesJson = root.optJSONArray("entries")
        if (entriesJson == null || entriesJson.length() == 0) {
            return DtcCatalogLoadResult.Failure(
                code = "DTC_CATALOG_ENTRIES",
                message = "Catalog entries are required and cannot be empty",
            )
        }

        val entries = mutableListOf<DtcCatalogEntry>()
        val duplicateGuard = mutableSetOf<String>()
        for (index in 0 until entriesJson.length()) {
            val entryJson =
                entriesJson.optJSONObject(index)
                    ?: return DtcCatalogLoadResult.Failure(
                        code = "DTC_CATALOG_ENTRY",
                        message = "Catalog entry at index $index must be an object",
                    )

            val entry =
                parseEntry(entryJson)
                    ?: return DtcCatalogLoadResult.Failure(
                        code = "DTC_CATALOG_ENTRY",
                        message = "Catalog entry at index $index is invalid or incomplete",
                    )

            val duplicateKey = "${entry.code}|${entry.platform}|${entry.yearFrom}|${entry.yearTo}"
            if (!duplicateGuard.add(duplicateKey)) {
                return DtcCatalogLoadResult.Failure(
                    code = "DTC_CATALOG_DUPLICATE",
                    message = "Duplicate DTC catalog entry detected for $duplicateKey",
                )
            }

            entries += entry
        }

        return DtcCatalogLoadResult.Success(
            dataset =
                DtcCatalogDataset(
                    catalogId = catalogId,
                    version = version,
                    source = source,
                    entries = entries.toList(),
                ),
        )
    }

    private fun parseSource(sourceJson: JSONObject): DtcCatalogSource? {
        val type = sourceJson.optString("type").trim()
        val reference = sourceJson.optString("reference").trim()
        val receivedAt = sourceJson.optString("receivedAt").trim()
        if (type.isEmpty() || reference.isEmpty() || !receivedAt.matches(RECEIVED_AT_PATTERN)) {
            return null
        }

        return DtcCatalogSource(
            type = type,
            reference = reference,
            receivedAt = receivedAt,
        )
    }

    private fun parseEntry(entryJson: JSONObject): DtcCatalogEntry? {
        val code = entryJson.optString("code").trim().uppercase(Locale.US)
        val platform = entryJson.optString("platform").trim()
        val yearFrom = entryJson.optInt("yearFrom", -1)
        val yearTo = entryJson.optInt("yearTo", -1)
        val titleKey = entryJson.optString("titleKey").trim()
        val defaultDescription = entryJson.optString("defaultDescription").trim()

        val yearsAreValid = yearFrom in MIN_SUPPORTED_YEAR..MAX_SUPPORTED_YEAR && yearTo in yearFrom..MAX_SUPPORTED_YEAR
        val mandatoryFieldsAreValid =
            CODE_PATTERN.matches(code) &&
                platform.isNotEmpty() &&
                titleKey.isNotEmpty() &&
                defaultDescription.isNotEmpty()

        if (!yearsAreValid || !mandatoryFieldsAreValid) {
            return null
        }

        return DtcCatalogEntry(
            code = code,
            platform = platform,
            yearFrom = yearFrom,
            yearTo = yearTo,
            titleKey = titleKey,
            defaultDescription = defaultDescription,
        )
    }

    private companion object {
        private const val DEFAULT_RESOURCE_PATH: String = "dtc/triumph_pcodes_2016_2019.v1.json"
        private const val MIN_SUPPORTED_YEAR: Int = 2000
        private const val MAX_SUPPORTED_YEAR: Int = 2100
        private val CODE_PATTERN = Regex("^P[0-9A-F]{4}$")
        private val RECEIVED_AT_PATTERN = Regex("^\\d{4}-\\d{2}-\\d{2}$")

        private fun readResourceText(path: String): String {
            val inputStream =
                VersionedJsonDtcCatalogRepository::class.java.classLoader?.getResourceAsStream(path)
                    ?: throw IllegalStateException("Resource not found: $path")
            return inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader -> reader.readText() }
        }
    }
}
