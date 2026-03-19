package com.ecuforge.app

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.ArrayAdapter
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
    private lateinit var transportProfileStore: AppTransportProfileStore
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
        transportProfileStore = AppTransportProfileStore(this)

        renderIdentificationState(IdentificationUiState.Idle)
        renderDtcState(DtcUiState.Idle)
        renderTelemetryState(TelemetryUiState.Idle)
        setupSelectorDropdowns()

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
        if (!applyTransportConfiguration()) {
            return
        }

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
        if (!applyTransportConfiguration()) {
            return
        }

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
        if (!applyTransportConfiguration()) {
            return
        }

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
                            getString(
                                R.string.telemetry_export_saved,
                                result.receipt.exportId,
                                warningSuffix,
                            )
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

    /**
     * Sets up searchable dropdowns for transport and vehicle catalog context fields.
     */
    private fun setupSelectorDropdowns() {
        val transportOptions = resources.getStringArray(R.array.transport_selector_options)
        val makeOptions = resources.getStringArray(R.array.vehicle_make_options)
        val modelOptions = resources.getStringArray(R.array.vehicle_model_options)
        val yearOptions = resources.getStringArray(R.array.vehicle_year_options)

        binding.transportSelectorInput.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                transportOptions,
            ),
        )
        binding.transportSelectorInput.setOnItemClickListener { _, _, _, _ ->
            val selectedTransport =
                AppReadOnlyTransportMapper.map(
                    binding.transportSelectorInput.text?.toString().orEmpty(),
                )
            bindTransportProfileToInputs(transportProfileStore.load(selectedTransport))
        }

        binding.vehicleMakeInput.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                makeOptions,
            ),
        )
        binding.vehicleModelInput.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                modelOptions,
            ),
        )
        binding.vehicleYearInput.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                yearOptions,
            ),
        )

        val selectedTransport = transportProfileStore.loadSelectedTransport()
        binding.transportSelectorInput.setText(
            transportLabelFor(selectedTransport),
            false,
        )
        bindTransportProfileToInputs(transportProfileStore.load(selectedTransport))
    }

    /**
     * Applies current transport selector value to both diagnostics and telemetry providers.
     */
    private fun applyTransportConfiguration(): Boolean {
        val selectedTransport =
            AppReadOnlyTransportMapper.map(
                rawSelection = binding.transportSelectorInput.text?.toString().orEmpty(),
            )

        val profileBuildResult =
            AppTransportProfileFactory.build(
                transport = selectedTransport,
                primaryValue = binding.transportPrimaryInput.text?.toString().orEmpty(),
                secondaryValue = binding.transportSecondaryInput.text?.toString().orEmpty(),
                connectTimeoutValue = binding.connectTimeoutInput.text?.toString().orEmpty(),
                readTimeoutValue = binding.readTimeoutInput.text?.toString().orEmpty(),
            )

        if (!profileBuildResult.validation.isValid || profileBuildResult.profile == null) {
            val errorMessage = profileBuildResult.validation.errors.joinToString(separator = "\n")
            renderIdentificationState(
                IdentificationUiState.Error(
                    code = "TRANSPORT_CONFIG_INVALID",
                    message = errorMessage,
                ),
            )
            renderDtcState(
                DtcUiState.Error(
                    code = "TRANSPORT_CONFIG_INVALID",
                    message = errorMessage,
                ),
            )
            renderTelemetryState(
                TelemetryUiState.Error(
                    code = "TRANSPORT_CONFIG_INVALID",
                    message = errorMessage,
                ),
            )
            return false
        }

        val profile = profileBuildResult.profile
        transportProfileStore.save(profile)
        bindTransportFieldHints(profile.transport)

        DiagnosticsFeatureEntry.configureReadOnlyTransport(profile.transport.toDiagnosticsTransport())
        TelemetryFeatureEntry.configureReadOnlyTransport(profile.transport.toTelemetryTransport())
        DiagnosticsFeatureEntry.configureReadOnlyConnectionSettings(
            profile.toDiagnosticsConnectionSettings(),
        )
        TelemetryFeatureEntry.configureReadOnlyConnectionSettings(
            profile.toTelemetryConnectionSettings(),
        )
        return true
    }

    private fun bindTransportProfileToInputs(profile: AppTransportProfile) {
        bindTransportFieldHints(profile.transport)
        binding.connectTimeoutInput.setText(profile.connectTimeoutMs.toString())
        binding.readTimeoutInput.setText(profile.readTimeoutMs.toString())

        when (profile) {
            is AppTransportProfile.Bluetooth -> {
                binding.transportPrimaryInput.setText(profile.macAddress)
                binding.transportSecondaryInput.setText("")
            }

            is AppTransportProfile.Usb -> {
                binding.transportPrimaryInput.setText(profile.vendorId.toString())
                binding.transportSecondaryInput.setText(profile.productId.toString())
            }

            is AppTransportProfile.Wifi -> {
                binding.transportPrimaryInput.setText(profile.host)
                binding.transportSecondaryInput.setText(profile.port.toString())
            }
        }
    }

    private fun bindTransportFieldHints(transport: AppReadOnlyTransport) {
        when (transport) {
            AppReadOnlyTransport.BLUETOOTH -> {
                binding.transportPrimaryInputLayout.hint = getString(R.string.transport_primary_hint_bluetooth)
                binding.transportSecondaryInputLayout.hint = getString(R.string.transport_secondary_label)
                binding.transportSecondaryInputLayout.isEnabled = false
                binding.transportSecondaryInputLayout.isHintEnabled = false
                binding.transportSecondaryInputLayout.visibility = android.view.View.GONE
            }

            AppReadOnlyTransport.USB -> {
                binding.transportPrimaryInputLayout.hint = getString(R.string.transport_primary_hint_usb)
                binding.transportSecondaryInputLayout.hint = getString(R.string.transport_secondary_hint_usb)
                binding.transportSecondaryInputLayout.isEnabled = true
                binding.transportSecondaryInputLayout.isHintEnabled = true
                binding.transportSecondaryInputLayout.visibility = android.view.View.VISIBLE
            }

            AppReadOnlyTransport.WIFI -> {
                binding.transportPrimaryInputLayout.hint = getString(R.string.transport_primary_hint_wifi)
                binding.transportSecondaryInputLayout.hint = getString(R.string.transport_secondary_hint_wifi)
                binding.transportSecondaryInputLayout.isEnabled = true
                binding.transportSecondaryInputLayout.isHintEnabled = true
                binding.transportSecondaryInputLayout.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun transportLabelFor(transport: AppReadOnlyTransport): String {
        return when (transport) {
            AppReadOnlyTransport.BLUETOOTH -> getString(R.string.transport_option_bluetooth)
            AppReadOnlyTransport.USB -> getString(R.string.transport_option_usb)
            AppReadOnlyTransport.WIFI -> getString(R.string.transport_option_wifi)
        }
    }
}
