# KMP Engineering Workflow v2 — 九阶段工程质量加固

适用场景：Kotlin Multiplatform + Compose Multiplatform 工程的完整工程质量加固。
输入：一个已有的 KMP 工程。
输出：安全审计报告 + 抽象重构 + 代码审查报告 + 测试套件 + 静态分析配置 + CI/CD 流水线 + 文档更新。

原则：**先诊断后手术，先验证后交付。**

## 总览

```
Phase 1: Explore     → 扫描全量代码，理解架构和依赖关系
Phase 2: Audit       → 安全审计 + 依赖审计，列出所有待修复项 ⚠️ 先于 Abstract
Phase 3: Abstract    → 按审计发现重构：抽接口、拆文件、修复致命 bug
Phase 4: Review      → 审查抽象结果：架构评分、命名、错误处理、KMP 实践
Phase 5: Test        → 补测试覆盖：引擎/解析器/存储/模型/协议，每类 5-14 用例
Phase 6: Lint        → 静态分析：detekt 配置 + 规则集，跑 check
Phase 7: Build       → 编译验证：androidRelease + iosFramework + allTests 全部通过
Phase 8: CI/CD       → 生成/更新 CI pipeline，验证 CI 能跑通
Phase 9: Finalize    → 文档 + memory + changelog + git tag
```

耗时估算：~120 分钟/工程（比 v1 多 30 分钟，主要加在 Lint + Build + CI/CD）

---

## Phase 1: Explore（工程扫描）

目标：10 分钟内建立完整代码理解。

### 步骤

1. `find <project> -name "*.kt" -o -name "*.kts" -o -name "*.swift" | grep -v .gradle | grep -v build | sort`
2. 读取顺序：model → engine → data → platform → UI → build scripts → CI yml
3. 输出工程清单：文件数、行数、包结构、expect/actual 配对矩阵、依赖图

### 自动检查清单

- [ ] expect/actual 配对完整（Android + iOS 各一个 actual）
- [ ] sourceSets 完整（commonMain/androidMain/iosMain + commonTest）
- [ ] versions catalog 存在且引用一致
- [ ] AndroidManifest 存在
- [ ] .github/workflows/ 存在
- [ ] README.md + CLAUDE.md 存在

---

## Phase 2: Audit（双重审计：安全 + 依赖）

⚠️ **先于 Abstract 执行。** 发现问题比动手改更优先——否则改了再审计会返工。

### 2a. 安全审计

维度：BLE 通信、数据隐私、输入验证、物理安全、权限声明

报告输出到 `code-review-reports/<project>-security-audit-<date>.md`

自动检查项（能在不改代码的情况下扫描出来）：
- AndroidManifest 缺少 BLE/Location 权限 → 🔴
- Info.plist 缺少蓝牙描述 → 🔴
- 帧解析器无最小长度校验 → 🔴
- 加热/电机命令无温度/力度钳制 → 🟡
- HttpClient 无超时/重试配置 → 🟡
- ProGuard/R8 规则缺失 Ktor/Serialization/Coil → 🟡

### 2b. 依赖审计

扫描 `gradle/libs.versions.toml`，检查：
- 所有依赖版本是否是最新 stable（对比 Maven Central / Google Maven）
- 是否有已知 CVE（Ktor < 3.1.1 有 HTTP/2 相关 CVE，Coil < 3.0.4 有 decoding issue）
- 是否有 alpha/beta/RC 版本在生产路径上（导航库 navigation-compose 经常是 alpha）
- Kotlin/AGP/Compose 版本三角是否兼容（Kotlin 2.1.x + Compose 1.7.x + AGP 8.7.x）

输出：依赖审计表，格式：

```
| 依赖 | 当前版本 | 最新 stable | 状态 | 备注 |
| ktor | 3.1.0 | 3.1.1 | 🟡 可升级 | HTTP/2 flow control fix |
```

---

## Phase 3: Abstract（抽象封装）

目标：按 Phase 2 的发现修复架构缺陷 + 抽取接口层。

### 执行顺序

1. 先修 P0/🔴 项目（致命 bug、权限缺失）
2. 再拆文件（单文件多职责 → 独立模块）
3. 最后抽接口（engine/repository/store）

### 标准拆分模板

```
重构前: PostureRepository.kt (expect + protocol + parser + repo = 4 职责)
重构后:
  platform/BleAdapter.kt       ← expect 声明
  data/<Device>BleProtocol.kt  ← 协议常量 + 命令编码
  data/<Device>FrameParser.kt  ← Result<T> 帧解析
  data/I<Name>Repository.kt    ← 接口
  data/<Name>Repository.kt     ← 实现 (构造器注入 BleAdapter)
```

### 接口模板

```kotlin
// 引擎 — 策略模式，可替换为 ML 版
interface I<Name>Engine { fun calculate(input): Output }

// 仓库 — 可模拟，解耦 BLE
interface I<Name>Repository { fun stream(): Flow<Data>; suspend fun command(...) }

// 存储 — 泛型，可替换为 SQLite/云端
interface ISessionStore<T> {
    suspend fun add(item: T)
    suspend fun today(): List<T>
    suspend fun recent(count: Int): List<T>
    suspend fun count(): Int
    suspend fun clear()
}
```

### 并发安全（每个工程必做）

- InMemoryStore: 所有读写通过 `Mutex.withLock`
- BleAdapter.connect(): `connectMutex.withLock` 防竞态双连

---

## Phase 4: Review（代码审查）

目标：验证 Phase 3 的重构质量，生成审查报告。

审查维度：架构、错误处理、命名、KMP 实践、性能、并发安全

报告输出到 `code-review-reports/<project>-review-<date>.md`

---

## Phase 5: Test（测试覆盖）

目标：核心模块达到可验证正确性。每个模块覆盖：正常路径 + 边界值 + 异常路径 + 并发安全。

### 测试文件生成

```
shared/src/commonTest/kotlin/<package>/
├── engine/<Engine>Test.kt       — 7-14 用例
├── engine/<Engine>ContractTest.kt — 2-3 用例 (接口验证)
├── data/<Parser>Test.kt         — 5-7 用例
├── data/<Store>Test.kt          — 5-6 用例
├── data/<Protocol>Test.kt       — 5-6 用例
├── model/<Model>Test.kt         — 6-9 用例
├── model/EntitiesTest.kt        — 6-9 用例
└── engine/TestFixtures.kt       — 标准测试夹具
```

### 必覆盖的用例类型

| 模块 | 理想路径 | 边界值 | 异常路径 | 并发 | 确定性 |
|------|---------|--------|---------|------|--------|
| Engine | ✅ | ✅ | ✅ | — | ✅ |
| Parser | ✅ | ✅ | ✅ (帧损坏) | — | ✅ |
| Store | ✅ | ✅ (满淘汰) | ✅ (空查询) | ✅ | — |
| Protocol | ✅ | ✅ (钳制) | ✅ (溢出) | — | ✅ |
| Model | ✅ | ✅ (默认值) | ✅ (校验) | — | — |

### 环境配置

在 `shared/build.gradle.kts` 添加 `commonTest.dependencies`:
```kotlin
commonTest.dependencies {
    implementation(kotlin("test"))
    implementation(libs.kotlinx.coroutines.test)
}
```

在 `libs.versions.toml` 添加：
```toml
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
```

---

## Phase 6: Lint（静态分析）

目标：在构建之前发现代码异味和潜在 bug。

### 配置 detekt

在工程根目录创建 `detekt-config.yml`，包含 KMP 专项规则：

```yaml
style:
  WildcardImport: { active: true }
  MagicNumber: { active: true, ignoreNumbers: ['-1','0','1','2','100','255','1000'] }

potential-bugs:
  UnsafeCast: { active: true }

compose:
  UnnecessaryComposition: { active: true }  # 避免无意义重组

naming:
  ClassNaming: { classPattern: '[A-Z][a-zA-Z0-9]*' }
  FunctionNaming: { functionPattern: '[a-z][a-zA-Z0-9]*' }
```

### 自动化检查项

- expect class 签名与 actual class 一致（方法名、参数类型、返回类型）
- commonMain 无 `android.*` / `UIKit.*` 导入
- 无未使用的 import
- 无 `!!` 强制解包（建议用 `?.let` 或 `?: error`）
- Compose `@Composable` 函数无副作用（不在 Composable 里读 `Clock.System.now()`）

### 执行

```bash
./gradlew :shared:detekt
./gradlew :composeApp:detekt
```

---

## Phase 7: Build（构建验证）

目标：确认重构后的代码可以编译通过。

### 验证矩阵

| 平台 | 命令 | 验证内容 |
|------|------|---------|
| Android | `./gradlew :composeApp:assembleRelease` | ProGuard 不报错，APK 产物存在 |
| iOS | `./gradlew :shared:linkDebugFrameworkIosArm64` | framework 编译通过 |
| 测试 | `./gradlew :shared:allTests` | 所有 test 通过 |

如果任一命令失败 → 回到对应 Phase 修复 → 重新 Build 验证。

### 产物检查

- Android APK 文件存在：`composeApp/build/outputs/apk/release/*.apk`
- iOS framework 存在：`shared/build/bin/iosArm64/debugFramework/shared.framework/`
- 测试报告：`shared/build/reports/tests/`

---

## Phase 8: CI/CD（持续集成配置）

目标：CI pipeline 能在 Push/PR 时自动执行 Build 的所有验证。

### CI YAML 模板

```yaml
name: <Project> CI/CD
on:
  push: { branches: [main] }
  pull_request: { branches: [main] }
jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew :shared:detekt :composeApp:detekt

  test:
    runs-on: ubuntu-latest
    needs: lint
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew :shared:allTests

  android-build:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew :composeApp:assembleRelease
      - uses: actions/upload-artifact@v4
        with: { name: <project>-android, path: composeApp/build/outputs/apk/release/*.apk }

  ios-build:
    runs-on: macos-latest
    needs: test
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew :shared:linkDebugFrameworkIosArm64
```

### CI 验证

- 确认 CI YAML 写入 `.github/workflows/ci.yml`
- 若需 macOS runner（iOS build），标注 `macos-latest` 而非 `ubuntu-latest`
- lint → test → build 串行依赖：lint 不过就不跑 test，test 不过就不跑 build

---

## Phase 9: Finalize（收尾交付）

- [ ] 更新 CLAUDE.md（架构图 + 命令 + 模块说明 + 测试覆盖表）
- [ ] 更新 README.md（功能列表 + 架构树 + 接口表 + 构建命令 + 测试统计）
- [ ] 生成 CHANGELOG.md（本次加固的所有变更摘要）
- [ ] 保存审查报告到 `code-review-reports/<project>-review-<date>.md`
- [ ] 保存审计报告到 `code-review-reports/<project>-security-audit-<date>.md`
- [ ] 保存依赖审计报告到 `code-review-reports/<project>-dependency-audit-<date>.md`
- [ ] 更新 memory（项目记忆文件）
- [ ] git tag: `engineering-v2-<date>`

### CHANGELOG 模板

```markdown
# <Project> Engineering Hardening — <date>

## 安全修复
- 🔴 <致命 bug>
- 🟡 <中风险项>

## 抽象重构
- ✅ 新增 N 个接口: <列表>
- ✅ 拆分 N 个模块: <列表>

## 测试
- ✅ N 个测试类, M 个用例

## CI/CD
- ✅ lint → test → android-build → ios-build 流水线
- ✅ 依赖审计通过

## 文档
- ✅ CLAUDE.md + README.md 更新
```

---

## v1 → v2 变更摘要

| 变更 | v1 | v2 |
|------|-----|-----|
| 阶段数 | 6 | 9 |
| 审计位置 | Phase 4（抽象后） | Phase 2（抽象前）⚠️ |
| 依赖审计 | 无 | Phase 2b — 版本/CVE 扫描 |
| Lint/静态分析 | 无 | Phase 6 — detekt 配置 + KMP 规则 |
| 构建验证 | 散落 | Phase 7 — 独立门禁 (Android+iOS+Test) |
| CI/CD | Finalize 一个 checkbox | Phase 8 — 独立模板生成 + 串行验证 |
| 版本标记 | 无 | Phase 9 — CHANGELOG + git tag |
| 总耗时 | ~90 min | ~120 min |
