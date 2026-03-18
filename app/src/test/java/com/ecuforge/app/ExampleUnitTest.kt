package com.ecuforge.app

import com.ecuforge.feature.diagnostics.domain.EcuIdentification
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun formatterReturnsLoadingMessage() {
        val text = IdentificationStatusFormatter.format(IdentificationUiState.Loading)

        assertThat(text)
            .describedAs("Loading state should render the expected identification progress message")
            .isEqualTo("Reading ECU identification...")
    }

    @Test
    fun formatterReturnsSuccessDetails() {
        val state =
            IdentificationUiState.Success(
                identification =
                    EcuIdentification(
                        model = "KM601EU",
                        firmwareVersion = "2.10.4",
                        serialNumber = "A1B2C3",
                    ),
            )

        val text = IdentificationStatusFormatter.format(state)

        assertThat(text)
            .describedAs("Success state text should include ECU model")
            .contains("KM601EU")
        assertThat(text)
            .describedAs("Success state text should include ECU firmware version")
            .contains("2.10.4")
        assertThat(text)
            .describedAs("Success state text should include ECU serial number")
            .contains("A1B2C3")
    }

    @Test
    fun formatterReturnsErrorMessage() {
        val state =
            IdentificationUiState.Error(
                code = "TIMEOUT",
                message = "Read timeout",
            )

        val text = IdentificationStatusFormatter.format(state)

        assertThat(text)
            .describedAs("Error state should include stable error code and human-readable message")
            .isEqualTo("Identification failed (TIMEOUT): Read timeout")
    }
}
