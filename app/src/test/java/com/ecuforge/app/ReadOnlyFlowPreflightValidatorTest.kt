package com.ecuforge.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReadOnlyFlowPreflightValidatorTest {
    @Test
    fun dtcPrecheckFailsWhenIdentificationWasNotSuccessful() {
        val result =
            ReadOnlyFlowPreflightValidator.validate(
                request =
                    ReadOnlyFlowPreflightValidator.Request(
                        action = ReadOnlyFlowPreflightValidator.Action.DTC,
                        hasSuccessfulIdentification = false,
                        catalogOptIn = false,
                        vehicleMake = "",
                        vehicleModel = "",
                    ),
            )

        assertThat(result)
            .describedAs("DTC precheck should fail when identification has not completed successfully")
            .isInstanceOf(ReadOnlyFlowPreflightValidator.Result.Failure::class.java)
        val failure = result as ReadOnlyFlowPreflightValidator.Result.Failure
        assertThat(failure.code)
            .describedAs("DTC precheck should report identification-required code")
            .isEqualTo(ReadOnlyFlowPreflightValidator.PRECHECK_IDENTIFICATION_REQUIRED)
    }

    @Test
    fun dtcPrecheckFailsWhenCatalogOptInHasMissingVehicleContext() {
        val result =
            ReadOnlyFlowPreflightValidator.validate(
                request =
                    ReadOnlyFlowPreflightValidator.Request(
                        action = ReadOnlyFlowPreflightValidator.Action.DTC,
                        hasSuccessfulIdentification = true,
                        catalogOptIn = true,
                        vehicleMake = "Triumph",
                        vehicleModel = "",
                    ),
            )

        assertThat(result)
            .describedAs("DTC precheck should fail when catalog opt-in is enabled without full vehicle selection")
            .isInstanceOf(ReadOnlyFlowPreflightValidator.Result.Failure::class.java)
        val failure = result as ReadOnlyFlowPreflightValidator.Result.Failure
        assertThat(failure.code)
            .describedAs("Catalog opt-in precheck should report vehicle-selection-required code")
            .isEqualTo(ReadOnlyFlowPreflightValidator.PRECHECK_VEHICLE_SELECTION_REQUIRED)
    }

    @Test
    fun telemetryPrecheckPassesWhenIdentificationWasSuccessful() {
        val result =
            ReadOnlyFlowPreflightValidator.validate(
                request =
                    ReadOnlyFlowPreflightValidator.Request(
                        action = ReadOnlyFlowPreflightValidator.Action.TELEMETRY,
                        hasSuccessfulIdentification = true,
                        catalogOptIn = false,
                        vehicleMake = "",
                        vehicleModel = "",
                    ),
            )

        assertThat(result)
            .describedAs("Telemetry precheck should pass after successful identification")
            .isEqualTo(ReadOnlyFlowPreflightValidator.Result.Success)
    }
}
