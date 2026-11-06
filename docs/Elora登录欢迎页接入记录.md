# Elora 登录欢迎页接入记录

- 设计来源：[Figma 1:2040](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-2040)。
- `ui/login/LoginWelcomeScreen.kt` 实现未登录 Choice 页，包含圆形 Logo、玻璃覆盖层、紫色氛围背景、两行标语、功能标签、两种登录入口及协议链接；使用 Android 系统栏，短屏/大字体允许滚动。
- `LoginScreen` 保留默认勾选、E-201 协议拦截、submitting、服务器错误、账号表单与协议 WebView；认证、归因及已有会话恢复门禁未修改。
- 字符串在 `login_strings.xml`，稳定颜色在 `ui/theme/Color.kt`，素材与使用权证据见 `asset-sources.md`。

## 2026-11-04 本地验证

- `./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug` 通过。
- `:app:connectedDevDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=yumo.achat.app.ui.login.LoginScreenTest`：API 36 模拟器 6/6 通过，覆盖欢迎页、默认勾选、账号进入及返回、busy 禁用、取消协议拦截、输入校验，以及 320×480dp / 1.5 倍字体下操作区和协议可滚动到达。
- 已检查设备截图，常规屏幕首屏完整显示协议；只读交叉审查未发现明确回归。测试生成截图仅用于本地验证，不纳入仓库。
- 根目录 `connectedDebugAndroidTest` 已尝试，但 core、integration:analytics-appsflyer、integration:analytics-thinkingdata 在启动 instrumentation 时因缺失 `androidx.test.runner.AndroidJUnitRunner` 失败；本次未修改这些模块或测试运行器配置。上述根目录结果不代表完整设备套件通过。
- 本次证据仅为本地 UI/构建验证，不替代归因规范 C01–C38 的后端、平台或发布证据。

## 账号密码页（2026-11-04）

- 设计来源：[Figma 1:2119](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-2119)。`AccountLoginScreen` 实现圆形 Logo、标题、副标题、账号/密码输入、显隐、渐变提交按钮及底部入口；与欢迎页共享 `EloraLoginOrb`。
- 继续使用现有 `cleanAccountInput`、`cleanPasswordInput`、`validLoginInput`、consent 和 `onAccountLogin`，不修改认证/归因接口。旧设计的 `nexor` 文案改为 Elora。
- 找回密码没有已接入接口，只显示明确的本地不可用说明；Create workspace 说明现有账号注册/登录方式，不发送新请求。
- 已修复审查发现的 Next 焦点顺序问题：显式通过 FocusRequester 聚焦密码框。busy 时禁用字段、显隐与提交，保留返回入口。
- `./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug` 通过。
- API 36 模拟器登录测试集最终 11/11 通过：包含原欢迎页回归，以及有效凭据提交、Next/Done、密码显隐保值、账号 E-201、busy、帮助弹窗、320×480dp / 1.5 倍字体和键盘操作。一次中途 instrumentation 进程退出，完整重跑通过；禁用字段测试最初使用 SetText action 定位导致失败，改为稳定内容描述定位后通过。
- 已查看账号页设备截图；本地截图不入仓库。根目录完整设备套件的其他模块运行器限制仍见上文，不宣称全部模块设备测试通过。本地验证不替代后端/平台/发布证据。

## 2026-11-05 账号页简化

- 按用户划线截图移除 Forgot password? 和底部完整 Don’t have an account? Create workspace 区域，并去掉其专用本地帮助弹窗。
- 保留账号/密码字段、密码显隐、IME 下一项/提交、登录忙碌/错误/同意校验与返回；对应设备用例断言两部分不存在，并回归正常输入与提交。
