package com.ecuforge.app

import com.ecuforge.feature.diagnostics.domain.DtcRecord
import com.ecuforge.feature.diagnostics.domain.DtcUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DtcStatusFormatterTest {
    @Test
    fun formatterReturnsLoadingMessage() {
        val text = DtcStatusFormatter.format(DtcUiState.Loading)

        assertEquals("Reading diagnostic trouble codes...", text)
    }

    @Test
    fun formatterReturnsEmptyMessageWhenNoCodes() {
        val state = DtcUiState.Success(dtcs = emptyList())

        val text = DtcStatusFormatter.format(state)

        assertEquals("No active DTC codes reported by ECU.", text)
    }

    @Test
    fun formatterReturnsDetailsForCodes() {
        val state =
            DtcUiState.Success(
                dtcs =
                    listOf(
                        DtcRecord(code = "P0130", description = "O2 Sensor Circuit"),
                        DtcRecord(code = "P0301", description = "Cylinder 1 Misfire"),
                    ),
            )

        val text = DtcStatusFormatter.format(state)

        assertTrue(text.contains("P0130"))
        assertTrue(text.contains("O2 Sensor Circuit"))
        assertTrue(text.contains("P0301"))
    }

    @Test
    fun formatterReturnsErrorMessage() {
        val state = DtcUiState.Error(code = "TIMEOUT", message = "Read timeout")

        val text = DtcStatusFormatter.format(state)

        assertEquals("DTC retrieval failed (TIMEOUT): Read timeout", text)
    }
}
