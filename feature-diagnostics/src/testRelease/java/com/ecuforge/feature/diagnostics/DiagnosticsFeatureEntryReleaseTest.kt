package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsFeatureEntryReleaseTest {
    @Test
    fun readOnlyDemoIsDisabledInRelease() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.identifyReadOnlyDemo()

            assertTrue(result is IdentificationUiState.Error)
            val error = result as IdentificationUiState.Error
            assertEquals("DEMO_DISABLED", error.code)
        }

    @Test
    fun timeoutDemoIsDisabledInRelease() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.identifyReadOnlyTimeoutDemo()

            assertTrue(result is IdentificationUiState.Error)
            val error = result as IdentificationUiState.Error
            assertEquals("DEMO_DISABLED", error.code)
        }

    @Test
    fun readDtcDemoIsDisabledInRelease() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.readDtcReadOnlyDemo()

            assertTrue(result is DtcUiState.Error)
            val error = result as DtcUiState.Error
            assertEquals("DEMO_DISABLED", error.code)
        }

    @Test
    fun readDtcTimeoutDemoIsDisabledInRelease() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.readDtcReadOnlyTimeoutDemo()

            assertTrue(result is DtcUiState.Error)
            val error = result as DtcUiState.Error
            assertEquals("DEMO_DISABLED", error.code)
        }
}
