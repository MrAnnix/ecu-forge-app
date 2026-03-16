package com.ecuforge.app

import com.ecuforge.feature.diagnostics.domain.EcuIdentification
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun formatterReturnsLoadingMessage() {
        val text = IdentificationStatusFormatter.format(IdentificationUiState.Loading)

        assertEquals("Reading ECU identification...", text)
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

        assertTrue(text.contains("KM601EU"))
        assertTrue(text.contains("2.10.4"))
        assertTrue(text.contains("A1B2C3"))
    }

    @Test
    fun formatterReturnsErrorMessage() {
        val state =
            IdentificationUiState.Error(
                code = "TIMEOUT",
                message = "Read timeout",
            )

        val text = IdentificationStatusFormatter.format(state)

        assertEquals("Identification failed (TIMEOUT): Read timeout", text)
    }
}
