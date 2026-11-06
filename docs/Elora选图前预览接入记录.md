# Elora 选择本地图片前的模板预览

- 设计：[Figma 1:93](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-93)。对应点击首页视频或非首页图片/视频模板后的媒体展示区，参考echatApp选择本地图片页面上半部分。
- `UploadTemplatePreviewScreen` 展示真实模板标题、34dp外圆角/26dp媒体圆角、图片/视频预览、信息入口和底部Upload Photo按钮；采用系统状态栏，短屏媒体区可滚动，CTA固定可达。复用既有Fit和视频RESIZE_MODE_FIT，不裁切源媒体。
- `UploadPhotoScreen` 以本地图片URI是否为空划分阶段：空/取消停留预览，picker返回非空URI后进入原YOUR PHOTO表单并沿用现有上传/生成流程。报价、幂等、API、归因和错误诊断未更改。
- Upload Photo启动原ACTION_PICK image/*；信息对话框显示真实媒体类型及视频时长，保留举报入口。系统Back与顶部返回均回来源模板页。
- 图片素材与图标来源见asset-sources.md；设计示例只用于测试fixture，不作为生产媒体。

## 2026-11-04 本地验证

- 单元测试覆盖空URI/取消保留预览、非空URI进入选图后阶段，以及既有Fit行为。
- API36模拟器4项UI测试覆盖图片预览/上传回调/信息/举报/返回、视频类型和真实时长显示、禁用CTA、320×480dp和1.5倍字体下CTA可达。
- 其中真实UploadPhotoScreen路由测试通过可控ActivityResultRegistry验证ACTION_PICK的image/*、取消不离开预览、系统Back回调、结果URI进入YOUR PHOTO表单；用故意不可读的本地provider让上传止于本机错误，确认未上传时CONTINUE禁用，不以此声称验证远端上传或实际媒体库授权。
- 已检查本地fixture设备截图；根目录跨模块设备套件的既有AndroidJUnitRunner限制见此前记录，本轮运行相关app测试。真实CDN视频/媒体库/后端生成和归因平台验收仍需独立证据。

最终 `./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug` 与相关4项UI测试均通过；只读交叉审查提出的系统返回缺口已修复并验证。实施前同步了相关实施Skill。

## 照片选择弹层衔接

Upload Photo现先打开Figma78:1828照片选择sheet，+和设备图库入口才调用原系统picker。有效选择与取消仍遵循原阶段边界；详见 `Elora照片选择弹层接入记录.md`。此说明取代上文“按钮直接启动ACTION_PICK”的早期入口描述。

## 上传成功后的下一阶段

上传成功并有resourceId后，现进入Figma1:505模式选择页，再由Generate提交既有图片/视频任务；上传中或失败仍保留原表单与重试。详见 `Elora生成模式页接入记录.md`。
