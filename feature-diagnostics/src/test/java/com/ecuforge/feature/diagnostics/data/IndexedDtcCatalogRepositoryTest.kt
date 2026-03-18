package com.ecuforge.feature.diagnostics.data

import com.ecuforge.feature.diagnostics.domain.DtcCatalogLoadResult
import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IndexedDtcCatalogRepositoryTest {
    @Test
    fun loadCatalogWithoutContextUsesDefaultCatalogFromIndex() {
        val repository = IndexedDtcCatalogRepository()

        val result = repository.loadCatalog()

        assertThat(result)
            .describedAs("Index-based repository should load default catalog when no vehicle context is provided")
            .isInstanceOf(DtcCatalogLoadResult.Success::class.java)
        val success = result as DtcCatalogLoadResult.Success
        assertThat(success.dataset.catalogId)
            .describedAs("Default indexed catalog should resolve to the Triumph Modern Classics dataset")
            .isEqualTo("triumph-modern-classics-2016-2019")
    }

    @Test
    fun loadCatalogWithMatchingVehicleUsesIndexedCatalog() {
        val repository = IndexedDtcCatalogRepository()

        val result =
            repository.loadCatalog(
                VehicleCatalogContext(
                    make = "Triumph",
                    model = "Bonneville T120",
                    modelYear = 2018,
                ),
            )

        assertThat(result)
            .describedAs("Matching vehicle context should resolve a catalog via the index mapping")
            .isInstanceOf(DtcCatalogLoadResult.Success::class.java)
        val success = result as DtcCatalogLoadResult.Success
        assertThat(success.dataset.entries)
            .describedAs("Resolved indexed dataset should expose known code mappings")
            .anySatisfy { entry ->
                assertThat(entry.code)
                    .describedAs("Indexed dataset should include code P0030")
                    .isEqualTo("P0030")
            }
    }

    @Test
    fun loadCatalogWithUnknownMakeFallsBackToDefaultCatalog() {
        val repository = IndexedDtcCatalogRepository()

        val result =
            repository.loadCatalog(
                VehicleCatalogContext(
                    make = "OtherBrand",
                    model = "Model X",
                    modelYear = 2020,
                ),
            )

        assertThat(result)
            .describedAs("Unknown make/model should fallback to default catalog instead of failing")
            .isInstanceOf(DtcCatalogLoadResult.Success::class.java)
        val success = result as DtcCatalogLoadResult.Success
        assertThat(success.dataset.catalogId)
            .describedAs("Fallback selection should still return the default configured catalog")
            .isEqualTo("triumph-modern-classics-2016-2019")
    }
}

