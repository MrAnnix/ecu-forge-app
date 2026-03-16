package com.ecuforge.feature.diagnostics

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.feature.diagnostics.domain.IdentificationRequest
import com.ecuforge.feature.diagnostics.domain.IdentificationUiState
import com.ecuforge.feature.diagnostics.domain.IdentifyEcuUseCase
import com.ecuforge.transport.fake.FakeTransportGateway
import com.ecuforge.transport.fake.FakeTransportOperation
import com.ecuforge.transport.fake.FakeTransportScenario
import com.ecuforge.transport.fake.FakeTransportStep

internal object DiagnosticsDemoDelegate {

    suspend fun identifyReadOnlyDemo(): IdentificationUiState {
        return executeIdentification(defaultSuccessScenario())
    }

    suspend fun identifyReadOnlyTimeoutDemo(): IdentificationUiState {
        return executeIdentification(defaultTimeoutScenario())
    }

    private suspend fun executeIdentification(
        scenario: FakeTransportScenario
    ): IdentificationUiState {
        val useCase = IdentifyEcuUseCase(
            transportGateway = FakeTransportGateway(scenario)
        )

        return useCase.execute(
            request = IdentificationRequest(
                ecuFamily = "KEIHIN",
                endpointHint = "BT"
            ),
            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF")
        )
    }

    private fun defaultSuccessScenario(): FakeTransportScenario {
        return FakeTransportScenario.of(
            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
            FakeTransportStep(
                operation = FakeTransportOperation.READ,
                success = true,
                readPayload = "MODEL=KM601EU|FW=2.10.4|SN=A1B2C3".encodeToByteArray()
            ),
            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true)
        )
    }

    private fun defaultTimeoutScenario(): FakeTransportScenario {
        return FakeTransportScenario.of(
            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
            FakeTransportStep(
                operation = FakeTransportOperation.READ,
                success = false,
                errorCode = TransportFailureCode.TIMEOUT,
                errorMessage = "Read timeout"
            ),
            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true)
        )
    }
}

