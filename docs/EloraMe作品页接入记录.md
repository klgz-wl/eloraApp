# Elora Me 生成作品页

- 设计：[Figma 1:1766](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-1766)。`MeCreationsScreen` 实现真实profile/avatar/balance、Settings入口、Purchase Credits外观、Creations/Videos/Images及两列作品卡片。
- 按用户后续要求恢复Creations下72小时提示展示，仅恢复UI文案与样式，不新增客户端删除策略。生成结果URL为空/null/空白的条目不展示；失败或处理中任务不作为生成作品。
- `MeCreationsRoute` 通过既有generated resources接口查询，固定resource_type=generated，Creations不传modality，Videos传video、Images传image；分页/重试保持筛选。状态与旧MyTasks列表隔离，取消和代际保护避免迟到响应覆盖当前筛选。
- 过滤空URL后某页无有效作品但有下一页时继续读取，直到找到作品或分页耗尽；仅成功且已确认无有效作品时显示用户指定“No Creations yet.”及白色“Create Now”。后者路由首页4。加载、错误、分页失败不会伪装成空态。
- Creations数量仅在All分页耗尽、有效数可确认时显示；未知不使用已加载数量充当总数。筛选时保留最近已确认的All数量。
- 按后续充值页面需求，Purchase Credits现已启用，回调宿主充值入口（替代原disabled原型状态）。资料名/头像仍是两个独立后端编辑入口；原账号设置、反馈、任务记录、退出和删号保留于Settings。
- 点击作品复用现有结果查看/保存页面（图片中央图标打开结果页），顶部和系统返回均回Me。Me筛选与Settings状态提升到宿主，结果/设置/编辑返回保留来源；头像沿用本地优先、远端禁用缓存的更新策略。

## 2026-11-05 验证

- `./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug`通过。
- 单元测试覆盖混合/单媒体筛选、空/空白URL和非成功记录过滤、跨空地址页读取；本地HTTP测试确认generated资源查询的modality、page和resource_type。
- API36模拟器MeCreationsTest 6项+原ProfileCardTest 1项，共7/7通过，含三种筛选、原型充值禁用、资料/设置回调、空态回首页、筛选迟到响应隔离、首屏失败重试、分页失败后同筛选重试、结果页系统返回。
- 已检查有作品/空态两种fixture截图；未以fixture验证声称真实账号后端数据或CDN下载验收完成。跨模块设备套件既有AndroidJUnitRunner限制及归因C01–C38平台/后端/发布证据仍独立记录。
- 实施前已同步相关实施Skill。只读交叉审查提出的结果页系统返回、跨空地址页误报空态及计数问题均已修复并复核。

## 充值入口后续变更

2026-11-05：按用户后续要求启用 Purchase Credits，进入 Elora Tokens 充值页；返回恢复 Me。充值实现及本轮验证见 [充值与支付重试接入记录](Elora充值与支付重试接入记录.md)。上文首次验证的充值禁用状态为历史记录，现由新的启用回调用例替代。

## 设置页后续变更

按后续选中 Figma 1:1662，Settings 现为独立设置根页；协议、条款、版本和邮箱读取现有 flavor 配置，历史任务/反馈分别在关于/联系说明保留入口，资料编辑仍在 Me。见 [设置页接入记录](Elora设置页接入记录.md)。

## 全屏作品查看后续变更

按Figma59:1670，Me来源作品现进入独立全屏CreationViewerScreen，视频初始暂停、图片直接查看，返回保留筛选并支持状态恢复；任务记录来源仍使用旧GenerationResultScreen。详见 [作品查看页接入记录](Elora作品查看页接入记录.md)。

## Creations下提示恢复（2026-11-05）

按当前选中Figma1:1766（提示卡1:1839）恢复紫色隐私提示卡，包含时钟、Auto-deletion enabled、72 hours金色强调以及下载保留访问的完整文案；在筛选下方、作品/空态之前展示，切换筛选保持可见。此需求覆盖早先不显示该提示的视觉约定，服务端保留策略和客户端删除操作未在本轮更改。文字进入string资源、品牌色进入theme；有作品和空态设备用例同步断言提示可见。实施前已同步实施Skill，本轮为静态展示恢复，采用单Agent核对。

2026-11-05本轮验证：根构建/单元/lint及显式app dev/prod单元/lint均BUILD SUCCESSFUL。API37的Me6项通过；额外作品查看本地视频用例遇到c2.goldfish.h264.decoder的queueInputBuffer错误，隔离重跑同样失败，未改播放器来掩盖环境问题。新建独立API36模拟器emulator-5556完整复跑Me6+作品查看3，共9/9通过（同一H264用例通过），并已关闭本轮模拟器，保留原API37设备。真机1b40f92f已保留数据覆盖安装最新dev。图标、完整文案和金色72 hours截图已核对；git diff --check通过。
