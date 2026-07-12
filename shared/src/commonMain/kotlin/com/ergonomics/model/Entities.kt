package com.ergonomics.model

/// 用户档案 — 跨产品共享
data class UserProfile(
    val heightCm: Int = 170,
    val weightKg: Int = 65,
    val age: Int = 30,
    val bodyType: BodyType = BodyType.AVERAGE,
    val knownConditions: List<String> = emptyList()  // 如 "椎间盘突出", "脊柱侧弯"
)

/// 坐姿纠正指令 — 跨产品通用输出
data class CorrectionGuidance(
    val timestamp: Long,
    val currentState: PostureState,
    val targetState: PostureState = PostureState.NEUTRAL,
    val pelvicTiltAdjustDeg: Double = 0.0,     // +前倾 -后倾
    val lateralShiftAdjustCm: Double = 0.0,    // +右移 -左移
    val shoulderAlignDeg: Double = 0.0,
    val verbalGuidance: String = "保持当前坐姿",
    val severity: PostureQuality = PostureQuality.GOOD
) {
    fun toVoicePrompt(): String = when {
        kotlin.math.abs(pelvicTiltAdjustDeg) > 2.0 -> {
            val dir = if (pelvicTiltAdjustDeg > 0) "前倾" else "后倾"
            "请将骨盆${dir}${"%.0f".format(kotlin.math.abs(pelvicTiltAdjustDeg))}度"
        }
        kotlin.math.abs(lateralShiftAdjustCm) > 0.5 -> {
            val dir = if (lateralShiftAdjustCm > 0) "右" else "左"
            "请将重心向${dir}移${"%.1f".format(kotlin.math.abs(lateralShiftAdjustCm))}厘米"
        }
        else -> verbalGuidance
    }
}

/// 疲劳数据 — 跨产品通用
data class FatigueData(
    val timestamp: Long,
    val fatigueIndex: Double = 0.0,
    val level: FatigueLevel = FatigueLevel.COMFORTABLE,
    val continuousSittingMinutes: Double = 0.0,
    val cumulativeStrain: Double = 0.0
)

/// 每日统计摘要
data class DailyStats(
    val totalSittingMinutes: Double = 0.0,
    val goodPostureRate: Double = 0.0,
    val breakCount: Int = 0,
    val avgFatigueIndex: Double = 0.0
)
