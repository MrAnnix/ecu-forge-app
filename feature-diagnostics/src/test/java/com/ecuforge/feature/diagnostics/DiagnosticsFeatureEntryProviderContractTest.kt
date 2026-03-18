package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DiagnosticsFeatureEntryProviderContractTest {
    @Test
    fun installedProviderOverridesDefaultEntrypointBehavior() {
        runBlocking {
            val provider =
                object : DiagnosticsFlowProvider {
                    override suspend fun identifyReadOnlyDemo(): IdentificationUiState {
                        return IdentificationUiState.Error(code = "PROVIDER", message = "custom identify")
                    }

                    override suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
                        return IdentificationUiState.Error(code = "PROVIDER", message = "custom timeout")
                    }

                    override suspend fun readDtcReadOnlyDemo(): DtcUiState {
                        return DtcUiState.Error(code = "PROVIDER", message = "custom dtc")
                    }

                    override suspend fun readDtcReadOnlyDemo(
                        vehicleCatalogContext: VehicleCatalogContext?,
                        preferCatalogDescriptions: Boolean,
                    ): DtcUiState {
                        return DtcUiState.Error(code = "PROVIDER", message = "custom dtc catalog")
                    }

                    override suspend fun readDtcReadOnlyTimeoutDemo(): DtcUiState {
                        return DtcUiState.Error(code = "PROVIDER", message = "custom dtc timeout")
                    }
                }

            DiagnosticsFeatureEntry.installProvider(provider)
            try {
                val identify = DiagnosticsFeatureEntry.identifyReadOnlyDemo()
                val dtc = DiagnosticsFeatureEntry.readDtcReadOnlyDemo()
                val dtcCatalogAware =
                    DiagnosticsFeatureEntry.readDtcReadOnlyDemo(
                        vehicleCatalogContext =
                            VehicleCatalogContext(
                                make = "Triumph",
                                model = "Bonneville T120",
                                modelYear = 2018,
                            ),
                        preferCatalogDescriptions = true,
                    )

                assertThat(identify)
                    .describedAs("Installed diagnostics provider should override identification flow implementation")
                    .isInstanceOf(IdentificationUiState.Error::class.java)
                assertThat((identify as IdentificationUiState.Error).code)
                    .describedAs("Installed diagnostics provider should expose custom identification error code")
                    .isEqualTo("PROVIDER")

                assertThat(dtc)
                    .describedAs("Installed diagnostics provider should override DTC flow implementation")
                    .isInstanceOf(DtcUiState.Error::class.java)
                assertThat((dtc as DtcUiState.Error).code)
                    .describedAs("Installed diagnostics provider should expose custom DTC error code")
                    .isEqualTo("PROVIDER")

                assertThat(dtcCatalogAware)
                    .describedAs("Installed diagnostics provider should override catalog-aware DTC flow implementation")
                    .isInstanceOf(DtcUiState.Error::class.java)
                assertThat((dtcCatalogAware as DtcUiState.Error).code)
                    .describedAs("Installed diagnostics provider should expose custom catalog-aware DTC error code")
                    .isEqualTo("PROVIDER")
            } finally {
                DiagnosticsFeatureEntry.resetProvider()
            }
        }
    }
}
