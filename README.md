# Ergonomics Core KMP

人体工学共享核心库 — BLE 基础设施 + 坐姿模型 + 疲劳计算 + 纠正引擎

## 模块

```
ergonomics-core-kmp/shared/
├── model/
│   ├── Enums.kt          — PostureQuality, PostureState, FatigueLevel, ConnectionState, BodyType
│   └── Entities.kt       — UserProfile, CorrectionGuidance, FatigueData, DailyStats
├── data/
│   ├── SessionStore.kt   — ISessionStore<T>, InMemoryStore<T> (通用环形缓冲区)
│   └── CoreHttpClient.kt — Ktor HTTP 客户端 (超时/重试/压缩)
├── engine/
│   ├── CoreFatigueCalculator.kt — IFatigueEngine + 实现 (参数化)
│   └── CoreCorrectEngine.kt     — ICorrectEngine + 实现 (骨盆+侧移+语音)
├── platform/
│   ├── BleAdapter.kt     — expect BLE (Kable)
│   └── PlatformInfo.kt   — expect 平台信息
└── ErgoConfig.kt         — 全局常量

androidMain/ — Kable + Android Bluetooth
iosMain/     — Kable + CoreBluetooth
```

## 依赖方

- **lumiposture-kmp** — 引用 `ergonomics-core` 的 `BleAdapter`, `ISessionStore`, `IFatigueEngine`
- **verdseat-kmp** — 引用 `ergonomics-core` 的 `BleAdapter`, `ISessionStore`, `ICorrectEngine`, `UserProfile`

## 构建

```bash
./gradlew :shared:build        # 编译
./gradlew :shared:allTests     # 测试 (4 类, 24 用例)
```
