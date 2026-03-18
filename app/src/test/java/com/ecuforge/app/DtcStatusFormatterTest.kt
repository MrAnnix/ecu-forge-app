package com.ecuforge.app

import com.ecuforge.feature.diagnostics.domain.DtcRecord
import com.ecuforge.feature.diagnostics.domain.DtcUiState
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DtcStatusFormatterTest {
    @Test
    fun formatterReturnsLoadingMessage() {
        val text = DtcStatusFormatter.format(DtcUiState.Loading)

        assertThat(text)
            .describedAs("Loading DTC state should render a progress message")
            .isEqualTo("Reading diagnostic trouble codes...")
    }

    @Test
    fun formatterReturnsEmptyMessageWhenNoCodes() {
        val state = DtcUiState.Success(dtcs = emptyList())

        val text = DtcStatusFormatter.format(state)

        assertThat(text)
            .describedAs("Successful DTC response with empty list should explain that no active codes exist")
            .isEqualTo("No active DTC codes reported by ECU.")
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

        assertThat(text)
            .describedAs("Rendered DTC message should include first DTC code")
            .contains("P0130")
        assertThat(text)
            .describedAs("Rendered DTC message should include first DTC description")
            .contains("O2 Sensor Circuit")
        assertThat(text)
            .describedAs("Rendered DTC message should include second DTC code")
            .contains("P0301")
    }

    @Test
    fun formatterReturnsErrorMessage() {
        val state = DtcUiState.Error(code = "TIMEOUT", message = "Read timeout")

        val text = DtcStatusFormatter.format(state)

        assertThat(text)
            .describedAs("Error DTC state should include error code and reason")
            .isEqualTo("DTC retrieval failed (TIMEOUT): Read timeout")
    }
}
