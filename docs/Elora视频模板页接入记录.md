# Elora 视频模板页接入记录

- 设计：[Figma 1:1404](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-1404)。`ui/imagevideo/TemplateCatalogScreen.kt` 提供视频列表展示；后续图片布局接入也复用该组件，详见 `Elora图片模板页接入记录.md`。
- 顶部 Elora 入口与余额入口沿用充值路由；底部按设计排序为充值入口、视频、图片、个人中心，路由索引仍为 2、0、1、3。Android 系统状态栏/导航栏提供系统信息。
- 卡片只加载真实模板封面，不在列表同时运行视频播放器；点击有效卡片沿用已有模板媒体、报价、生成上下文、analytics 和上传页面。此页不再使用旧的单模板播放/上下翻页控件或独立 USE THIS TEMPLATE 按钮。
- Hot=0→hot，New=1→latest；Trending=2 复用 hot，是现有热度排序的呈现入口，不宣称独立趋势算法。后端分类=3+，使用后端分类名/ID，首次查询、重试、分页都保留同一 ID。空态、加载、错误重试、恢复资料重试及任务状态提示保留。
- 曝光按可见模板去重，在离开前台后重置；接近末尾请求下一页，已有 loading 状态防止并发重复请求，分页失败保留原列表和显式重试。
- 不伪造 VOICE/NEW/余额等元数据，素材来源及测试 fixture 限制见 `asset-sources.md`。

## 2026-11-04 本地验证

- `./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug` 通过；新增 Trending→hot 规则先在旧代码上确认测试失败，再实现并通过。
- API 36 模拟器 `VideoTemplateCatalogTest` + `TemplateErrorStateTest` 共 5/5 通过，覆盖选中标签、真实分类 ID、卡片回调、四导航路由、余额入口、接近末尾分页、防重复请求/曝光、分页失败重试与初次失败重试。
- 已检查使用 Figma 本地 fixture 的设备截图；真实后端图片/分类数量会决定实际呈现内容，未以 fixture 结果声称完成真实账号的端到端生成验收。原依赖开发后端的页面测试已更新入口与断言，但本轮未执行整套联网用例。
- 只读交叉审查未发现阻塞问题。视频非追加加载清空旧列表，避免分类切换请求失败后旧分类卡片被错误展示；分页追加仍保留已有卡片。
- 根目录完整设备测试仍受其他 core/统计集成模块缺少 AndroidJUnitRunner 的已记录限制；上述 5/5 仅指本次相关 app UI 测试。未更改归因或其他公共规范，不替代 C01–C38 后端/平台/发布证据。

## 首页/非首页边界修正

用户确认瀑布流只展示非首页模板：本页现显式home_featured=false，标签为All加后台返回的视频分类，不再合成Hot/Trending/New分类。首页独立为导航4的竖滑视频流，home_featured=true，详见 `Elora首页视频流接入记录.md`；此约定取代上文早期筛选映射和底部充值入口描述。
