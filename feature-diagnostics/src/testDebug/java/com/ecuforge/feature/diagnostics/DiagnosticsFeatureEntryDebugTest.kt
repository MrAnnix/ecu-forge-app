package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsFeatureEntryDebugTest {
    @Test
    fun readOnlyDemoReturnsSuccess() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.identifyReadOnlyDemo()

            assertTrue(result is IdentificationUiState.Success)
        }

    @Test
    fun timeoutDemoReturnsTimeoutError() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.identifyReadOnlyTimeoutDemo()

            assertTrue(result is IdentificationUiState.Error)
            val error = result as IdentificationUiState.Error
            assertEquals("TIMEOUT", error.code)
        }

    @Test
    fun readDtcDemoReturnsSuccess() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.readDtcReadOnlyDemo()

            assertTrue(result is DtcUiState.Success)
            val success = result as DtcUiState.Success
            assertTrue(success.dtcs.isNotEmpty())
        }

    @Test
    fun readDtcTimeoutDemoReturnsTimeoutError() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.readDtcReadOnlyTimeoutDemo()

            assertTrue(result is DtcUiState.Error)
            val error = result as DtcUiState.Error
            assertEquals("TIMEOUT", error.code)
        }
}
