package com.ecuforge.feature.diagnostics

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.feature.diagnostics.domain.DtcCatalogDataset
import com.ecuforge.feature.diagnostics.domain.DtcCatalogEntry
import com.ecuforge.feature.diagnostics.domain.DtcCatalogLoadResult
import com.ecuforge.feature.diagnostics.domain.DtcCatalogRepository
import com.ecuforge.feature.diagnostics.domain.DtcCatalogSource
import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.diagnostics.domain.VehicleCatalogContext
import com.ecuforge.transport.fake.FakeTransportGateway
import com.ecuforge.transport.fake.FakeTransportOperation
import com.ecuforge.transport.fake.FakeTransportScenario
import com.ecuforge.transport.fake.FakeTransportStep
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TransportBackedDiagnosticsFlowProviderTest {
    @Test
    fun identifyReadOnlyDemoDelegatesToTransportUseCase() {
        runBlocking {
            val provider =
                TransportBackedDiagnosticsFlowProvider(
                    transportGatewayFactory = {
                        FakeTransportGateway(
                            scenario =
                                FakeTransportScenario.of(
                                    FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                                    FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
                                    FakeTransportStep(
                                        operation = FakeTransportOperation.READ,
                                        success = true,
                                        readPayload =
                                            "MODEL=KM601EU|FW=2.10.4|SN=A1B2C3"
                                                .encodeToByteArray(),
                                    ),
                                    FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                                ),
                        )
                    },
                    profile =
                        DiagnosticsReadOnlyProfile(
                            ecuFamily = "KEIHIN",
                            endpointHint = "BT",
                            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                        ),
                )

            val result = provider.identifyReadOnlyDemo()

            assertThat(result)
                .describedAs("Transport-backed provider should return identification success for valid read payload")
                .isInstanceOf(IdentificationUiState.Success::class.java)
            val success = result as IdentificationUiState.Success
            assertThat(success.identification.model)
                .describedAs("Identification success should expose parsed ECU model")
                .isEqualTo("KM601EU")
        }
    }

    @Test
    fun identifyTimeoutDemoReturnsScenarioUnavailableError() {
        runBlocking {
            val provider =
                TransportBackedDiagnosticsFlowProvider(
                    transportGatewayFactory = {
                        FakeTransportGateway(scenario = FakeTransportScenario.of())
                    },
                    profile =
                        DiagnosticsReadOnlyProfile(
                            ecuFamily = "KEIHIN",
                            endpointHint = "BT",
                            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                        ),
                )

            val result = provider.identifyReadOnlyTimeoutDemo()

            assertThat(result)
                .describedAs("Transport-backed provider should not simulate timeout demo scenarios")
                .isInstanceOf(IdentificationUiState.Error::class.java)
            val error = result as IdentificationUiState.Error
            assertThat(error.code)
                .describedAs("Timeout demo calls should return SCENARIO_UNAVAILABLE code")
                .isEqualTo("SCENARIO_UNAVAILABLE")
        }
    }

    @Test
    fun readDtcDemoWithCatalogContextAppliesCatalogDescription() {
        runBlocking {
            val provider =
                TransportBackedDiagnosticsFlowProvider(
                    transportGatewayFactory = {
                        FakeTransportGateway(
                            scenario =
                                FakeTransportScenario.of(
                                    FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
                                    FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
                                    FakeTransportStep(
                                        operation = FakeTransportOperation.READ,
                                        success = true,
                                        readPayload = "P0030;ECU DESCRIPTION".encodeToByteArray(),
                                    ),
                                    FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
                                ),
                        )
                    },
                    profile =
                        DiagnosticsReadOnlyProfile(
                            ecuFamily = "KEIHIN",
                            endpointHint = "BT",
                            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                        ),
                    dtcCatalogRepository =
                        StubDtcCatalogRepository(
                            descriptionByCode = mapOf("P0030" to "Catalog Description"),
                        ),
                )

            val result =
                provider.readDtcReadOnlyDemo(
                    vehicleCatalogContext =
                        VehicleCatalogContext(
                            make = "Triumph",
                            model = "Bonneville T120",
                            modelYear = 2018,
                        ),
                    preferCatalogDescriptions = true,
                )

            assertThat(result)
                .describedAs("Transport-backed DTC flow should return success for valid read payload")
                .isInstanceOf(DtcUiState.Success::class.java)
            val success = result as DtcUiState.Success
            assertThat(success.dtcs)
                .describedAs("Catalog-enabled request should replace ECU description for mapped DTC code")
                .hasSize(1)
            assertThat(success.dtcs.first().description)
                .describedAs("Mapped DTC should use catalog description when opt-in is enabled")
                .isEqualTo("Catalog Description")
        }
    }

    @Test
    fun readDtcTimeoutDemoReturnsScenarioUnavailableError() {
        runBlocking {
            val provider =
                TransportBackedDiagnosticsFlowProvider(
                    transportGatewayFactory = {
                        FakeTransportGateway(scenario = FakeTransportScenario.of())
                    },
                    profile =
                        DiagnosticsReadOnlyProfile(
                            ecuFamily = "KEIHIN",
                            endpointHint = "BT",
                            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
                        ),
                )

            val result = provider.readDtcReadOnlyTimeoutDemo()

            assertThat(result)
                .describedAs("Transport-backed provider should not simulate DTC timeout demo scenarios")
                .isInstanceOf(DtcUiState.Error::class.java)
            val error = result as DtcUiState.Error
            assertThat(error.code)
                .describedAs("Timeout demo calls should return SCENARIO_UNAVAILABLE code")
                .isEqualTo("SCENARIO_UNAVAILABLE")
        }
    }

    private class StubDtcCatalogRepository(
        descriptionByCode: Map<String, String>,
    ) : DtcCatalogRepository {
        private val dataset =
            DtcCatalogDataset(
                catalogId = "stub-catalog",
                version = "1.0.0",
                source =
                    DtcCatalogSource(
                        type = "test",
                        reference = "test",
                        receivedAt = "2026-03-18",
                    ),
                entries =
                    descriptionByCode.map { (code, description) ->
                        DtcCatalogEntry(
                            code = code,
                            platform = "test-platform",
                            yearFrom = 2016,
                            yearTo = 2019,
                            titleKey = "test.$code",
                            defaultDescription = description,
                        )
                    },
            )

        override fun loadCatalog(): DtcCatalogLoadResult {
            return DtcCatalogLoadResult.Success(dataset)
        }
    }
}
