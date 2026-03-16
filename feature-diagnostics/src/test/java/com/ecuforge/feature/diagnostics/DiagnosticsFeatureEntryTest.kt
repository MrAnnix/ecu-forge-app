package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsFeatureEntryTest {
    @Test
    fun readOnlyDemoReturnsSupportedStateForActiveVariant() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.identifyReadOnlyDemo()

            val isExpectedDebugState = result is IdentificationUiState.Success
            val isExpectedReleaseState = result is IdentificationUiState.Error && result.code == "DEMO_DISABLED"
            assertTrue(isExpectedDebugState || isExpectedReleaseState)
        }

    @Test
    fun timeoutDemoReturnsSupportedStateForActiveVariant() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.identifyReadOnlyTimeoutDemo()

            val isExpectedDebugState = result is IdentificationUiState.Error && result.code == "TIMEOUT"
            val isExpectedReleaseState = result is IdentificationUiState.Error && result.code == "DEMO_DISABLED"
            assertTrue(isExpectedDebugState || isExpectedReleaseState)
        }

    @Test
    fun readDtcDemoReturnsSupportedStateForActiveVariant() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.readDtcReadOnlyDemo()

            val isExpectedDebugState = result is DtcUiState.Success
            val isExpectedReleaseState = result is DtcUiState.Error && result.code == "DEMO_DISABLED"
            assertTrue(isExpectedDebugState || isExpectedReleaseState)
        }

    @Test
    fun readDtcTimeoutDemoReturnsSupportedStateForActiveVariant() =
        runBlocking {
            val result = DiagnosticsFeatureEntry.readDtcReadOnlyTimeoutDemo()

            val isExpectedDebugState = result is DtcUiState.Error && result.code == "TIMEOUT"
            val isExpectedReleaseState = result is DtcUiState.Error && result.code == "DEMO_DISABLED"
            assertTrue(isExpectedDebugState || isExpectedReleaseState)
        }
}
