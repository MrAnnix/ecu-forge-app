package com.ecuforge.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReadOnlyActionAvailabilityTest {
    @Test
    fun dtcAndTelemetryAreDisabledBeforeSuccessfulIdentification() {
        val result =
            ReadOnlyActionAvailability.evaluate(
                state = ReadOnlyActionAvailability.State(hasSuccessfulIdentification = false),
            )

        assertThat(result.isReadDtcEnabled)
            .describedAs("DTC action should be disabled before identification succeeds")
            .isFalse()
        assertThat(result.isReadTelemetryEnabled)
            .describedAs("Telemetry action should be disabled before identification succeeds")
            .isFalse()
    }

    @Test
    fun dtcAndTelemetryAreEnabledAfterSuccessfulIdentification() {
        val result =
            ReadOnlyActionAvailability.evaluate(
                state = ReadOnlyActionAvailability.State(hasSuccessfulIdentification = true),
            )

        assertThat(result.isReadDtcEnabled)
            .describedAs("DTC action should be enabled after identification succeeds")
            .isTrue()
        assertThat(result.isReadTelemetryEnabled)
            .describedAs("Telemetry action should be enabled after identification succeeds")
            .isTrue()
    }

    @Test
    fun dtcAndTelemetryReturnToDisabledWhenIdentificationIsNoLongerSuccessful() {
        val result =
            ReadOnlyActionAvailability.evaluate(
                state = ReadOnlyActionAvailability.State(hasSuccessfulIdentification = false),
            )

        assertThat(result.isReadDtcEnabled)
            .describedAs("DTC action should return to disabled when identification is not successful")
            .isFalse()
        assertThat(result.isReadTelemetryEnabled)
            .describedAs("Telemetry action should return to disabled when identification is not successful")
            .isFalse()
    }
}

