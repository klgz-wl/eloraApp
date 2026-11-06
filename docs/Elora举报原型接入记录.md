# Elora 举报确认原型

- 设计：[Figma 1:257](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-257)。`MediaReportSheet` 改为34dp顶圆角的紧凑底部弹层，含拖动条、48dp关闭触达区、居中加粗说明、旗帜图标及紫粉Report胶囊按钮；错误或大字体时允许内容滚动。
- 当前仍使用既有 `ReportSubmissionController` 与 `LocalMediaReportRepository` 本地模拟，未新增或接通后台举报接口；本地完成回调不等于服务端已受理。
- 保留提交中禁用、防重复提交、成功回调和E-701失败重试。关闭按钮、系统返回/弹层dismiss均取消本弹层拥有的Job；从预览信息入口进入、关闭回到预览。上传页举报显示期间暂停底层模板视频，关闭后恢复。
- 资源和无障碍文案在report_strings.xml，颜色在theme/Color.kt，导出图标来源见asset-sources.md；未改举报载荷或错误码契约。

## 2026-11-05 本地验证

- `./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug`通过。
- API36模拟器ReportSheetTest共7/7通过，覆盖成功回调、失败后E-701及重试、关闭不提交、提交中关闭取消且无成功回调、系统Back关闭、预览信息入口与返回、320dp高度约束/1.5字体下Report及Close可滚动到达。
- 已检查真实弹层覆盖预览的fixture截图；只读交叉审查未发现本轮新增阻塞问题，建议补充的取消与大字体场景均已验证。实施前已同步相关实施Skill。
- 未进行真实后端举报受理验收；本地UI/控制器证据不替代平台、后端、发布或归因C01–C38证据。根目录跨模块设备套件的既有AndroidJUnitRunner限制见此前记录，本轮运行相关app测试。

## 举报入口直接确认（2026-11-05）

待上传模板预览在有onReport上下文时，以用户指定的圆形i信息图标和Report content语义直接触发MediaReportSheet，不再经过模板信息AlertDialog。点击图标仅打开确认，点击sheet内Report才调用原提交控制器；没有新增后端举报接口。无举报上下文的纯模板信息入口保留metadata说明。新增/更新设备断言覆盖单次图标点击→确认sheet、无中间Image template、关闭返回，以及既有busy取消/失败重试。实施前已同步实施Skill。

2026-11-05本轮合并验证：根构建/单元/lint与显式app dev/prod单元/lint全部BUILD SUCCESSFUL；限定emulator-5554（API37）执行ReportSheetTest7项、UploadTemplatePreviewTest4项、VideoTemplateCatalogTest7项、GenerationHomeRoutingTest1项，共19/19通过。此前生成模式3项也通过。独立只读复核未发现阻断问题，git diff --check通过；测试只模拟受理/举报，不执行真实生成扣币或远端举报。

## 入口图标纠正

用户明确入口应为截图中的圆形i，不是旗帜。MediaReportIconButton统一固定48dp深色圆形触达区及16dp原导出elora_preview_info；模板预览复用该组件，旧结果页也不再用手绘青色感叹号。Report确认按钮内部的旗帜图标保留。单次点击直达确认sheet的行为不变。实施前已同步Skill，独立只读复核未发现阻断问题。

2026-11-05图标与空分类增量验证：根构建/单元/lint及显式app dev/prod单元/lint全部BUILD SUCCESSFUL；API37模拟器ReportSheetTest7项+UploadTemplatePreviewTest4项+VideoTemplateCatalogTest8项+GenerationHomeRoutingTest1项共20/20通过。真机1b40f92f已保留数据覆盖安装最新dev并启动；图标使用原导出i资源，未新增举报API。
