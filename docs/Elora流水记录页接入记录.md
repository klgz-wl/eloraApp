# Elora 钱包 Record 页面

## 页面与路由

- 按 [Figma 1:827](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-827) 实现混合充值/消耗记录卡片、金额正负及时间，以及右上问号的底部说明弹层。关闭、I Understand、系统返回只关闭说明，不退出列表。
- 首页、视频模板、图片模板右上余额通过独立 onRecord 回调进入 WalletRecords 目的地；返回 Templates 保留 selectedNavigation、分类和模板上下文，三个目录通过按导航分隔的 SaveableStateHolder 保存本地列表位置。Me/充值/Settings不使用这个目录快照，避免改变原设置子页返回策略。
- 会员品牌胶囊仍执行原充值回调；Me Purchase Credits、上传/生成中的充值入口及余额不足充值不改。未增加真实扣款或退款动作。页面统计复用 records 契约。

## 数据与状态

- 使用既有 GET /api/v1/wallet/transactions?page=&page_size=，page_size=20。既有解析返回List，满页继续、短页终止；按id去重，整页重复时终止追加，避免无限重复加载。刷新、追加失败各自可重试，不把错误当成空流水。
- 请求按profileId隔离，刷新取消旧追加任务并递增代际；迟到响应不能覆盖新用户或新刷新。没有把充值页仅5条recent transactions当完整记录。
- 金额沿用income/expense正负语义，转Long避免Int.MIN_VALUE取绝对值溢出；展示两位小数但不把整金币除100。描述优先后台文案，视频/图片类别仅按明确字段识别，未知使用Wallet transaction，不按列表位置伪造媒体类型。日期读取真实created_at，带offset的时间转换为设备时区，无offset保留本地时间；使用兼容API24的严格SimpleDateFormat解析，并归一化微秒精度，不为缺失时间填设计日期。
- 帮助文案按照原型保留3日退款提示并修正拼写/空格，属于展示内容；本轮未新增、调用或验证支付渠道自动退款策略。真实支付成功、到账和退款验收证据应另行记录。
- 加载错误使用唯一ErrorDiagnostic格式边界，列表加载暂按E-501回退、会话失效按既有E-207；尚无流水专属错误码，不声称全覆盖。

## 验证

- WalletRecordPresentationTest覆盖income/expense正负、未知类型、最小整数及时间转换/缺失时间。
- WalletRecordScreenTest覆盖Record与帮助弹层三个关闭方式、来源三个页面余额与会员动作分离、首屏失败重试、分页失败保留记录/原页重试、用户变化迟到响应隔离、加载与成功空态区别。截图/请求均使用fixture，不依赖真实支付。
- 三来源测试组装真实HomeVideoFeedScreen/TemplateCatalogScreen与RecordRoute；宿主根路由通过静态交叉审查补充，不以fixture声称已验证真实后端认证后的完整线上流程。
- 实施前已同步 `/Users/kuailegeziwl/.codex/skills/my-implementation-workflow/SKILL.md` 的Elora钱包Record约定，独立只读交叉审查未发现阻断项。

2026-11-05最终验证：`./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug :app:testDevDebugUnitTest :app:testProdDebugUnitTest :app:lintDevDebug :app:lintProdDebug :app:connectedDevDebugAndroidTest`（设备类限定WalletRecordScreenTest、VideoTemplateCatalogTest、CreditsPurchaseScreenTest）BUILD SUCCESSFUL。dev/prod各327项单元通过；API36模拟器Record4项、模板6项、充值2项共12/12通过；git diff --check通过。跨模块connectedDebugAndroidTest的既有AndroidJUnitRunner配置限制未更改，设备证据限定上述app用例；归因C01–C38平台/后端/发布证据继续独立记录。任务启动模拟器已关闭，截图保留在任务预览目录而非仓库。

## 充值页入口与空态文案更新

2026-11-05：充值页右上历史图标改为进入同一个WalletRecords页面，替代原recent-transactions弹窗；返回时selectedNavigation仍为充值2，选中商品和原单状态由宿主控制器保留，生成余额不足的充值返回目标不修改。Record仅成功加载无流水时展示精确文案“No transactions yet”，错误/加载不展示空态。设备测试覆盖充值入口→Record空态→返回保留所选商品，并回归原有Record与支付恢复用例。

本轮完整命令BUILD SUCCESSFUL：根要求testDebugUnitTest/assembleDebug/lintDebug，显式app dev/prod单元与lint均通过；API36上CreditsPurchaseScreenTest 2项、WalletRecordScreenTest 4项、PaymentRecoveryUiTest 8项共14/14通过，git diff --check通过。首次设备运行因模拟器断连中止，重启后完整复跑通过；未操作真实支付。相关Skill已在实施前同步，独立只读复核未发现阻断问题。
