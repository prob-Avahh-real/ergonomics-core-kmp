package com.ergonomics.engine

import com.ergonomics.model.FatigueData
import com.ergonomics.model.FatigueLevel
import kotlin.math.abs
import kotlin.math.min

/// 疲劳计算引擎接口 — 跨产品通用
interface IFatigueEngine {
    fun calculate(
        sittingDurationSeconds: Int,
        asymmetryIndex: Double,       // 0-1, 1=完美对称
        tiltDeviationDeg: Double,     // 偏离最佳角度的度数
        knownConditions: List<String> = emptyList()
    ): FatigueData
}

/// 时域加权线性疲劳计算器 — 跨产品通用
///
/// 公式:
///   fatigueIndex = (tiltDev × W_TILT + pressureAsym × W_ASYM + durationFactor × W_DUR) × 100
class CoreFatigueCalculator(
    private val optimalTiltDeg: Double = 10.0,
    private val tiltWeight: Double = 0.25,
    private val asymmetryWeight: Double = 0.30,
    private val durationWeight: Double = 0.30,
    private val durationCapHours: Double = 4.0,
    private val strainDivisor: Double = 7200.0
) : IFatigueEngine {

    override fun calculate(
        sittingDurationSeconds: Int,
        asymmetryIndex: Double,
        tiltDeviationDeg: Double,
        knownConditions: List<String>
    ): FatigueData {
        val tiltDev = min(abs(tiltDeviationDeg) / 30.0, 1.0)
        val pressureAsym = 1.0 - asymmetryIndex
        val sittingHours = sittingDurationSeconds / 3600.0
        val durFactor = min(sittingHours / durationCapHours, 1.0)

        val rawIndex = (tiltDev * tiltWeight + pressureAsym * asymmetryWeight + durFactor * durationWeight) * 100.0
        val conditionFactor = if (knownConditions.isNotEmpty()) 1.15 else 1.0

        val fatigueIndex = (rawIndex * conditionFactor).coerceIn(0.0, 100.0)
        val cumulativeStrain = fatigueIndex * (1.0 + sittingDurationSeconds / strainDivisor).coerceAtMost(200.0)

        val level = when {
            fatigueIndex < 25.0 -> FatigueLevel.COMFORTABLE
            fatigueIndex < 50.0 -> FatigueLevel.MILD
            fatigueIndex < 75.0 -> FatigueLevel.MODERATE
            else -> FatigueLevel.SEVERE
        }

        return FatigueData(
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            fatigueIndex = fatigueIndex,
            level = level,
            continuousSittingMinutes = sittingHours * 60.0,
            cumulativeStrain = cumulativeStrain
        )
    }
}
