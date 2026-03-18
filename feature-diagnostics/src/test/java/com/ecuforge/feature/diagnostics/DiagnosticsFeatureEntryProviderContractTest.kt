package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
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

                    override suspend fun readDtcReadOnlyTimeoutDemo(): DtcUiState {
                        return DtcUiState.Error(code = "PROVIDER", message = "custom dtc timeout")
                    }
                }

            DiagnosticsFeatureEntry.installProvider(provider)
            try {
                val identify = DiagnosticsFeatureEntry.identifyReadOnlyDemo()
                val dtc = DiagnosticsFeatureEntry.readDtcReadOnlyDemo()

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
            } finally {
                DiagnosticsFeatureEntry.resetProvider()
            }
        }
    }
}
