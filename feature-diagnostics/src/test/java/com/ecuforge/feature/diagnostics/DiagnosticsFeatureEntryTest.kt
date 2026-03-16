package com.ecuforge.feature.diagnostics

import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsFeatureEntryTest {

	@Test
	fun readOnlyDemoReturnsSuccess() = runBlocking {
		val result = DiagnosticsFeatureEntry.identifyReadOnlyDemo()

		assertTrue(result is IdentificationUiState.Success)
	}

	@Test
	fun timeoutDemoReturnsTimeoutError() = runBlocking {
		val result = DiagnosticsFeatureEntry.identifyReadOnlyTimeoutDemo()

		assertTrue(result is IdentificationUiState.Error)
		val error = result as IdentificationUiState.Error
		assertEquals("TIMEOUT", error.code)
	}
}

