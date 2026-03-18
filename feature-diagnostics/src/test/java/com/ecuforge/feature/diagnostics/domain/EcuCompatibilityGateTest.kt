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
    fun modelTransportSupportRejectsInferredEvidenceEntries() {
        val supported =
            gate.isModelSupportedForTransport(
                family = "KEIHIN",
                model = "KM602EU",
                endpointHint = "BT",
            )

        assertThat(supported)
            .describedAs("Transport-aware compatibility should reject non-validated inferred evidence entries")
            .isFalse()
    }
}
