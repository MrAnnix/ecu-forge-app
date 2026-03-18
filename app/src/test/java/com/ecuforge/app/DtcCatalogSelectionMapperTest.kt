package com.ecuforge.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DtcCatalogSelectionMapperTest {
    @Test
    fun disabledCatalogOptInReturnsBaseSelection() {
        val result =
            DtcCatalogSelectionMapper.map(
                makeInput = "Triumph",
                modelInput = "Bonneville T120",
                yearInput = "2018",
                catalogOptIn = false,
            )

        assertThat(result.preferCatalogDescriptions)
            .describedAs("Catalog preference should remain disabled when user opt-in is off")
            .isFalse()
        assertThat(result.vehicleCatalogContext)
            .describedAs("Vehicle context should be omitted when catalog preference is disabled")
            .isNull()
    }

    @Test
    fun enabledCatalogOptInWithValidVehicleBuildsContext() {
        val result =
            DtcCatalogSelectionMapper.map(
                makeInput = "  Triumph ",
                modelInput = " Bonneville T120 ",
                yearInput = " 2018 ",
                catalogOptIn = true,
            )

        assertThat(result.preferCatalogDescriptions)
            .describedAs("Catalog preference should be enabled when user opt-in is active with valid inputs")
            .isTrue()
        assertThat(result.vehicleCatalogContext)
            .describedAs("Vehicle context should be mapped from trimmed make/model/year inputs")
            .isNotNull()
        val context = result.vehicleCatalogContext
        assertThat(context?.make)
            .describedAs("Make should be trimmed before building vehicle context")
            .isEqualTo("Triumph")
        assertThat(context?.model)
            .describedAs("Model should be trimmed before building vehicle context")
            .isEqualTo("Bonneville T120")
        assertThat(context?.modelYear)
            .describedAs("Numeric year should be parsed when provided")
            .isEqualTo(2018)
    }

    @Test
    fun enabledCatalogOptInWithInvalidYearKeepsNullYear() {
        val result =
            DtcCatalogSelectionMapper.map(
                makeInput = "Triumph",
                modelInput = "Bonneville T120",
                yearInput = "20X8",
                catalogOptIn = true,
            )

        assertThat(result.preferCatalogDescriptions)
            .describedAs("Catalog preference should remain enabled when make and model are valid")
            .isTrue()
        assertThat(result.vehicleCatalogContext)
            .describedAs("Vehicle context should still be built when year is optional and invalid")
            .isNotNull()
        assertThat(result.vehicleCatalogContext?.modelYear)
            .describedAs("Invalid year input should be ignored and mapped to null")
            .isNull()
    }

    @Test
    fun enabledCatalogOptInWithoutMakeOrModelFallsBackToBaseSelection() {
        val result =
            DtcCatalogSelectionMapper.map(
                makeInput = " ",
                modelInput = "Bonneville T120",
                yearInput = "2018",
                catalogOptIn = true,
            )

        assertThat(result.preferCatalogDescriptions)
            .describedAs("Catalog preference should be disabled when make/model are incomplete")
            .isFalse()
        assertThat(result.vehicleCatalogContext)
            .describedAs("Vehicle context should be omitted when make/model inputs are incomplete")
            .isNull()
    }
}
