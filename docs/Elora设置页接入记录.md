# Elora Settings 接入记录

## 页面与数据

- 按 [Figma 1:1662](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-1662) 实现独立 `SettingsScreen` / `SettingsRoute`，Me 右上齿轮进入，顶部/系统返回恢复 Me 原筛选。根页移除旧资料卡和底部导航，匹配两组设置卡、底部 Delete Account 与紫粉 Sign out。短屏/大字体中间区域可滚动、账号操作保持可达。
- 隐私政策读取 `BuildConfig.PRIVACY_POLICY_URL`；使用条款读取 `TERMS_OF_SERVICE_URL`；版本信息读取 `VERSION_NAME`；联系方式读取 `CONTACT_EMAIL`。这些字段均由现有 config/app.properties 与 config/dev.properties / prod.properties 合并生成；未更改归因版本、应用身份或正式发布确认值。协议地址/联系邮箱为空时不展示对应行，不使用设计中的样例邮箱或 1.0.0。
- 隐私/条款复用登录的 PolicyWebView，使用显式可保存页状态；返回 Settings。关于使用 Elora 名称及真实构建版本，保留历史任务入口；联系弹窗展示完整配置邮箱，复制操作仅写剪贴板，并保留原 Feedback 入口，不自动发送邮件或提交反馈。姓名/头像编辑仍通过 Me 的两个独立编辑入口。
- 退出仍复用原登录会话退出链路；删除仍复用既有 DeleteAccountDialog 三秒倒计时与确认、取消、后端回调。busy 同时禁用退出/删除，错误沿用既有诊断文本，不新增账号存储或 API。

## 验证范围

- `SettingsScreenTest` 使用真实 MeCreationsRoute 加可替换 policy 内容验证齿轮→Settings→协议/关于/联系→返回、配置地址传递、Me Images 筛选保留；另覆盖空配置隐藏、真实版本、账号回调/busy/error、320×480dp 与 1.5 倍字体。
- 截图使用实际 dev flavor 配置。协议 UI 测试以本地内容替代 WebView，未声称真实站点内容验收。测试中的账号回调为计数器，未注销会话或删除真实账号。
- 已更新既有联网 ImageToVideoScreenTest 的设置、任务/反馈入口和姓名编辑断言；本轮确定性设备验证不依赖真实后端认证，实际根页进入远端任务/反馈再返回链路由静态审查补充，未以 fixture 声称完整线上验收。
- 实施前已同步 `/Users/kuailegeziwl/.codex/skills/my-implementation-workflow/SKILL.md` 的 Elora Me 设置页约定，并完成独立只读交叉审查，未发现阻断项。

2026-11-05 最终验证：`./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug :app:testDevDebugUnitTest :app:testProdDebugUnitTest :app:lintDevDebug :app:lintProdDebug` BUILD SUCCESSFUL；app dev/prod 各325项单元通过。API36模拟器 SettingsScreenTest 4项、MeCreationsTest 6项、ProfileCardTest 1项，共11/11通过。git diff --check通过。跨模块connectedDebugAndroidTest既有AndroidJUnitRunner配置限制未在本轮更改，设备证据限定为上述app测试；归因C01–C38平台/后端/发布证据仍独立记录。任务启动的模拟器已关闭，临时截图未加入仓库。

## About / Contact 独立信息页（2026-11-05）

- 按用户新截图，两个入口改为黑底独立页面，顶部纯返回箭头，标题和正文左对齐，内容可滚动；取代原信息AlertDialog及其中历史任务/Feedback按钮。版本信息仍显示在Settings的About行，原账号退出/删除保持。
- About标题为About Elora，正文“Create images and videos with Elora. More information will be available before release.”，名称从app_name带入，不使用旧Vexora名称。
- Contact标题为Contact Us，正文“Contact support using the email address below.”，完整邮箱来自SettingsConfiguration/BuildConfig.CONTACT_EMAIL；当前dev继承common邮箱，prod使用其覆盖邮箱，不写死截图地址。邮箱支持系统文本选择复制，不自动发送邮件。
- SettingsPage仍由rememberSaveable保存，顶部和系统返回回Settings；宿主meSettingsOpen改用稳定startupSession.userId，防止资料异步初始化重置恢复状态。
- SettingsScreenTest覆盖两种子页/返回与Me筛选、配置邮箱、Contact恢复、空配置隐藏和小屏大字体；ImageToVideoScreenTest的旧About/Contact模块断言改为截图内容。测试使用fixture，无真实账号删除或消息发送。
- 实施前已同步实施Skill，独立只读复核另记。About/Contact 与 Me 组合设备用例11/11通过；后续变更后的综合验证另记。

综合验证：设置5项设备测试通过，About/Contact本地截图已核对；根要求构建/单元/Lint与显式两flavor检查通过。dev按原配置邮箱展示，prod使用其覆盖邮箱。
