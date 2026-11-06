# Elora 充值页面与支付重试

## 实施范围（2026-11-05）

- 按 [Figma 59:410](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=59-410) 实现 `CreditsPurchaseScreen`：返回、真实余额、历史入口、三列套餐、选中高亮、优惠/原价/现价及固定购买按钮。商品来自 StoreCatalog；消费型额度文案为 Purchase，仅真实订阅商品为 Subscribe。历史入口展示现有钱包流水，不伪造支付订单历史。
- Me 的 Purchase Credits 进入同一充值页；余额入口仍可进入。返回恢复来源页面。图片/视频 Generate 在可靠本地报价超过余额时先展示提示2秒后引导充值，后台明确拒绝余额不足也引导同页。未创建任务前的本地判断不会产生生成请求。
- 充值流程保存用户隔离的生成模板、媒体、上传资源、模式及报价；返回继续由用户点击 Generate，不自动再次生成。仅明确到账、余额刷新满足既有返回条件后沿用自动返回策略。
- 复用现有 PaymentCoordinator / PaymentEngine / PurchaseRouter、会话归属、持久化原单和 SDK 集成；页面根据真实支付状态决定可购买/可重试。初始化期间固定所选商品、阻止切换和重复点击。
- Google Play 本地 SDK 不明失败不再直接终结订单：保持原 request/order，查询后台；查询失败可继续同单确认，后台未终结前不能新购或再次拉起 Play。已付款但未发币只确认履约。明确取消/不可用路径保持既有策略，真实后台终态才允许后续新购。
- 三方支付沿用现有原单恢复/初始化路由，以及关闭、超时、网络失败后的同单重试；恢复商品已下架、目录为空/失败时仍显示原商品金额与重试。回调、SDK消费完成和本地成功提示均不代替服务端付款/发币确认。

## 验证范围

- 测试命令：`./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug`，并在 API36 模拟器执行 `:app:connectedDevDebugAndroidTest` 的 CreditsPurchaseScreenTest、TopUpCardTest、PaymentRecoveryUiTest、MeCreationsTest。
- 单元用例覆盖本地报价与类型化余额不足拒绝、模板状态序列化、官方 SDK 失败后保留/查询原单、后台 pending 时禁止重新拉起/新购，以及后台终态后新购。原有官方/三方恢复、会话归属、报价和履约测试继续执行。
- 设备用例覆盖套餐选中/购买回调、历史/返回、生成模式跨充值与 saved-state 恢复、待确认/成功状态、下架或无目录原单恢复、网络重试及 Me 充值入口。截图使用本地 fixture 商品，并非真实价格或支付凭证。
- 跨模块 connectedDebugAndroidTest 既有 AndroidJUnitRunner 配置限制没有在本轮更改；设备证据限定于上述 app 测试。归因 C01–C38 后端/平台/发布证据仍需独立维护。
- 本轮未进行真实扣款；正式 Google Play 的分发、签名、SKU、账户及第三方渠道配置，以及真实订单支付→回调→到账证据仍需平台联调验收。

最终组合命令 BUILD SUCCESSFUL：根命令构建/单元/lint、显式 app dev/prod 各 325 项单元和两套 lint 均通过；API36 设备用例 21/21 通过。修正历史启动图标测试仍使用 Zorv 来源的问题；Me 文案配置读取已改为 stringResource + rememberUpdatedState。git diff --check 通过。

支付改动已经独立只读交叉复核，未发现阻断项；复核未操作真实支付。

## 历史入口后续变更

按后续用户要求，右上历史图标现直接进入独立Record页面，替代原钱包流水弹窗；空态为“No transactions yet”。返回恢复充值来源与原选择，原订单和付款状态仍由既有控制器持有。详见 [流水记录页接入记录](Elora流水记录页接入记录.md)。

## 2026-11-05 取消支付后切换金额

- Play 明确 CANCELLED 回调与用户关闭三方收银台，保存 userCancelled 证据。仅已初始化、CLOSED 且没有错误的主动取消单允许选择其他套餐；官方不可用、程序性启动失败、超时、网络不明、已支付待发币继续按原门禁处理。
- 不同金额走所选套餐的恢复查询/新建单；同金额保留原单重试。旧单没有伪造为服务端取消或删除，仍留在持久订单列表中接受迟到通知/查单。服务端 recover 返回仍优先遵循后台真实订单，不凭界面取消伪造交易终态。
- 套餐目录刷新与取消单状态更新不强制选回旧金额。恢复与再次购买先全局处理不可替换的未决单；旧取消单的迟到 pending 通知即使更新存储顺序，也不能遮蔽新支付订单。
- 新增官方/三方取消→切换金额→Purchase设备用例；core单元覆盖取消恢复、同金额重试、不同金额新单、旧单迟到成功不覆盖当前页、迟到pending重排后恢复、网络关闭失败/待发币/非用户关闭不可替换。本地fixture未发起真实支付或发币。
- 实施前已同步实施Skill；只读交叉检查发现的多未决订单恢复优先级问题已修复。最终验证：根要求 testDebugUnitTest assembleDebug lintDebug、显式两flavor app单元与Lint均BUILD SUCCESSFUL；core458项、app dev/prod各331项单元零失败，API37充值相关9项设备测试零失败。git diff --check通过，dev已覆盖安装真机并成功启动，保留原数据。

## 2026-11-05 真机历史关闭单兼容

- 真机只读诊断确认已安装APK与当前构建SHA一致；页面显示Checkout closed / TRY AGAIN，现存官方支付记录为CLOSED、无error、缺少userCancelled字段，因此上一轮只识别显式true仍锁金额。未清除或直接修改设备支付账本。
- 新增真实旧JSON无字段回归用例并先观察失败。userCancelled改为可空证据：null代表历史缺失，false为明确非用户取消，true为用户取消。旧官方CLOSED且无错误、不需恢复确认时释放选择；官方历史CLOSED产生于SDK取消/未拉起购买或READY恢复，不等同已支付。旧三方、不明失败、超时、待发币不按此兼容放开。
- 显式false必须编码保存，避免重启后因默认值被省略变成null。新增序列化往返+恢复门禁，以及旧官方关闭后换金额的设备场景。后续真机只验证选中不同套餐，不触发购买。

兼容最终验证：根要求命令及显式两flavor单元/Lint BUILD SUCCESSFUL，充值设备用例10/10通过；dev覆盖安装真机并成功启动。尝试真机只点选套餐时，MIUI拒绝ADB输入注入（INJECT_EVENTS），因此未完成真机实际点选验证，未点击购买、未清数据或修改账本。
