package com.ecuforge.feature.diagnostics.data

import com.ecuforge.feature.diagnostics.domain.DtcCatalogLoadResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VersionedJsonDtcCatalogRepositoryTest {
    @Test
    fun loadCatalogReturnsValidatedDatasetFromDefaultResource() {
        val repository = VersionedJsonDtcCatalogRepository()

        val result = repository.loadCatalog()

        assertThat(result)
            .describedAs("Default DTC dataset resource should parse and validate successfully")
            .isInstanceOf(DtcCatalogLoadResult.Success::class.java)
        val success = result as DtcCatalogLoadResult.Success
        assertThat(success.dataset.catalogId)
            .describedAs("Loaded dataset should preserve the stable catalog id")
            .isEqualTo("triumph-modern-classics-2016-2019")
        assertThat(success.dataset.entries)
            .describedAs("Loaded dataset should contain known reference code P1690")
            .anySatisfy { entry ->
                assertThat(entry.code)
                    .describedAs("One of the entries should map to code P1690")
                    .isEqualTo("P1690")
                assertThat(entry.defaultDescription)
                    .describedAs("P1690 entry should include a useful default description")
                    .contains("CAN-bus network fault")
            }
    }

    @Test
    fun duplicateEntriesReturnDeterministicFailure() {
        val duplicatePayload =
            """
            {
              "catalogId": "catalog",
              "version": "1.0.0",
              "source": {
                "type": "manual",
                "reference": "test",
                "receivedAt": "2026-03-18"
              },
              "entries": [
                { "code": "P0030", "platform": "Triumph", "yearFrom": 2016, "yearTo": 2019, "titleKey": "dtc.p0030.title", "defaultDescription": "desc" },
                { "code": "P0030", "platform": "Triumph", "yearFrom": 2016, "yearTo": 2019, "titleKey": "dtc.p0030.duplicate", "defaultDescription": "desc2" }
              ]
            }
            """.trimIndent()

        val repository = VersionedJsonDtcCatalogRepository(readResource = { duplicatePayload })

        val result = repository.loadCatalog()

        assertThat(result)
            .describedAs("Duplicated code/platform/year tuple should fail dataset validation")
            .isInstanceOf(DtcCatalogLoadResult.Failure::class.java)
        val failure = result as DtcCatalogLoadResult.Failure
        assertThat(failure.code)
            .describedAs("Duplicate validation should map to DTC_CATALOG_DUPLICATE")
            .isEqualTo("DTC_CATALOG_DUPLICATE")
    }

    @Test
    fun invalidCodeFormatReturnsEntryFailure() {
        val invalidCodePayload =
            """
            {
              "catalogId": "catalog",
              "version": "1.0.0",
              "source": {
                "type": "manual",
                "reference": "test",
                "receivedAt": "2026-03-18"
              },
              "entries": [
                { "code": "P1607/8", "platform": "Triumph", "yearFrom": 2016, "yearTo": 2019, "titleKey": "dtc.p1607_8.title", "defaultDescription": "desc" }
              ]
            }
            """.trimIndent()

        val repository = VersionedJsonDtcCatalogRepository(readResource = { invalidCodePayload })

        val result = repository.loadCatalog()

        assertThat(result)
            .describedAs("Non canonical DTC code formats should be rejected explicitly")
            .isInstanceOf(DtcCatalogLoadResult.Failure::class.java)
        val failure = result as DtcCatalogLoadResult.Failure
        assertThat(failure.code)
            .describedAs("Invalid code format should map to DTC_CATALOG_ENTRY failure code")
            .isEqualTo("DTC_CATALOG_ENTRY")
    }

    @Test
    fun missingSourceMetadataReturnsSourceFailure() {
        val missingSourcePayload =
            """
            {
              "catalogId": "catalog",
              "version": "1.0.0",
              "entries": [
                { "code": "P0030", "platform": "Triumph", "yearFrom": 2016, "yearTo": 2019, "titleKey": "dtc.p0030.title", "defaultDescription": "desc" }
              ]
            }
            """.trimIndent()

        val repository = VersionedJsonDtcCatalogRepository(readResource = { missingSourcePayload })

        val result = repository.loadCatalog()

        assertThat(result)
            .describedAs("Catalog source metadata should be mandatory for provenance and auditability")
            .isInstanceOf(DtcCatalogLoadResult.Failure::class.java)
        val failure = result as DtcCatalogLoadResult.Failure
        assertThat(failure.code)
            .describedAs("Missing source metadata should map to DTC_CATALOG_SOURCE")
            .isEqualTo("DTC_CATALOG_SOURCE")
    }
}

