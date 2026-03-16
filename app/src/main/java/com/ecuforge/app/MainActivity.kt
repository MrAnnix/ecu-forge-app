package com.ecuforge.app

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ecuforge.app.databinding.ActivityMainBinding
import com.ecuforge.feature.diagnostics.DiagnosticsFeatureEntry
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val screenScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        renderState(IdentificationUiState.Idle)
        binding.identifyButton.setOnClickListener {
            runReadOnlyIdentification()
        }
    }

    override fun onDestroy() {
        screenScope.cancel()
        super.onDestroy()
    }

    private fun runReadOnlyIdentification() {
        val isDebuggableBuild = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebuggableBuild) {
            renderState(
                IdentificationUiState.Error(
                    code = "DEMO_DISABLED",
                    message = "Demo identification is only available in debug builds"
                )
            )
            return
        }

        binding.identifyButton.isEnabled = false
        renderState(IdentificationUiState.Loading)

        screenScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    DiagnosticsFeatureEntry.identifyReadOnlyDemo()
                }
                renderState(result)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                renderState(
                    IdentificationUiState.Error(
                        code = "UNEXPECTED",
                        message = exception.message ?: "Unexpected application error"
                    )
                )
            } finally {
                binding.identifyButton.isEnabled = true
            }
        }
    }

    private fun renderState(state: IdentificationUiState) {
        binding.statusText.text = IdentificationStatusFormatter.format(state)
    }
}
