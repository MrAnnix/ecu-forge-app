package com.ecuforge.feature.telemetry.domain

/**
 * Retention policy for telemetry export artifacts.
 *
 * @property maxAgeDays Maximum export age in days before expiration.
 * @property maxExportCount Maximum number of retained exports.
 */
data class TelemetryRetentionPolicy(
    val maxAgeDays: Int = 30,
    val maxExportCount: Int = 100,
) {
    init {
        require(maxAgeDays > 0) { "maxAgeDays must be greater than zero" }
        require(maxExportCount > 0) { "maxExportCount must be greater than zero" }
    }

    /**
     * Returns true when an export should be retained considering age and count limits.
     */
    fun shouldRetain(
        exportedAtEpochMillis: Long,
        nowEpochMillis: Long,
        newerExportCount: Int,
    ): Boolean {
        if (nowEpochMillis < exportedAtEpochMillis) {
            return false
        }

        val maxAgeMillis = maxAgeDays.toLong() * MILLIS_PER_DAY
        val ageMillis = nowEpochMillis - exportedAtEpochMillis
        val withinAge = ageMillis <= maxAgeMillis
        val withinCount = newerExportCount < maxExportCount
        return withinAge && withinCount
    }

    private companion object {
        const val MILLIS_PER_DAY: Long = 86_400_000L
    }
}
