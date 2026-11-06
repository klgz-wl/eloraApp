# Elora 照片选择底部弹层

- 设计：[Figma 78:1828](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=78-1828)。`LocalPhotoPickerSheet` 覆盖模板媒体预览，含拖动条、访问提示、关闭、Photos/Albums、选项菜单、Recent、虚线加号和缩略图选中标记。
- Upload Photo/换图先打开sheet，+或设备图库菜单沿用原ACTION_PICK image/*。系统图库取消保留sheet；关闭或首次系统Back仅关闭sheet，后续Back返回模板来源。
- Recent仅来自本流程用户已选择URI，当前页面会话内保存、按最新去重并限制12项；不扫描MediaStore，不新增全库照片权限，不将设计示例当作用户照片。首次没有记录时只显示添加入口和空态。
- Albums展示已选图片分组，并提供原系统图库浏览入口；完整设备相册仍由系统界面提供。选项清除只移除会话历史，不删除设备文件或清除已上传资源。
- 有效系统选择或点击已有缩略图共用acceptSelectedPhoto：关闭sheet、更新URI/历史、清除旧资源ID/幂等上下文、继续既有上传。取消不重置；上传忙碌时不能另开选择。背景模板视频在sheet期间暂停。
- 上传/生成API、报价、错误码边界与任务流程未改变，素材来源见asset-sources.md。

## 2026-11-04 本地验证

- `testDebugUnitTest assembleDebug lintDebug`通过；历史去重/上限/空值有单元测试。
- API36相关sheet和预览测试6/6通过，覆盖Photos/Albums、缩略图选中态及真实URI回调、设备图库入口、清空记录与关闭；真实UploadPhotoScreen测试覆盖image/*、系统图库取消保留sheet、第一次Back关闭sheet/第二次Back回来源，以及返回URI进入原表单。
- 测试图库结果使用故意不可读的本地provider，使上传止于本机错误并验证CONTINUE禁用；未以此声明真实媒体库授权或后端上传验收通过。
- 已完成只读交叉审查，未发现阻塞；截图使用已记录的本地fixture。跨模块设备测试的既有AndroidJUnitRunner限制仍见此前记录，本轮运行相关app测试，不替代归因C01–C38后端/平台/发布验收。
