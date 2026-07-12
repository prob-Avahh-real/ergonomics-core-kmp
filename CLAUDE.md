# Ergonomics Core KMP

人体工学共享核心库 — BLE 基础设施 + 坐姿模型 + 疲劳计算 + 纠正引擎

## 模块

```
shared/commonMain/
├── model/
│   ├── Enums.kt          — PostureQuality, PostureState, FatigueLevel, ConnectionState, BodyType
│   └── Entities.kt       — UserProfile, CorrectionGuidance, FatigueData, DailyStats
├── data/
│   ├── SessionStore.kt   — ISessionStore<T>, InMemoryStore<T> (Mutex 线程安全)
│   └── CoreHttpClient.kt — Ktor HTTP 客户端
├── engine/
│   ├── CoreFatigueCalculator.kt — IFatigueEngine + 实现 (参数化)
│   └── CoreCorrectEngine.kt     — ICorrectEngine + 实现
├── platform/
│   ├── BleAdapter.kt     — expect BLE (Kable)
│   └── PlatformInfo.kt   — expect 平台信息
└── ErgoConfig.kt         — 全局常量
```

## 依赖方

- **lumiposture-kmp** — 智能腰托 (BLE + 疲劳计算)
- **verdseat-kmp** — 翠座/智能坐垫 (BLE + 坐姿纠正)

## 构建

```bash
./gradlew :shared:build        # 编译
./gradlew :shared:allTests     # 测试 (4 类, 24 用例)
```
