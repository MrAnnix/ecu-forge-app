package com.ecuforge.app

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.ecuforge.app.databinding.ActivityDeviceSettingsBinding

/**
 * Dedicated screen for transport-specific read-only device configuration.
 */
class DeviceSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceSettingsBinding
    private lateinit var transportProfileStore: AppTransportProfileStore

    /**
     * Initializes settings form and loads persisted profile values.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        transportProfileStore = AppTransportProfileStore(this)

        applySystemBarInsets()
        setupTopAppBar()
        setupTransportSelector()
        loadPersistedSelection()

        binding.saveButton.setOnClickListener {
            saveProfile()
        }
    }

    /**
     * Applies status/navigation insets so settings content avoids system bars.
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
     * Configures toolbar navigation action.
     */
    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Configures transport dropdown behavior.
     */
    private fun setupTransportSelector() {
        val transportOptions = resources.getStringArray(R.array.transport_selector_options)
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
            bindProfileToInputs(transportProfileStore.load(selectedTransport))
        }
    }

    /**
     * Loads the last selected transport profile into settings inputs.
     */
    private fun loadPersistedSelection() {
        val selectedTransport = transportProfileStore.loadSelectedTransport()
        binding.transportSelectorInput.setText(transportLabelFor(selectedTransport), false)
        bindProfileToInputs(transportProfileStore.load(selectedTransport))
    }

    /**
     * Saves current settings form values as transport profile when validation passes.
     */
    private fun saveProfile() {
        val selectedTransport =
            AppReadOnlyTransportMapper.map(
                binding.transportSelectorInput.text?.toString().orEmpty(),
            )

        val buildResult =
            AppTransportProfileFactory.build(
                transport = selectedTransport,
                primaryValue = binding.transportPrimaryInput.text?.toString().orEmpty(),
                secondaryValue = binding.transportSecondaryInput.text?.toString().orEmpty(),
                connectTimeoutValue = binding.connectTimeoutInput.text?.toString().orEmpty(),
                readTimeoutValue = binding.readTimeoutInput.text?.toString().orEmpty(),
            )

        if (!buildResult.validation.isValid || buildResult.profile == null) {
            binding.statusText.text = buildResult.validation.errors.joinToString("\n")
            return
        }

        transportProfileStore.save(buildResult.profile)
        binding.statusText.text = getString(R.string.device_settings_saved)
    }

    private fun bindProfileToInputs(profile: AppTransportProfile) {
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
