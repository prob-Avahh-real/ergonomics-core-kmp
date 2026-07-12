package com.ergonomics.model

/// 坐姿质量 — 跨产品通用
enum class PostureQuality(val label: String, val description: String) {
    EXCELLENT("优秀", "骨盆中正，压力均匀分布"),
    GOOD("良好", "轻微偏差，无需纠正"),
    FAIR("一般", "中度偏差，建议微调"),
    POOR("较差", "明显异常，需要纠正"),
    CRITICAL("严重", "请立即调整坐姿")
}

/// 坐姿状态分类 — 骨盆 + 胸椎综合
enum class PostureState(val label: String, val description: String) {
    NEUTRAL("中立位", "骨盆中正，椎间盘压力均匀分布"),
    ANTERIOR_TILT("骨盆前倾", "腰椎曲度过大，L4-S1 压力集中"),
    POSTERIOR_TILT("骨盆后倾", "腰椎曲度消失，椎间盘前部压力增大"),
    LATERAL_SHIFT("侧移", "骨盆左右不对称，一侧椎间盘受压"),
    SLOUCHING("驼背", "胸椎后凸过度，颈椎前探"),
    KYPHOTIC("脊柱后凸", "上背部过度弯曲")
}

/// 疲劳等级 — 跨产品通用
enum class FatigueLevel(val label: String, val emoji: String, val suggestion: String) {
    COMFORTABLE("舒适", "😊", "继续保持良好坐姿"),
    MILD("轻度疲劳", "😐", "可以微调坐姿"),
    MODERATE("中度疲劳", "😟", "建议起身活动"),
    SEVERE("重度疲劳", "😫", "请立即起身休息")
}

/// 设备连接状态
enum class ConnectionState(val label: String) {
    DISCONNECTED("未连接"), CONNECTING("连接中..."), CONNECTED("已连接")
}

/// 用户体质 — 影响压力阈值和疲劳计算
enum class BodyType(val label: String, val loadMultiplier: Double) {
    SLIM("偏瘦", 0.85),
    AVERAGE("标准", 1.0),
    ATHLETIC("运动型", 1.1),
    OVERWEIGHT("偏重", 1.25)
}
