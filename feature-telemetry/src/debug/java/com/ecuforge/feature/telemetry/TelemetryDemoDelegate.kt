package com.ecuforge.feature.telemetry

import com.ecuforge.core.transport.TransportEndpoint
import com.ecuforge.core.transport.TransportFailureCode
import com.ecuforge.feature.telemetry.domain.ReadTelemetryRequest
import com.ecuforge.feature.telemetry.domain.ReadTelemetryUseCase
import com.ecuforge.feature.telemetry.domain.TelemetryUiState
import com.ecuforge.transport.fake.FakeTransportGateway
import com.ecuforge.transport.fake.FakeTransportOperation
import com.ecuforge.transport.fake.FakeTransportScenario
import com.ecuforge.transport.fake.FakeTransportStep

/**
 * Debug-only demo delegate that wires telemetry flows to fake transport scenarios.
 */
internal object TelemetryDemoDelegate {
    /**
     * Executes the happy-path telemetry snapshot demo.
     */
    suspend fun readTelemetryReadOnlyDemo(): TelemetryUiState {
        return executeTelemetry(defaultSuccessScenario())
    }

    /**
     * Executes telemetry snapshot demo that simulates a read timeout.
     */
    suspend fun readTelemetryReadOnlyTimeoutDemo(): TelemetryUiState {
        return executeTelemetry(defaultTimeoutScenario())
    }

    /**
     * Runs telemetry use case against the provided scripted fake scenario.
     */
    private suspend fun executeTelemetry(scenario: FakeTransportScenario): TelemetryUiState {
        val useCase = ReadTelemetryUseCase(transportGateway = FakeTransportGateway(scenario))

        return useCase.execute(
            request = ReadTelemetryRequest(ecuFamily = "KEIHIN", endpointHint = "BT"),
            endpoint = TransportEndpoint.Bluetooth("AA:BB:CC:DD:EE:FF"),
        )
    }

    /**
     * Provides the default successful telemetry fake script.
     */
    private fun defaultSuccessScenario(): FakeTransportScenario {
        return FakeTransportScenario.of(
            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
            FakeTransportStep(
                operation = FakeTransportOperation.READ,
                success = true,
                readPayload = "RPM=1440|TPS=2.0|ECT=81.8|VBAT=13.8".encodeToByteArray(),
            ),
            FakeTransportStep(
                operation = FakeTransportOperation.READ,
                success = true,
                readPayload = "RPM=1450|TPS=2.1|ECT=82.0|VBAT=13.9".encodeToByteArray(),
            ),
            FakeTransportStep(
                operation = FakeTransportOperation.READ,
                success = true,
                readPayload = "RPM=1460|TPS=2.2|ECT=82.1|VBAT=13.9".encodeToByteArray(),
            ),
            FakeTransportStep(operation = FakeTransportOperation.DISCONNECT, success = true),
        )
    }

    /**
     * Provides the default timeout telemetry fake script.
     */
    private fun defaultTimeoutScenario(): FakeTransportScenario {
        return FakeTransportScenario.of(
            FakeTransportStep(operation = FakeTransportOperation.CONNECT, success = true),
            FakeTransportStep(operation = FakeTransportOperation.WRITE, success = true),
            FakeTransportStep(
                operation = FakeTransportOperation.READ,
                success = true,
                readPayload = "RPM=1450|TPS=2.1|ECT=82.0|VBAT=13.9".encodeToByteArray(),
            ),
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
