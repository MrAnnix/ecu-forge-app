package com.ecuforge.app

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ecuforge.app.databinding.ActivityMainBinding
import com.ecuforge.feature.diagnostics.DiagnosticsFeatureEntry
import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.telemetry.TelemetryFeatureEntry
import com.ecuforge.feature.telemetry.domain.PersistTelemetryExportResult
import com.ecuforge.feature.telemetry.domain.PersistTelemetryExportUseCase
import com.ecuforge.feature.telemetry.domain.TelemetryUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Host activity for the initial diagnostics read-only demo flow.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val screenScope = MainScope()
    private lateinit var exportUseCase: PersistTelemetryExportUseCase
    private var latestTelemetrySuccessState: TelemetryUiState.Success? = null

    /**
     * Initializes the view binding and registers UI actions.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        exportUseCase =
            PersistTelemetryExportUseCase(
                exportStore = AppTelemetryExportStore(File(filesDir, "telemetry-exports")),
            )

        renderIdentificationState(IdentificationUiState.Idle)
        renderDtcState(DtcUiState.Idle)
        renderTelemetryState(TelemetryUiState.Idle)
        binding.identifyButton.setOnClickListener {
            runReadOnlyIdentification()
        }
        binding.readDtcButton.setOnClickListener {
            runReadOnlyDtc()
        }
        binding.readTelemetryButton.setOnClickListener {
            runReadOnlyTelemetry()
        }
        binding.exportTelemetryButton.setOnClickListener {
            runTelemetryExport()
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

        val selection =
            DtcCatalogSelectionMapper.map(
                makeInput = binding.vehicleMakeInput.text?.toString().orEmpty(),
                modelInput = binding.vehicleModelInput.text?.toString().orEmpty(),
                yearInput = binding.vehicleYearInput.text?.toString().orEmpty(),
                catalogOptIn = binding.useCatalogDescriptionsCheckbox.isChecked,
            )

        screenScope.launch {
            try {
                val result =
                    withContext(Dispatchers.IO) {
                        DiagnosticsFeatureEntry.readDtcReadOnlyDemo(
                            vehicleCatalogContext = selection.vehicleCatalogContext,
                            preferCatalogDescriptions = selection.preferCatalogDescriptions,
                        )
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
     * Runs read-only telemetry snapshot retrieval in debug-enabled app builds.
     */
    private fun runReadOnlyTelemetry() {
        val isDebuggableBuild = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebuggableBuild) {
            renderTelemetryState(
                TelemetryUiState.Error(
                    code = "DEMO_DISABLED",
                    message = "Demo telemetry flow is only available in debug builds",
                ),
            )
            return
        }

        binding.readTelemetryButton.isEnabled = false
        renderTelemetryState(TelemetryUiState.Loading)

        screenScope.launch {
            try {
                val result =
                    withContext(Dispatchers.IO) {
                        TelemetryFeatureEntry.readTelemetryReadOnlyDemo()
                    }
                renderTelemetryState(result)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                renderTelemetryState(
                    TelemetryUiState.Error(
                        code = "UNEXPECTED",
                        message = exception.message ?: "Unexpected application error",
                    ),
                )
            } finally {
                binding.readTelemetryButton.isEnabled = true
            }
        }
    }

    /**
     * Persists the latest successful telemetry snapshot as a deterministic export artifact.
     */
    private fun runTelemetryExport() {
        val successState = latestTelemetrySuccessState
        if (successState == null) {
            renderTelemetryState(
                TelemetryUiState.Error(
                    code = "EXPORT_INVALID_STATE",
                    message = "Telemetry export requires a successful telemetry snapshot",
                ),
            )
            return
        }

        binding.exportTelemetryButton.isEnabled = false

        screenScope.launch {
            try {
                val result =
                    withContext(Dispatchers.IO) {
                        exportUseCase.execute(successState)
                    }

                when (result) {
                    is PersistTelemetryExportResult.Failure -> {
                        renderTelemetryState(
                            TelemetryUiState.Error(
                                code = result.code,
                                message = result.message,
                            ),
                        )
                    }

                    is PersistTelemetryExportResult.Success -> {
                        val warningSuffix =
                            result.receipt.warning?.let { warning -> " Warning: $warning" }.orEmpty()
                        binding.telemetryStatusText.text =
                            "Telemetry export saved (${result.receipt.exportId})." + warningSuffix
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                renderTelemetryState(
                    TelemetryUiState.Error(
                        code = "EXPORT_UNEXPECTED",
                        message = exception.message ?: "Unexpected export error",
                    ),
                )
            } finally {
                binding.exportTelemetryButton.isEnabled = latestTelemetrySuccessState != null
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

    /**
     * Renders telemetry state into the telemetry status text view.
     */
    private fun renderTelemetryState(state: TelemetryUiState) {
        binding.telemetryStatusText.text = TelemetryStatusFormatter.format(state)
        latestTelemetrySuccessState = state as? TelemetryUiState.Success
        binding.exportTelemetryButton.isEnabled = latestTelemetrySuccessState != null
    }
}
