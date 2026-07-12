# CHANGELOG

## v1.0.0 — 2026-06-07

### Initial release
- 工程扫描: 22 文件
- BleAdapter expect/actual (Android Kable + iOS CoreBluetooth)
- ISessionStore<T> + InMemoryStore<T> (Mutex 线程安全)
- IFatigueEngine + CoreFatigueCalculator (参数化)
- ICorrectEngine + CoreCorrectEngine
- CoreHttpClient (Ktor, 超时/重试/压缩)
- 共享模型: PostureQuality, PostureState, FatigueLevel, BodyType, UserProfile, CorrectionGuidance, FatigueData, DailyStats
- 测试: 4 类 24 用例
- CI/CD: build + test
