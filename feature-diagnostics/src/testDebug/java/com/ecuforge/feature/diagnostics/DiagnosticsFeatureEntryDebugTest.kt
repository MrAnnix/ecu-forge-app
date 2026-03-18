package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DiagnosticsFeatureEntryDebugTest {
    @Test
    fun readOnlyDemoReturnsSuccess() {
        runBlocking {
            val result = DiagnosticsFeatureEntry.identifyReadOnlyDemo()

            assertThat(result)
                .describedAs("Debug identifyReadOnlyDemo should return Success state")
                .isInstanceOf(IdentificationUiState.Success::class.java)
            val success = result as IdentificationUiState.Success
            assertThat(success.identification.model)
                .describedAs("Debug identifyReadOnlyDemo should return KM601EU model from USB transport-backed gateway")
                .isEqualTo("KM601EU")
        }
    }

    @Test
    fun timeoutDemoReturnsTimeoutError() {
        runBlocking {
            val result = DiagnosticsFeatureEntry.identifyReadOnlyTimeoutDemo()

            assertThat(result)
                .describedAs("Debug identifyReadOnlyTimeoutDemo should return Error state")
                .isInstanceOf(IdentificationUiState.Error::class.java)
            val error = result as IdentificationUiState.Error
            assertThat(error.code)
                .describedAs("Timeout demo should expose TIMEOUT error code in debug variant")
                .isEqualTo("TIMEOUT")
        }
    }

    @Test
    fun readDtcDemoReturnsSuccess() {
        runBlocking {
            val result = DiagnosticsFeatureEntry.readDtcReadOnlyDemo()

            assertThat(result)
                .describedAs("Debug readDtcReadOnlyDemo should return Success state")
                .isInstanceOf(DtcUiState.Success::class.java)
            val success = result as DtcUiState.Success
            assertThat(success.dtcs)
                .describedAs("Debug DTC demo should include at least one scripted DTC record")
                .isNotEmpty()
        }
    }

    @Test
    fun readDtcTimeoutDemoReturnsTimeoutError() {
        runBlocking {
            val result = DiagnosticsFeatureEntry.readDtcReadOnlyTimeoutDemo()

            assertThat(result)
                .describedAs("Debug readDtcReadOnlyTimeoutDemo should return Error state")
                .isInstanceOf(DtcUiState.Error::class.java)
            val error = result as DtcUiState.Error
            assertThat(error.code)
                .describedAs("DTC timeout demo should expose TIMEOUT error code in debug variant")
                .isEqualTo("TIMEOUT")
        }
    }

    @Test
    fun readDtcDemoWithVehicleContextCanUseCatalogDescriptions() {
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
                .describedAs("Debug catalog-aware DTC demo should return Success state")
                .isInstanceOf(DtcUiState.Success::class.java)
            val success = result as DtcUiState.Success
            assertThat(success.dtcs)
                .describedAs("Catalog-aware DTC demo should include Triumph catalog description for P0130")
                .anySatisfy { record ->
                    assertThat(record.code)
                        .describedAs("Catalog-aware DTC demo should include scripted code P0130")
                        .isEqualTo("P0130")
                    assertThat(record.description)
                        .describedAs("Catalog-aware DTC demo should replace ECU description with catalog description")
                        .contains("Oxygen sensor cyl 1")
                }
        }
    }
}
