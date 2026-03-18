package com.ecuforge.feature.telemetry.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class TelemetryRetentionPolicyTest {
    @Test
    fun constructorRejectsInvalidLimits() {
        assertThatThrownBy {
            TelemetryRetentionPolicy(maxAgeDays = 0, maxExportCount = 100)
        }
            .describedAs("Retention policy should reject non-positive age limits")
            .isInstanceOf(IllegalArgumentException::class.java)

        assertThatThrownBy {
            TelemetryRetentionPolicy(maxAgeDays = 30, maxExportCount = 0)
        }
            .describedAs("Retention policy should reject non-positive count limits")
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldRetainReturnsTrueWhenWithinAgeAndCount() {
        val policy = TelemetryRetentionPolicy(maxAgeDays = 30, maxExportCount = 100)

        val retain =
            policy.shouldRetain(
                exportedAtEpochMillis = 1_710_000_000_000,
                nowEpochMillis = 1_710_100_000_000,
                newerExportCount = 10,
            )

        assertThat(retain)
            .describedAs("Retention policy should keep exports that are within age and count limits")
            .isTrue()
    }

    @Test
    fun shouldRetainReturnsFalseWhenAgeLimitExceeded() {
        val policy = TelemetryRetentionPolicy(maxAgeDays = 1, maxExportCount = 100)

        val retain =
            policy.shouldRetain(
                exportedAtEpochMillis = 1_710_000_000_000,
                nowEpochMillis = 1_710_200_000_000,
                newerExportCount = 10,
            )

        assertThat(retain)
            .describedAs("Retention policy should drop exports older than configured maxAgeDays")
            .isFalse()
    }

    @Test
    fun shouldRetainReturnsFalseWhenCountLimitExceeded() {
        val policy = TelemetryRetentionPolicy(maxAgeDays = 30, maxExportCount = 5)

        val retain =
            policy.shouldRetain(
                exportedAtEpochMillis = 1_710_000_000_000,
                nowEpochMillis = 1_710_010_000_000,
                newerExportCount = 5,
            )

        assertThat(retain)
            .describedAs("Retention policy should drop exports when newer export count reaches the configured limit")
            .isFalse()
    }
}
