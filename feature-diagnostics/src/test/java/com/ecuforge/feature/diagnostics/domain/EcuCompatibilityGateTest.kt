package com.ecuforge.feature.diagnostics.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EcuCompatibilityGateTest {
    private val gate = EcuCompatibilityGate()

    @Test
    fun familySupportRemainsCaseInsensitiveForBaselineFlow() {
        val supported = gate.isSupported(IdentificationRequest(ecuFamily = "keihin", endpointHint = "BT"))

        assertThat(supported)
            .describedAs("Family-level compatibility gate should keep case-insensitive support checks")
            .isTrue()
    }

    @Test
    fun modelSupportReturnsTrueForValidatedModelEvidence() {
        val supported = gate.isModelSupported(family = "KEIHIN", model = "KM601EU")

        assertThat(supported)
            .describedAs("Model-level compatibility evidence should include validated KEIHIN model KM601EU")
            .isTrue()
    }

    @Test
    fun modelSupportIsCaseInsensitiveAndTrimAware() {
        val supported = gate.isModelSupported(family = " keihin ", model = " km602eu ")

        assertThat(supported)
            .describedAs("Model-level compatibility check should normalize casing and surrounding spaces")
            .isTrue()
    }

    @Test
    fun modelSupportReturnsFalseForUnknownModelInKnownFamily() {
        val supported = gate.isModelSupported(family = "KEIHIN", model = "KM999ZZ")

        assertThat(supported)
            .describedAs("Unknown model should remain unsupported even when family exists")
            .isFalse()
    }

    @Test
    fun modelSupportReturnsFalseForBlankInputs() {
        val supportedFamilyBlank = gate.isModelSupported(family = "", model = "KM601EU")
        val supportedModelBlank = gate.isModelSupported(family = "KEIHIN", model = "")

        assertThat(supportedFamilyBlank)
            .describedAs("Blank family should be rejected by model-level compatibility check")
            .isFalse()
        assertThat(supportedModelBlank)
            .describedAs("Blank model should be rejected by model-level compatibility check")
            .isFalse()
    }

    @Test
    fun modelTransportSupportReturnsTrueForValidatedBluetoothEvidence() {
        val supported =
            gate.isModelSupportedForTransport(
                family = "KEIHIN",
                model = "KM601EU",
                endpointHint = "BT",
            )

        assertThat(supported)
            .describedAs("Transport-aware compatibility should accept KEIHIN KM601EU over validated Bluetooth evidence")
            .isTrue()
    }

    @Test
    fun modelTransportSupportReturnsTrueForValidatedUsbEvidence() {
        val supported =
            gate.isModelSupportedForTransport(
                family = "KEIHIN",
                model = "KM601EU",
                endpointHint = "USB",
            )

        assertThat(supported)
            .describedAs("Transport-aware compatibility should accept KEIHIN KM601EU over validated USB evidence")
            .isTrue()
    }

    @Test
    fun modelTransportSupportReturnsTrueForPromotedValidatedBluetoothEvidence() {
        val supported =
            gate.isModelSupportedForTransport(
                family = "KEIHIN",
                model = "KM602EU",
                endpointHint = "BT",
            )

        assertThat(supported)
            .describedAs("Transport-aware compatibility should accept promoted validated KEIHIN KM602EU over Bluetooth")
            .isTrue()
    }

    @Test
    fun modelTransportSupportRejectsRemainingInferredEvidenceEntries() {
        val supported =
            gate.isModelSupportedForTransport(
                family = "SIEMENS",
                model = "SIE-ECU-01",
                endpointHint = "BT",
            )

        assertThat(supported)
            .describedAs("Transport-aware compatibility should reject tuples that remain inferred")
            .isFalse()
    }

    @Test
    fun modelTransportSupportReturnsFalseForUnknownTransportHint() {
        val supported =
            gate.isModelSupportedForTransport(
                family = "KEIHIN",
                model = "KM601EU",
                endpointHint = "WIFI",
            )

        assertThat(supported)
            .describedAs("Transport-aware compatibility should reject WiFi transport until validated WiFi evidence exists")
            .isFalse()
    }

    @Test
    fun modelTransportSupportAcceptsWifiTransportHintAliasesWhenValidatedEvidenceExists() {
        val wifiGate =
            EcuCompatibilityGate(
                supportedModelTransports =
                    setOf(
                        ModelTransportSupportKey(
                            family = "KEIHIN",
                            model = "KM601EU",
                            transport = "WIFI",
                        ),
                    ),
            )

        val supportedFromWifiHint =
            wifiGate.isModelSupportedForTransport(
                family = "KEIHIN",
                model = "KM601EU",
                endpointHint = "WIFI",
            )
        val supportedFromWiFiAlias =
            wifiGate.isModelSupportedForTransport(
                family = "KEIHIN",
                model = "KM601EU",
                endpointHint = "wi-fi",
            )

        assertThat(supportedFromWifiHint)
            .describedAs("Transport-aware compatibility should normalize WIFI hint when validated evidence exists")
            .isTrue()
        assertThat(supportedFromWiFiAlias)
            .describedAs("Transport-aware compatibility should normalize WI-FI alias when validated evidence exists")
            .isTrue()
    }
}
