package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DiagnosticsFeatureEntryReleaseTest {
    @Test
    fun readOnlyDemoIsDisabledInRelease() {
        runBlocking {
            val result = DiagnosticsFeatureEntry.identifyReadOnlyDemo()

            assertThat(result)
                .describedAs("Release identifyReadOnlyDemo should be blocked and return Error state")
                .isInstanceOf(IdentificationUiState.Error::class.java)
            val error = result as IdentificationUiState.Error
            assertThat(error.code)
                .describedAs("Release identifyReadOnlyDemo should return DEMO_DISABLED code")
                .isEqualTo("DEMO_DISABLED")
        }
    }

    @Test
    fun timeoutDemoIsDisabledInRelease() {
        runBlocking {
            val result = DiagnosticsFeatureEntry.identifyReadOnlyTimeoutDemo()

            assertThat(result)
                .describedAs("Release identifyReadOnlyTimeoutDemo should be blocked and return Error state")
                .isInstanceOf(IdentificationUiState.Error::class.java)
            val error = result as IdentificationUiState.Error
            assertThat(error.code)
                .describedAs("Release identifyReadOnlyTimeoutDemo should return DEMO_DISABLED code")
                .isEqualTo("DEMO_DISABLED")
        }
    }

    @Test
    fun readDtcDemoIsDisabledInRelease() {
        runBlocking {
            val result = DiagnosticsFeatureEntry.readDtcReadOnlyDemo()

            assertThat(result)
                .describedAs("Release readDtcReadOnlyDemo should be blocked and return Error state")
                .isInstanceOf(DtcUiState.Error::class.java)
            val error = result as DtcUiState.Error
            assertThat(error.code)
                .describedAs("Release readDtcReadOnlyDemo should return DEMO_DISABLED code")
                .isEqualTo("DEMO_DISABLED")
        }
    }

    @Test
    fun readDtcTimeoutDemoIsDisabledInRelease() {
        runBlocking {
            val result = DiagnosticsFeatureEntry.readDtcReadOnlyTimeoutDemo()

            assertThat(result)
                .describedAs("Release readDtcReadOnlyTimeoutDemo should be blocked and return Error state")
                .isInstanceOf(DtcUiState.Error::class.java)
            val error = result as DtcUiState.Error
            assertThat(error.code)
                .describedAs("Release readDtcReadOnlyTimeoutDemo should return DEMO_DISABLED code")
                .isEqualTo("DEMO_DISABLED")
        }
    }

    @Test
    fun readDtcCatalogAwareDemoIsDisabledInRelease() {
        runBlocking {
            val result =
                DiagnosticsFeatureEntry.readDtcReadOnlyDemo(
                    vehicleCatalogContext =
                        VehicleCatalogContext(
                            make = "Triumph",
                            model = "Bonneville T120",
                            modelYear = 2018,
                        ),
                    preferCatalogDescriptions = true,
                )

            assertThat(result)
                .describedAs("Release catalog-aware DTC demo should be blocked and return Error state")
                .isInstanceOf(DtcUiState.Error::class.java)
            val error = result as DtcUiState.Error
            assertThat(error.code)
                .describedAs("Release catalog-aware DTC demo should return DEMO_DISABLED code")
                .isEqualTo("DEMO_DISABLED")
        }
    }
}
