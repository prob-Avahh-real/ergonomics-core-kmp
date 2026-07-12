package com.ergonomics.engine

import com.ergonomics.model.*

/// 坐姿纠正引擎接口 — 跨产品通用
interface ICorrectEngine {
    fun compute(
        postureState: PostureState,
        anteriorShiftPercent: Double,
        leftRightImbalancePercent: Double
    ): CorrectionGuidance
}

/// 默认坐姿纠正引擎
class CoreCorrectEngine(
    private val maxPelvicDeg: Double = 15.0,
    private val maxLateralCm: Double = 3.0
) : ICorrectEngine {

    override fun compute(
        postureState: PostureState,
        anteriorShiftPercent: Double,
        leftRightImbalancePercent: Double
    ): CorrectionGuidance {
        val pelvic = pelvicAdjustment(postureState, anteriorShiftPercent)
        val lateral = lateralAdjustment(leftRightImbalancePercent)
        val shoulder = shoulderAlign(postureState, leftRightImbalancePercent)

        return CorrectionGuidance(
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            currentState = postureState,
            pelvicTiltAdjustDeg = pelvic,
            lateralShiftAdjustCm = lateral,
            shoulderAlignDeg = shoulder,
            verbalGuidance = verbal(postureState, pelvic, lateral, anteriorShiftPercent, leftRightImbalancePercent),
            severity = when (postureState) {
                PostureState.NEUTRAL -> PostureQuality.EXCELLENT
                PostureState.ANTERIOR_TILT, PostureState.POSTERIOR_TILT -> PostureQuality.FAIR
                PostureState.LATERAL_SHIFT -> PostureQuality.POOR
                PostureState.SLOUCHING -> PostureQuality.POOR
                PostureState.KYPHOTIC -> PostureQuality.CRITICAL
            }
        )
    }

    private fun pelvicAdjustment(state: PostureState, anteriorPct: Double): Double = when (state) {
        PostureState.ANTERIOR_TILT -> -min(kotlin.math.abs(anteriorPct * 0.5), maxPelvicDeg)
        PostureState.POSTERIOR_TILT -> min(kotlin.math.abs(anteriorPct * 0.5), maxPelvicDeg)
        PostureState.SLOUCHING -> -min(kotlin.math.abs(anteriorPct * 0.3), maxPelvicDeg * 0.7)
        else -> 0.0
    }

    private fun lateralAdjustment(imbalancePct: Double): Double {
        val raw = imbalancePct / 100.0 * 10.0
        return if (raw < 0.15) 0.0 else raw.coerceIn(-maxLateralCm, maxLateralCm)
    }

    private fun shoulderAlign(state: PostureState, imbalancePct: Double): Double = when (state) {
        PostureState.LATERAL_SHIFT -> imbalancePct * 0.2
        else -> 0.0
    }

    private fun verbal(
        state: PostureState, pelvic: Double, lateral: Double,
        ant: Double, lat: Double
    ): String = when {
        state == PostureState.NEUTRAL -> "✓ 坐姿良好，继续保持"
        kotlin.math.abs(pelvic) > 5.0 -> "骨盆${if (pelvic < 0) "后倾" else "前倾"}${"%.0f".format(kotlin.math.abs(pelvic))}度"
        kotlin.math.abs(lateral) > 1.0 -> "重心偏移，${if (lateral < 0) "左" else "右"}移${"%.1f".format(kotlin.math.abs(lateral))}cm"
        else -> state.description
    }

    private fun min(a: Double, b: Double) = kotlin.math.min(a, b)
}
