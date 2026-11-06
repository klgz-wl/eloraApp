# Elora 上传成功后的模式选择与生成

- 设计：[Figma 1:505](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-505)。`GenerationModeScreen` 实现Upload/Mode/Generate时间线、双预览、Rendering Engine选择和带钻石报价的Generate按钮。
- 左侧是本地照片，右侧是所选模板预览，明确标注，不表示已完成增强/生成。图片/视频Fit及播放生命周期沿用既有组件。
- 模板选择上下文保留fastPrice/qualityPrice；只展示后台有非负报价的模式。Standard Fast→fast，Ultra Studio Quality→quality，默认优先quality，缺失时fast，无报价禁用Generate，不套用设计固定价格3，也不创造新模型API。
- 收到非空上传resourceId后才进入此页；上传中或失败继续原可换图/重试表单。模式页返回模板预览，换图打开已实现的选择sheet，新的URI按原上传流程处理。
- Generate从当前选项构造不可变模板提交快照，quality/quotedDiamondCost及原image/video modality/source一致传给原提交逻辑。模板/quality/resource相同时重试复用幂等键，变化时由既有函数产生新键。忙碌时禁止重复提交、模式/换图/返回。
- API仍为既有图片/视频任务multipart请求；任务回调、实际扣费结果、余额不足导航与诊断均保留。旧局部终态事件现在从提交key的quality恢复报价上下文，避免误用模板初始模式。

## 2026-11-04 验证

- `./gradlew --no-configuration-cache testDebugUnitTest assembleDebug lintDebug`通过。
- 模式规则单测覆盖可用报价、负报价排除、默认模式、resource门禁、quality/价格/image/source保留；本地HTTP测试覆盖image/video×fast/quality的multipart字段与header/body幂等键。
- API36模式UI3项+上传前预览/路由4项共7/7通过，覆盖quality默认、价格联动、Generate回调、换图返回、busy禁用、无报价、320×480dp/1.5倍字大小和错误显示，以及原picker取消/结果路径。
- 已查看本地fixture截图并完成只读交叉审查。本轮没有调用真实付费生成，不以本地请求模拟/界面测试声称真实上传、模型服务出图出视频或扣费验收完成。
- 既有跨模块AndroidJUnitRunner限制与归因C01–C38后端/平台/发布验收仍独立记录。实施前已同步相关实施Skill。

## 2026-11-05 余额不足提示

- 本地报价超过余额时先展示 Insufficient credits. Please recharge to continue. 与 E-405，后台明确余额不足保留其安全提示及 E-405；两种分支统一等待 2000ms 再走原充值入口。
- 等待期间 busy 阻止重复生成和编辑；离开组件取消协程，不延迟误跳转。本地不足发生在幂等键与请求创建之前，充值返回仍保留上传照片与模式，不自动重新生成。
- 单元测试验证实际等待不少于2秒、单次路由、取消后无路由；设备用例验证提示可见、Generate禁用与2秒后回调。均不调用真实支付或付费生成。

最终验证：根要求 testDebugUnitTest assembleDebug lintDebug 及显式 app dev/prod 单元、Lint 均 BUILD SUCCESSFUL；两 flavor 各330项单元零失败。API37相关30项设备场景分批验证全部通过（首页/目录9、生成后首页1、模式4、Settings5、登录11）；新增延迟用例使用Compose虚拟时钟，实际延迟与取消另由单元验证。独立只读交叉复核未发现阻塞，git diff --check通过。新版dev已覆盖安装真机并成功启动，未清除数据；未执行真实生成或支付。
