package com.ergonomics

/// Ergonomics Core 全局常量
object ErgoConfig {
    const val DEFAULT_SAMPLE_INTERVAL_S = 2.0
    const val DEFAULT_HISTORY_MAX = 70000
    const val DEFAULT_OPTIMAL_TILT_DEG = 10.0

    object Fatigue {
        const val COMFORTABLE_MAX = 25.0
        const val MILD_MAX = 50.0
        const val MODERATE_MAX = 75.0
    }

    object Correction {
        const val MAX_PELVIC_DEG = 15.0
        const val MAX_LATERAL_CM = 3.0
        const val LATERAL_IMBALANCE_THRESHOLD = 0.15
    }

    object Heater {
        const val MIN_TEMP_C = 35
        const val MAX_TEMP_C = 45
    }
}
