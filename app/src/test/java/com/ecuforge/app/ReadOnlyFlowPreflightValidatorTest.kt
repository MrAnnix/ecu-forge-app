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

    @Test
    fun telemetryPrecheckFailsWhenIdentificationWasNotSuccessful() {
        val result =
            ReadOnlyFlowPreflightValidator.validate(
                request =
                    ReadOnlyFlowPreflightValidator.Request(
                        action = ReadOnlyFlowPreflightValidator.Action.TELEMETRY,
                        hasSuccessfulIdentification = false,
                        catalogOptIn = false,
                        vehicleMake = "",
                        vehicleModel = "",
                    ),
            )

        assertThat(result)
            .describedAs("Telemetry precheck should fail when identification has not completed successfully")
            .isInstanceOf(ReadOnlyFlowPreflightValidator.Result.Failure::class.java)
        val failure = result as ReadOnlyFlowPreflightValidator.Result.Failure
        assertThat(failure.code)
            .describedAs("Telemetry precheck should report identification-required code")
            .isEqualTo(ReadOnlyFlowPreflightValidator.PRECHECK_IDENTIFICATION_REQUIRED)
    }

    @Test
    fun dtcPrecheckPassesWhenCatalogOptInIsDisabledAndIdentificationWasSuccessful() {
        val result =
            ReadOnlyFlowPreflightValidator.validate(
                request =
                    ReadOnlyFlowPreflightValidator.Request(
                        action = ReadOnlyFlowPreflightValidator.Action.DTC,
                        hasSuccessfulIdentification = true,
                        catalogOptIn = false,
                        vehicleMake = "",
                        vehicleModel = "",
                    ),
            )

        assertThat(result)
            .describedAs("DTC precheck should pass when identification succeeded and catalog opt-in is disabled")
            .isEqualTo(ReadOnlyFlowPreflightValidator.Result.Success)
    }

    @Test
    fun dtcPrecheckPassesWhenCatalogOptInHasFullVehicleContext() {
        val result =
            ReadOnlyFlowPreflightValidator.validate(
                request =
                    ReadOnlyFlowPreflightValidator.Request(
                        action = ReadOnlyFlowPreflightValidator.Action.DTC,
                        hasSuccessfulIdentification = true,
                        catalogOptIn = true,
                        vehicleMake = "Triumph",
                        vehicleModel = "Street Triple",
                    ),
            )

        assertThat(result)
            .describedAs("DTC precheck should pass when catalog opt-in is enabled with both make and model")
            .isEqualTo(ReadOnlyFlowPreflightValidator.Result.Success)
    }

    @Test
    fun dtcPrecheckFailsWhenCatalogOptInVehicleModelContainsOnlyWhitespace() {
        val result =
            ReadOnlyFlowPreflightValidator.validate(
                request =
                    ReadOnlyFlowPreflightValidator.Request(
                        action = ReadOnlyFlowPreflightValidator.Action.DTC,
                        hasSuccessfulIdentification = true,
                        catalogOptIn = true,
                        vehicleMake = "Triumph",
                        vehicleModel = "   ",
                    ),
            )

        assertThat(result)
            .describedAs("DTC precheck should fail when vehicle model is only whitespace under catalog opt-in")
            .isInstanceOf(ReadOnlyFlowPreflightValidator.Result.Failure::class.java)
        val failure = result as ReadOnlyFlowPreflightValidator.Result.Failure
        assertThat(failure.code)
            .describedAs("Whitespace-only model should still report vehicle-selection-required code")
            .isEqualTo(ReadOnlyFlowPreflightValidator.PRECHECK_VEHICLE_SELECTION_REQUIRED)
    }

    @Test
    fun identificationPrecheckAlwaysPasses() {
        val result =
            ReadOnlyFlowPreflightValidator.validate(
                request =
                    ReadOnlyFlowPreflightValidator.Request(
                        action = ReadOnlyFlowPreflightValidator.Action.IDENTIFICATION,
                        hasSuccessfulIdentification = false,
                        catalogOptIn = true,
                        vehicleMake = "",
                        vehicleModel = "",
                    ),
            )

        assertThat(result)
            .describedAs("Identification action should not be blocked by read-only prechecks")
            .isEqualTo(ReadOnlyFlowPreflightValidator.Result.Success)
    }
}
