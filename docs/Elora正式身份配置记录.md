# Elora 正式应用身份配置

## 2026-11-05 用户确认

| 环境 | applicationId | versionName | versionCode |
| --- | --- | --- | --- |
| prod FORMAL | com.elorattt.app | 1.0.0 | 1 |
| dev | yumo.achat.app | 2.0.0 | 2 |

- 正式配置源为config/prod.properties；dev未声明applicationId，继续使用app/build.gradle.kts的yumo.achat.app回退。源代码namespace仍为yumo.achat.app，不改变app/core/integration边界或已有DataStore名称。
- UI版本、VERSION_NAME、ACHAT_CLIENT_VERSION及归因上报版本仍取同一flavor版本配置。正式签名门禁、verifyProdReleaseRuntimeConfig与真实平台证据包名门禁同步为com.elorattt.app；不改写旧平台证据冒充新包验收。
- 正式Firebase三项功能当前全部false。保留旧com.zorv.app google-services.json作为旧注册配置，不改造其不可变Firebase app ID来伪装新应用；关闭时跳过prod GoogleServices任务并清理旧generated/res/processProd{Debug,Release}GoogleServices输出。dev任务不受影响。后续启用正式Firebase须提供新包匹配的官方配置。
- 现有后端端点、Appsflyer/ThinkingData/Google及支付配置保持现有值。新包名对应的平台注册、分发/签名、渠道/SKU/后台包配置及C01–C38真实验收须独立同步；不以本地APK/单元通过声称平台接入已完成。

## 验证

- `./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug :app:testDevDebugUnitTest :app:testProdDebugUnitTest :app:lintDevDebug :app:lintProdDebug :app:verifyProdReleaseRuntimeConfig` BUILD SUCCESSFUL。
- aapt2 dump badging实际APK确认：dev=yumo.achat.app / 2.0.0 / code2；prod=com.elorattt.app / 1.0.0 / code1。
- aapt2 dump resources确认dev APK仍有google_app_id，prod APK没有旧google_app_id，避免旧生成资源残留；这是禁用Firebase资源校验，不是新Firebase平台注册证据。
- FormalProdConfigurationTest验证源配置、两flavor编译身份/版本一致性、旧Firebase配置与禁用边界；独立只读审查未发现阻断问题。未构建/发布正式签名分发包。
- 实施前已同步 /Users/kuailegeziwl/.codex/skills/my-implementation-workflow/SKILL.md 的Elora正式身份/初始版本约定；git diff --check通过。

## prod 数数 App ID 更新（2026-11-05）

按用户要求将prod的build.string.TD_APP_ID更新为T4b0x6g2r9s7kdpq5v3n8jchwm1yzf，同步verifyProdReleaseRuntimeConfig门禁及FormalProdConfigurationTest源配置/编译值断言。dev App ID、服务端地址、事件/队列与验收证据不改。本轮为单项配置和配套校验变更，采用单Agent核对；实施前已同步实施Skill。本轮根要求构建/单元/lint、显式app dev/prod单元/lint和verifyProdReleaseRuntimeConfig均通过（BUILD SUCCESSFUL）。生成的prod BuildConfig.TD_APP_ID已核对为新值，dev仍为98d422d47d1449ee8af3dd4a9efcb331；git diff --check通过。未以本地验证声称真实数数平台事件已验收。

## 应用显示名称（2026-11-05）

按用户要求，common/dev/prod的app.display.name及Android app_name资源统一为Elora。manifest继续使用appLabel占位符，不改变包名、namespace、版本、SDK身份或上报配置。本轮为单项展示名配置及配套断言修改，采用单Agent核对；实施前已同步实施Skill。本轮根构建/单元/lint、显式app dev/prod单元/lint及正式运行配置校验均BUILD SUCCESSFUL；aapt2实际APK核对dev/prod application-label均为Elora，包名和版本保持当前配置；git diff --check通过。

## prod AppsFlyer Dev Key 更新（2026-11-05）

按用户指定值更新config/prod.properties中的build.string.APPSFLYER_DEV_KEY，同步FormalProdConfigurationTest源配置与prod BuildConfig断言；dev、SDK初始化、事件队列及归因持久化不变。本轮为单项配置与配套断言调整，采用单Agent核对；实施前已同步实施Skill。本轮根构建/单元/lint、显式app dev/prod单元/lint和正式运行配置校验均BUILD SUCCESSFUL；生成prod BuildConfig中的key与用户指定值一致，git diff --check通过。本地构建验证不代表真实平台归因验收。

## prod 后台与媒体域名（2026-11-05）

- 后台：https://release.eloraapp.loan，API保留/api/v1/，支付保留/payment-api/v1/；WebSocket为wss://release.eloraapp.loan，连接地址派生为/connection/websocket。
- 媒体CDN：https://cdn.eloraapp.loan。生产端点由app/build.gradle.kts的flavorEndpoints维护，已同步正式运行配置门禁、prod.properties注释及配置单元/编译值断言；dev端点及第三方SDK接收地址未改。后台返回的完整媒体URL不做强制字符串改写，避免破坏签名URL。
- 实施前已同步实施Skill，独立只读复核未发现阻断问题；新域名的在线、TLS、接口/回调和C01–C38平台验收需真实环境证据，不由本地构建替代。本轮根构建/单元/lint、显式app dev/prod单元/lint及verifyProdReleaseRuntimeConfig均BUILD SUCCESSFUL；生成的dev/prod BuildConfig已逐项核对，git diff --check通过。
