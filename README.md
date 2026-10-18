# 开屏跳过 / LaunchAdSkip

[![Android CI](https://github.com/18393526858/launch-ad-skip/actions/workflows/android-ci.yml/badge.svg)](https://github.com/18393526858/launch-ad-skip/actions/workflows/android-ci.yml)

[中文](#中文) | [English](#english)

Privacy-first Android launch-ad skipper powered by AccessibilityService.

## 中文

一个仅在本机运行的 Android 开屏广告跳过工具。总开关默认关闭；主动打开并完成一次辅助功能授权后，应用才会处理从桌面启动的第三方应用。

### 功能

- 自动识别启动页中的“跳过广告 / 跳过 / 关闭”等明确控件。
- 支持任意位置的跳过按钮以及全屏广告页。
- 启动后主动轮询扫描 10 秒，提高动态广告和慢速加载页面的识别率。
- 每次应用启动最多点击一次，避免连续误操作。
- 识别“摇一摇”提示后，尝试拦截快速跳到浏览器、应用商店或电商 App 的行为。
- 首页总开关可随时暂停全部自动操作。
- 关键词可在“高级设置”中调整；最近记录最多展示 8 条，本地最多保存 50 条。

### 使用

1. 安装 `app/build/outputs/apk/debug/app-debug.apk`。
2. 打开应用，打开首页“自动跳过”总开关，并按提示启用系统辅助功能。
3. 按首页右上角“设置说明” 后台耗电、自启动和最近任务锁定。
4. 返回应用即可，无需逐个选择 App 或修改规则；暂时不用时直接关闭首页总开关。
5. 需要调整关键词或运行内置测试时，再进入右上角“高级设置”。

如果退出应用后辅助功能经常被系统关闭，不要从最近任务上滑强制结束应用，并按“设置说明”允许自启动、后台高耗电和电池优化例外。

### 构建

环境要求：JDK 21、Android SDK Platform 35、Build Tools 35.0.0。

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

APK 输出：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 隐私

- 不申请 `INTERNET` 权限。
- 不上传应用列表、规则或跳过记录。
- 所有配置和最近记录仅保存在本机 DataStore 中。
- 辅助功能服务只处理从桌面或系统界面启动的目标应用，总开关关闭时不会读取或操作页面。

### 边界

- 不建立 VPN，不修改其他 App，不使用 root 或坐标模拟点击。
- 仅处理辅助功能节点中暴露了安全关键词的控件。Canvas/WebView 自绘按钮、隐藏控件、`FLAG_SECURE` 页面和无跳过入口的广告无法保证处理。
- 普通应用无法阻止其他 App 读取加速度传感器。检测到“摇一摇”提示后，如果启动页迅速跳到浏览器、应用商店或电商 App，应用会尝试自动返回；该增强保护不能保证覆盖所有跳转。
- 建议只启用一个广告跳过或自动点击类辅助功能；同时运行多个同类服务可能造成重复点击。
- 全屏广告出现后，只要页面包含明确的“关闭 / 跳过 / 跳过广告”控件就会尝试点击；普通页面、广告本体和无明确关闭语义的控件不会被点击。
- OEM 后台限制可能要求用户额外允许辅助功能服务自启动或关闭省电限制。

### 素材

界面中的 3D 盾牌、警告和闪光素材来自 Microsoft Fluent Emoji，标题字体使用 OFL 许可的站酷庆科黄油体。完整归属信息见 `THIRD_PARTY_NOTICES.md`。

### 许可证

本项目采用 MIT License，详见 `LICENSE`。第三方素材与字体许可见 `THIRD_PARTY_NOTICES.md`。

## English

LaunchAdSkip is a local-only Android utility that uses an AccessibilityService to close recognizable launch ads in third-party apps. It requests no Internet permission and stores all settings and recent events on-device.

The master switch is disabled by default. After enabling it and granting accessibility permission, the app scans a target app's initial screen for explicit labels such as “跳过广告”, “跳过”, or “关闭”. It never taps coordinate positions or arbitrary page controls.

### Build

Requirements: JDK 21, Android SDK Platform 35, and Build Tools 35.0.0.

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### Limitations

Sensor-based shake ads cannot be blocked directly by a regular Android app. Canvas/WebView ads, hidden controls, secure screens, and ads without an accessible close action may not be handled. Several OEMs apply additional background restrictions that must be configured manually.

### License

MIT. See `LICENSE` and `THIRD_PARTY_NOTICES.md`.
