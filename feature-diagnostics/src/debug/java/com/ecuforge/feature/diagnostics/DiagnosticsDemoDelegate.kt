package com.ecuforge.feature.diagnostics

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.feature.diagnostics.domain.DtcUiState
import com.ecuforge.feature.diagnostics.domain.IdentificationRequest
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.diagnostics.domain.IdentifyEcuUseCase
import com.ecuforge.feature.diagnostics.domain.ReadDtcRequest
import com.ecuforge.feature.diagnostics.domain.ReadDtcUseCase
import com.ecuforge.transport.fake.FakeTransportGateway
import com.ecuforge.transport.fake.FakeTransportOperation
import com.ecuforge.transport.fake.FakeTransportScenario
import com.ecuforge.transport.fake.FakeTransportStep

/**
 * Debug-only demo delegate that wires diagnostics flows to fake transport scenarios.
 */
internal object DiagnosticsDemoDelegate {
    /**
     * Executes the happy-path identification demo.
     */
    suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        return executeIdentification(defaultSuccessScenario())
    }

    /**
     * Executes identification demo that simulates a read timeout.
     */
    suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        return executeIdentification(defaultTimeoutScenario())
    }

    /**
     * Runs identification use case against the provided scripted fake scenario.
     */
    private suspend fun executeIdentification(scenario: FakeTransportScenario): IdentificationUiState {
        val useCase =
            IdentifyEcuUseCase(
                transportGateway = FakeTransportGateway(scenario),
            )

        return useCase.execute(
            request =
                IdentificationRequest(
                    ecuFamily = "KEIHIN",
                    endpointHint = "BT",
                ),
            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
        )
    }

    /**
     * Provides the default successful identification fake script.
     */
    private fun defaultSuccessScenario(): FakeTransportScenario {
        return FakeTransportScenario.of(
            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
            FakeTransportStep(
                operation = FakeTransportOperation.READ,
                success = true,
                readPayload = "MODEL=KM601EU|FW=2.10.4|SN=A1B2C3".encodeToByteArray(),
            ),
            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
        )
    }

    /**
     * Provides the default timeout identification fake script.
     */
    private fun defaultTimeoutScenario(): FakeTransportScenario {
        return FakeTransportScenario.of(
            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
            FakeTransportStep(
                operation = FakeTransportOperation.READ,
                success = false,
                errorCode = TransportFailureCode.TIMEOUT,
                errorMessage = "Read timeout",
            ),
            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
        )
    }

    /**
     * Executes the happy-path DTC read demo.
     */
    suspend fun readDtcReadOnlyDemo(): DtcUiState {
        return executeDtc(defaultDtcSuccessScenario())
    }

    /**
     * Executes DTC read demo that simulates a read timeout.
     */
    suspend fun readDtcReadOnlyTimeoutDemo(): DtcUiState {
        return executeDtc(defaultDtcTimeoutScenario())
    }

    /**
     * Runs DTC use case against the provided scripted fake scenario.
     */
    private suspend fun executeDtc(scenario: FakeTransportScenario): DtcUiState {
        val useCase =
            ReadDtcUseCase(
                transportGateway = FakeTransportGateway(scenario),
            )

        return useCase.execute(
            request =
                ReadDtcRequest(
                    ecuFamily = "KEIHIN",
                    endpointHint = "BT",
                ),
            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
        )
    }

    /**
     * Provides the default successful DTC fake script.
     */
    private fun defaultDtcSuccessScenario(): FakeTransportScenario {
        return FakeTransportScenario.of(
            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
            FakeTransportStep(
                operation = FakeTransportOperation.READ,
                success = true,
                readPayload = "P0130;O2 Sensor Circuit|P0301;Cylinder 1 Misfire".encodeToByteArray(),
            ),
            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
        )
    }

    /**
     * Provides the default timeout DTC fake script.
     */
    private fun defaultDtcTimeoutScenario(): FakeTransportScenario {
        return FakeTransportScenario.of(
            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
            FakeTransportStep(
                operation = FakeTransportOperation.READ,
                success = false,
                errorCode = TransportFailureCode.TIMEOUT,
                errorMessage = "Read timeout",
            ),
            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
        )
    }
}
