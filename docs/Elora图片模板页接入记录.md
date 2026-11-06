# Elora 图片模板页布局接入记录

- 设计：[Figma 59:924](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=59-924)。
- 图片与视频目录现在共享 `ui/imagevideo/TemplateCatalogScreen.kt`；图片使用两列、22dp圆角、横向14dp/纵向10dp间距，首排240/211dp、后续240dp卡片。底部高亮Image，使用本次设计导出的活动图片/非活动视频图标；图片卡片不显示视频时长。
- 图片目录改用 Hot/Trending/New 与真实图片分类；Hot/Trending使用现有hot排序（Trending不是独立趋势算法），New使用latest。后端仍是既有模板接口，图片分类ID不会取自视频分类。
- 卡片选择保留 image modality/source、媒体、报价和既有上传生成回调；加载/空态/错误、重试、分页、任务状态和恢复资料重试保持可用。图片全量查询清空旧列表避免错误分类残留，分页追加失败仍保留现有列表。
- 启动请求默认图片为latest；活动目录如果使用其他排序或分类，在可应用startup响应后显式重载当前筛选，避免下一页混用排序。独立请求序号继续避免旧startup覆盖新模板查询。
- 资源来源和限制见 `asset-sources.md`；不虚构NEW标志、年龄或人物简介。

## 2026-11-04 本地验证

- `./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug` 通过。
- API36模拟器相关目录/错误状态测试6/6通过，含图片页面语义、Hot/Trending/New、图片分类ID、Image选中态、图片/视频往返、图片模板点击回调、无视频时长，以及视频目录和分页/错误重试回归。
- 已检查使用本地fixture的设备截图。使用真实开发后端的全套联网UI用例仅更新断言与入口，本轮未执行，不声称完成真实账号/生成链路端到端验收。
- 已完成只读交叉审查并修复启动重试排序覆盖问题；资料不替代归因C01–C38的后端/平台/发布证据。根目录跨模块设备套件的既有AndroidJUnitRunner限制见此前页面接入记录。

## 首页/非首页边界修正

本页属于非首页图片目录，筛选现为All加后台图片分类，保持latest排序，不传home_featured；原Hot/Trending/New映射由此取代。首页为独立竖滑视频流。四个主导航为首页/视频/图片/个人，充值从余额进入，详见 `Elora首页视频流接入记录.md`。
