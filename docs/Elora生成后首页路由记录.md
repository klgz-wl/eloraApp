# Elora 生成后返回首页与任务标签

## 行为（2026-11-05）

- Generate只有成功创建/受理任务后才进入首页（导航4、Templates），点击或等待请求阶段仍在生成页；不先展示My Tasks或Record。失败、报价缺失、余额不足与重试分支沿用原处理。
- handleGenerationTaskCreated先保留sessionTasks，再重置selectedTab/currentTemplate与结果来源，切到首页后显式reloadTemplates(Video, hot)，确保请求home_featured=true，不复用Video菜单home_featured=false的数据。已有请求取消/serial与启动响应保护继续避免迟到覆盖。
- Generating.../Completed通过TemplateTaskStatusBadges按导航4守卫；首页正常媒体、加载中及空模板都可展示真实任务标签，Video0/Image1不展示。归因资料重试、列表加载/空态/错误提示仍保留。
- 原任务持久化、后台轮询、成功/失败事件、扣币去重和权威余额刷新不变。结果仍由Me访问；旧任务历史入口保留，本轮只去掉生成后的中间跳转。

## 验证

- BottomNavigationRoutingTest验证所有来源都回首页；GenerationHomeRoutingTest模拟受理成功，检查受理前不跳转、受理后首页与Generating、终态Completed、Video/Image隐藏及返回Home恢复显示。模拟器验证不执行真实上传/生成或扣币。
- 设备用例组装真实GenerationModeScreen/HomeVideoFeedScreen/TemplateCatalogScreen与导航函数；不依赖后端认证。根页面任务回调、home_featured请求和后台轮询由只读交叉审查确认，未把fixture声明为真实后端全链路验收。
- 实施前同步/Users/kuailegeziwl/.codex/skills/my-implementation-workflow/SKILL.md；独立复核未发现阻断问题。

2026-11-05最终验证：根要求testDebugUnitTest/assembleDebug/lintDebug，以及显式app dev/prod单元与lint均BUILD SUCCESSFUL。ANDROID_SERIAL=emulator-5554限定API37模拟器，GenerationHomeRoutingTest1项+GenerationModeScreenTest3项+VideoTemplateCatalogTest6项共10/10通过，未在真机执行清数据的设备测试；git diff --check通过。首次设备报告仅因新增断言遗漏已有Generating...省略号失败，修正后完整重跑通过。

## 首页与目录交互补充

- AI Studio为静态标签，没有clickable/导航动作。
- Video/Image加载时保持目录头、分类与底导航，用无交互双列骨架代替旧Loading live templates整页卡；无障碍仍宣布Loading templates。加载成功、空态、错误/重试和分页各自处理。
- 这两项与生成成功回首页、任务标签仅首页一起回归；没有把后台空模板伪造成已有模板。

2026-11-05本轮合并验证：根构建/单元/lint与显式app dev/prod单元/lint全部BUILD SUCCESSFUL；限定emulator-5554（API37）执行ReportSheetTest7项、UploadTemplatePreviewTest4项、VideoTemplateCatalogTest7项、GenerationHomeRoutingTest1项，共19/19通过。此前生成模式3项也通过。独立只读复核未发现阻断问题，git diff --check通过；测试只模拟受理/举报，不执行真实生成扣币或远端举报。

## Video/Image空分类

按用户补充，成功返回空模板且无错误时，目录内容区直接留白，不显示No live templates、Loading live templates或RETRY卡片。加载继续使用双列骨架，真实加载失败仍提示错误及重试，分类和底导航始终保留。新增两种modality空分类设备用例。

2026-11-05图标与空分类增量验证：根构建/单元/lint及显式app dev/prod单元/lint全部BUILD SUCCESSFUL；API37模拟器ReportSheetTest7项+UploadTemplatePreviewTest4项+VideoTemplateCatalogTest8项+GenerationHomeRoutingTest1项共20/20通过。真机1b40f92f已保留数据覆盖安装最新dev并启动；图标使用原导出i资源，未新增举报API。
