package com.ergonomics

/// Ergonomics Core 跨平台发布清单
///
/// 使用方 (lumiposture-kmp / verdseat-kmp):
/// 依赖 `ergonomics-core-kmp:shared`, 导入如下命名空间:
///
///   com.ergonomics.model.*       — PostureQuality, PostureState, FatigueLevel,
///                                   UserProfile, CorrectionGuidance, FatigueData
///   com.ergonomics.data.*        — ISessionStore<T>, InMemoryStore<T>,
///                                   CoreHttpClient
///   com.ergonomics.engine.*      — IFatigueEngine, CoreFatigueCalculator,
///                                   ICorrectEngine, CoreCorrectEngine
///   com.ergonomics.platform.*    — BleAdapter (expect/actual), ScanResult,
///                                   PlatformInfo
///   com.ergonomics               — ErgoConfig
///
/// 各产品工程应:
/// 1. settings.gradle.kts 中 includeBuild("../ergonomics-core-kmp")
/// 2. build.gradle.kts 中 implementation("com.ergonomics:shared") 或 project 依赖
/// 3. 删除本地 BleAdapter expect/actual (改用 core 的)
/// 4. 删除本地 ISessionStore/InMemoryStore (改用 core 的)
/// 5. 删除本地 FatigueLevel/FatigueData 枚举 (改用 core 的)
/// 6. 保留产品特有的模型和引擎 (如 DiscLoadEngine, LumiBleProtocol)
object MigrationGuide {
    // 此文件仅供文档参考, 非可执行代码
}
