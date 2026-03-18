package com.ecuforge.app

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ecuforge.app.databinding.ActivityMainBinding
import com.ecuforge.feature.diagnostics.DiagnosticsFeatureEntry
import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Host activity for the initial diagnostics read-only demo flow.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val screenScope = MainScope()

    /**
     * Initializes the view binding and registers UI actions.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        renderIdentificationState(IdentificationUiState.Idle)
        renderDtcState(DtcUiState.Idle)
        binding.identifyButton.setOnClickListener {
            runReadOnlyIdentification()
        }
        binding.readDtcButton.setOnClickListener {
            runReadOnlyDtc()
        }
    }

    /**
     * Cancels screen scope jobs when activity is destroyed.
     */
    override fun onDestroy() {
        screenScope.cancel()
        super.onDestroy()
    }

    /**
     * Runs read-only ECU identification in debug-enabled app builds.
     */
    private fun runReadOnlyIdentification() {
        val isDebuggableBuild = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebuggableBuild) {
            renderIdentificationState(
                IdentificationUiState.Error(
                    code = "DEMO_DISABLED",
                    message = "Demo identification is only available in debug builds",
                ),
            )
            return
        }

        binding.identifyButton.isEnabled = false
        renderIdentificationState(IdentificationUiState.Loading)

        screenScope.launch {
            try {
                val result =
                    withContext(Dispatchers.IO) {
                        DiagnosticsFeatureEntry.identifyReadOnlyDemo()
                    }
                renderIdentificationState(result)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                renderIdentificationState(
                    IdentificationUiState.Error(
                        code = "UNEXPECTED",
                        message = exception.message ?: "Unexpected application error",
                    ),
                )
            } finally {
                binding.identifyButton.isEnabled = true
            }
        }
    }

    /**
     * Runs read-only DTC retrieval in debug-enabled app builds.
     */
    private fun runReadOnlyDtc() {
        val isDebuggableBuild = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebuggableBuild) {
            renderDtcState(
                DtcUiState.Error(
                    code = "DEMO_DISABLED",
                    message = "Demo DTC flow is only available in debug builds",
                ),
            )
            return
        }

        binding.readDtcButton.isEnabled = false
        renderDtcState(DtcUiState.Loading)

        screenScope.launch {
            try {
                val result =
                    withContext(Dispatchers.IO) {
                        DiagnosticsFeatureEntry.readDtcReadOnlyDemo()
                    }
                renderDtcState(result)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                renderDtcState(
                    DtcUiState.Error(
                        code = "UNEXPECTED",
                        message = exception.message ?: "Unexpected application error",
                    ),
                )
            } finally {
                binding.readDtcButton.isEnabled = true
            }
        }
    }

    /**
     * Renders identification state into the identification status text view.
     */
    private fun renderIdentificationState(state: IdentificationUiState) {
        binding.statusText.text = IdentificationStatusFormatter.format(state)
    }

    /**
     * Renders DTC state into the DTC status text view.
     */
    private fun renderDtcState(state: DtcUiState) {
        binding.dtcStatusText.text = DtcStatusFormatter.format(state)
    }
}
