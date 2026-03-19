package com.ecuforge.app

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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
    private var hasSuccessfulIdentification: Boolean = false

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
        applySystemBarInsets()
        setupTopAppBar()
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
     * Refreshes selected transport label after returning from settings screen.
     */
    override fun onResume() {
        super.onResume()
        val selectedTransport = transportProfileStore.loadSelectedTransport()
        binding.transportSelectorInput.setText(transportLabelFor(selectedTransport), false)
        renderActiveTransportSummary(selectedTransport)
    }

    /**
     * Runs read-only ECU identification in debug-enabled app builds.
     */
    private fun runReadOnlyIdentification() {
        val transportPermissionFailure = validateTransportPermissionPrecheck()
        if (transportPermissionFailure != null) {
            renderIdentificationState(
                IdentificationUiState.Error(
                    code = transportPermissionFailure.code,
                    message = transportPermissionFailure.message,
                ),
            )
            return
        }

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
        val dtcPrecheck = validateReadOnlyPrecheck(ReadOnlyFlowPreflightValidator.Action.DTC)
        if (dtcPrecheck != null) {
            renderDtcState(
                DtcUiState.Error(
                    code = dtcPrecheck.code,
                    message = dtcPrecheck.message,
                ),
            )
            return
        }

        val transportPermissionFailure = validateTransportPermissionPrecheck()
        if (transportPermissionFailure != null) {
            renderDtcState(
                DtcUiState.Error(
                    code = transportPermissionFailure.code,
                    message = transportPermissionFailure.message,
                ),
            )
            return
        }

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
                renderActionAvailability()
            }
        }
    }

    /**
     * Runs read-only telemetry snapshot retrieval in debug-enabled app builds.
     */
    private fun runReadOnlyTelemetry() {
        val telemetryPrecheck = validateReadOnlyPrecheck(ReadOnlyFlowPreflightValidator.Action.TELEMETRY)
        if (telemetryPrecheck != null) {
            renderTelemetryState(
                TelemetryUiState.Error(
                    code = telemetryPrecheck.code,
                    message = telemetryPrecheck.message,
                ),
            )
            return
        }

        val transportPermissionFailure = validateTransportPermissionPrecheck()
        if (transportPermissionFailure != null) {
            renderTelemetryState(
                TelemetryUiState.Error(
                    code = transportPermissionFailure.code,
                    message = transportPermissionFailure.message,
                ),
            )
            return
        }

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
                renderActionAvailability()
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
        hasSuccessfulIdentification = state is IdentificationUiState.Success
        renderActionAvailability()
    }

    /**
     * Updates read-only action buttons based on current identification availability rules.
     */
    private fun renderActionAvailability() {
        val availability =
            ReadOnlyActionAvailability.evaluate(
                state =
                    ReadOnlyActionAvailability.State(
                        hasSuccessfulIdentification = hasSuccessfulIdentification,
                    ),
            )

        binding.readDtcButton.isEnabled = availability.isReadDtcEnabled
        binding.readTelemetryButton.isEnabled = availability.isReadTelemetryEnabled
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
     * Applies status/navigation bar insets so content does not overlap system bars.
     */
    private fun applySystemBarInsets() {
        val initialTopPadding = binding.root.paddingTop
        val initialBottomPadding = binding.root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = initialTopPadding + systemBarsInsets.top,
                bottom = initialBottomPadding + systemBarsInsets.bottom,
            )
            insets
        }
    }

    /**
     * Sets up top app bar actions.
     */
    private fun setupTopAppBar() {
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_device_settings -> {
                    startActivity(Intent(this, DeviceSettingsActivity::class.java))
                    true
                }

                else -> false
            }
        }
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
        binding.transportSelectorInput.keyListener = null
        binding.transportSelectorInput.setOnClickListener {
            binding.transportSelectorInput.showDropDown()
        }
        binding.transportSelectorInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.transportSelectorInput.showDropDown()
            }
        }
        binding.transportSelectorInput.setOnItemClickListener { _, _, _, _ ->
            val selectedTransport =
                AppReadOnlyTransportMapper.map(
                    binding.transportSelectorInput.text?.toString().orEmpty(),
                )
            renderActiveTransportSummary(selectedTransport)
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
        renderActiveTransportSummary(selectedTransport)
    }

    /**
     * Applies current transport selector value to both diagnostics and telemetry providers.
     */
    private fun applyTransportConfiguration(): Boolean {
        val selectedTransport = selectedTransport()

        val profile = transportProfileStore.load(selectedTransport)
        transportProfileStore.save(profile)

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

    /**
     * Validates runtime transport permissions and requests missing ones.
     */
    private fun validateTransportPermissionPrecheck(): PrecheckFailure? {
        val permissionResult =
            ReadOnlyTransportPermissionPolicy.evaluate(
                state =
                    ReadOnlyTransportPermissionPolicy.State(
                        selectedTransport = selectedTransport(),
                        sdkInt = Build.VERSION.SDK_INT,
                        hasBluetoothConnectPermission = hasPermission(Manifest.permission.BLUETOOTH_CONNECT),
                        hasBluetoothScanPermission = hasPermission(Manifest.permission.BLUETOOTH_SCAN),
                        hasFineLocationPermission = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
                    ),
            )

        if (permissionResult.isGranted) {
            return null
        }

        ActivityCompat.requestPermissions(
            this,
            permissionResult.missingPermissions.toTypedArray(),
            REQUEST_CODE_READ_ONLY_TRANSPORT_PERMISSIONS,
        )
        val permissionLabelText = permissionLabelText(permissionResult.missingPermissions)
        return PrecheckFailure(
            code = ReadOnlyTransportPermissionPolicy.PRECHECK_TRANSPORT_PERMISSION_REQUIRED,
            message = getString(R.string.precheck_transport_permission_required, permissionLabelText),
        )
    }

    private fun selectedTransport(): AppReadOnlyTransport {
        return AppReadOnlyTransportMapper.map(
            rawSelection = binding.transportSelectorInput.text?.toString().orEmpty(),
        )
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun permissionLabelText(missingPermissions: List<String>): String {
        val labels =
            missingPermissions
                .mapNotNull { permission ->
                    when (permission) {
                        ReadOnlyTransportPermissionPolicy.PERMISSION_BLUETOOTH_CONNECT,
                        ReadOnlyTransportPermissionPolicy.PERMISSION_BLUETOOTH_SCAN,
                        -> getString(R.string.permission_label_nearby_devices)

                        ReadOnlyTransportPermissionPolicy.PERMISSION_ACCESS_FINE_LOCATION ->
                            getString(R.string.permission_label_location)

                        else -> null
                    }
                }.distinct()
        return labels.joinToString(separator = ", ")
    }

    /**
     * Renders a concise summary of the active saved transport profile.
     */
    private fun renderActiveTransportSummary(selectedTransport: AppReadOnlyTransport) {
        val profile = transportProfileStore.load(selectedTransport)
        val summaryText =
            when (profile) {
                is AppTransportProfile.Bluetooth ->
                    getString(
                        R.string.active_transport_summary_bluetooth,
                        profile.macAddress,
                    )

                is AppTransportProfile.Usb ->
                    getString(
                        R.string.active_transport_summary_usb,
                        profile.vendorId,
                        profile.productId,
                    )

                is AppTransportProfile.Wifi ->
                    getString(
                        R.string.active_transport_summary_wifi,
                        profile.host,
                        profile.port,
                    )
            }

        binding.activeTransportSummaryText.text = summaryText
    }

    private fun transportLabelFor(transport: AppReadOnlyTransport): String {
        return when (transport) {
            AppReadOnlyTransport.BLUETOOTH -> getString(R.string.transport_option_bluetooth)
            AppReadOnlyTransport.USB -> getString(R.string.transport_option_usb)
            AppReadOnlyTransport.WIFI -> getString(R.string.transport_option_wifi)
        }
    }

    /**
     * Validates preconditions before read-only actions and maps deterministic failures.
     */
    private fun validateReadOnlyPrecheck(action: ReadOnlyFlowPreflightValidator.Action): PrecheckFailure? {
        val result =
            ReadOnlyFlowPreflightValidator.validate(
                request =
                    ReadOnlyFlowPreflightValidator.Request(
                        action = action,
                        hasSuccessfulIdentification = hasSuccessfulIdentification,
                        catalogOptIn = binding.useCatalogDescriptionsCheckbox.isChecked,
                        vehicleMake = binding.vehicleMakeInput.text?.toString().orEmpty(),
                        vehicleModel = binding.vehicleModelInput.text?.toString().orEmpty(),
                    ),
            )

        return if (result is ReadOnlyFlowPreflightValidator.Result.Failure) {
            PrecheckFailure(
                code = result.code,
                message = precheckMessage(result.code),
            )
        } else {
            null
        }
    }

    /**
     * Maps stable precheck failure code to user-visible message.
     */
    private fun precheckMessage(code: String): String {
        return when (code) {
            ReadOnlyFlowPreflightValidator.PRECHECK_IDENTIFICATION_REQUIRED ->
                getString(R.string.precheck_identification_required)

            ReadOnlyFlowPreflightValidator.PRECHECK_VEHICLE_SELECTION_REQUIRED ->
                getString(R.string.precheck_vehicle_selection_required)

            else -> getString(R.string.precheck_unknown_failure)
        }
    }

    private data class PrecheckFailure(
        val code: String,
        val message: String,
    )

    private companion object {
        private const val REQUEST_CODE_READ_ONLY_TRANSPORT_PERMISSIONS: Int = 1001
    }
}
