package com.ecuforge.feature.diagnostics.data

import com.ecuforge.feature.diagnostics.domain.DtcCatalogLoadResult
import com.ecuforge.feature.diagnostics.domain.DtcCatalogRepository
import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext
import java.nio.charset.StandardCharsets
import java.util.Locale
import org.json.JSONObject

/**
 * Resolves a DTC catalog resource from an index and delegates loading to a versioned JSON repository.
 */
class IndexedDtcCatalogRepository(
    private val indexResourcePath: String = DEFAULT_INDEX_RESOURCE_PATH,
    private val readResource: (String) -> String = ::readResourceText,
) : DtcCatalogRepository {
    override fun loadCatalog(): DtcCatalogLoadResult {
        val selectionResult = selectCatalogResource(context = null)
        return when (selectionResult) {
            is CatalogResourceSelection.Success -> {
                VersionedJsonDtcCatalogRepository(
                    resourcePath = selectionResult.resourcePath,
                    readResource = readResource,
                ).loadCatalog()
            }

            is CatalogResourceSelection.Failure -> {
                DtcCatalogLoadResult.Failure(
                    code = selectionResult.code,
                    message = selectionResult.message,
                )
            }
        }
    }

    override fun loadCatalog(context: VehicleCatalogContext): DtcCatalogLoadResult {
        val selectionResult = selectCatalogResource(context = context)
        return when (selectionResult) {
            is CatalogResourceSelection.Success -> {
                VersionedJsonDtcCatalogRepository(
                    resourcePath = selectionResult.resourcePath,
                    readResource = readResource,
                ).loadCatalog()
            }

            is CatalogResourceSelection.Failure -> {
                DtcCatalogLoadResult.Failure(
                    code = selectionResult.code,
                    message = selectionResult.message,
                )
            }
        }
    }

    private fun selectCatalogResource(context: VehicleCatalogContext?): CatalogResourceSelection {
        val indexRoot =
            runCatching { JSONObject(readResource(indexResourcePath)) }
                .getOrElse { throwable ->
                    return CatalogResourceSelection.Failure(
                        code = "DTC_CATALOG_INDEX",
                        message = "Failed to load catalog index '$indexResourcePath': ${throwable.message}",
                    )
                }

        val defaultCatalogPath = indexRoot.optString("defaultCatalog").trim()
        if (defaultCatalogPath.isEmpty()) {
            return CatalogResourceSelection.Failure(
                code = "DTC_CATALOG_INDEX",
                message = "Catalog index requires non-empty defaultCatalog",
            )
        }

        if (context == null) {
            return CatalogResourceSelection.Success(defaultCatalogPath)
        }

        val normalizedMake = context.make.trim().lowercase(Locale.US)
        val normalizedModel = context.model.trim().lowercase(Locale.US)
        if (normalizedMake.isEmpty() || normalizedModel.isEmpty()) {
            return CatalogResourceSelection.Success(defaultCatalogPath)
        }

        val catalogs = indexRoot.optJSONArray("catalogs")
        if (catalogs == null) {
            return CatalogResourceSelection.Success(defaultCatalogPath)
        }

        for (index in 0 until catalogs.length()) {
            val entry = catalogs.optJSONObject(index) ?: continue
            val entryMake = entry.optString("make").trim().lowercase(Locale.US)
            val entryModel = entry.optString("model").trim().lowercase(Locale.US)
            val yearFrom = entry.optInt("yearFrom", Int.MIN_VALUE)
            val yearTo = entry.optInt("yearTo", Int.MAX_VALUE)
            val resourcePath = entry.optString("resourcePath").trim()

            if (entryMake.isEmpty() || entryModel.isEmpty() || resourcePath.isEmpty()) {
                continue
            }

            val makeMatches = entryMake == normalizedMake
            val modelMatches = entryModel == "*" || entryModel == normalizedModel
            val yearMatches =
                context.modelYear == null ||
                    context.modelYear in yearFrom..yearTo

            if (makeMatches && modelMatches && yearMatches) {
                return CatalogResourceSelection.Success(resourcePath)
            }
        }

        return CatalogResourceSelection.Success(defaultCatalogPath)
    }

    private sealed interface CatalogResourceSelection {
        data class Success(
            val resourcePath: String,
        ) : CatalogResourceSelection

        data class Failure(
            val code: String,
            val message: String,
        ) : CatalogResourceSelection
    }

    private companion object {
        private const val DEFAULT_INDEX_RESOURCE_PATH: String = "dtc/catalog_index.v1.json"

        private fun readResourceText(path: String): String {
            val inputStream =
                IndexedDtcCatalogRepository::class.java.classLoader?.getResourceAsStream(path)
                    ?: throw IllegalStateException("Resource not found: $path")
            return inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader -> reader.readText() }
        }
    }
}

