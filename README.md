# LaunchAdSkip

[![Android CI](https://github.com/18393526858/skip-launch-ads/actions/workflows/android-ci.yml/badge.svg)](https://github.com/18393526858/skip-launch-ads/actions/workflows/android-ci.yml)

**This project is shared so that more experienced Android developers can help improve its compatibility, safety, and reliability. Issues and pull requests are welcome.**

LaunchAdSkip is a local-only Android utility that uses an AccessibilityService to close recognizable launch ads in third-party apps. It requests no Internet permission and stores all settings and recent events on-device.

## Features

- Detects explicit launch-ad controls such as “跳过广告”, “跳过”, “关闭”, and their English equivalents.
- Supports skip or close controls anywhere on the screen, including full-screen ad pages.
- Actively scans the first 10 seconds after an app launch for dynamic and slow-loading ads.
- Clicks at most once per app launch to reduce accidental repeated actions.
- Attempts to recover from quickly triggered shake-ad redirects to browsers, stores, or shopping apps.
- Provides a master switch to pause all automatic behavior immediately.
- Keeps keyword customization and the built-in test page in an optional advanced screen.
- Shows up to 8 recent events and stores a local history of up to 50 entries.

## How It Works

The app observes accessibility events for user-installed launcher apps. During the initial launch window, it reads the active window's accessibility tree and looks for explicit skip or close labels. It never dispatches coordinate-based taps. The default master switch is disabled, so nothing is automated until the user explicitly enables it.

The accessibility service only processes launches from the home screen or system UI. If the user opens an app from another app, the launch-ad window is not activated.

## Usage

1. Install the APK from the latest GitHub Release or build it locally.
2. Open LaunchAdSkip and enable the “自动跳过” master switch.
3. Grant Accessibility permission in Android settings.
4. Follow the in-app setup guide to allow background running and autostart.
5. Return to the app. No per-app selection is required.
6. Turn the master switch off whenever automatic behavior is not wanted.

### vivo / OriginOS

Background settings vary by system version. In the in-app setup guide, use the provided shortcuts and check:

- Battery > Background power consumption management > allow high background power use.
- iManager > App management > Permission management > Autostart.
- Lock LaunchAdSkip in the recent-apps screen when required by the system.

## Privacy

- The app does not request `android.permission.INTERNET`.
- Application lists, keywords, and event history are not uploaded.
- Settings and recent events are stored only in the local app DataStore.
- When the master switch is disabled, the service does not read or act on foreground app content.

## Limitations

- Accessibility trees are controlled by each app. Canvas/WebView text, hidden controls, secure screens, and ads without an accessible close action may not be detected.
- Apps can change their launch-ad UI at any time, so selectors and keywords require ongoing compatibility work.
- A regular Android app cannot block another app from reading the accelerometer. Shake-ad protection is a best-effort redirect recovery, not a sensor-level blocker.
- The project does not use root, VPN, coordinate taps, or modifications to other apps.
- Running multiple ad-skipping or auto-click accessibility services at the same time can cause duplicate clicks.
- Device vendors may apply additional background restrictions.

## Build

Requirements:

- JDK 21
- Android SDK Platform 35
- Android SDK Build Tools 35.0.0

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

GitHub Actions runs the same tests, lint checks, and debug build for pushes and pull requests.

## Release APK

GitHub Release artifacts are debug-signed for personal sideloading. They are not intended for app-store distribution. A locally built debug APK may use a different debug signing key, so switching between locally built and CI-built debug APKs may require uninstalling the previous package.

## Contributing

Compatibility improvements are especially valuable. Useful contributions include:

- New safe launch-ad patterns with reproducible test cases.
- Improvements for specific Android vendors and accessibility implementations.
- Tests for false positives, full-screen ads, localizations, and background behavior.
- Privacy, accessibility, and documentation review.

Please keep reports free of personal information. When filing an issue, include the Android version, device vendor, app version, expected behavior, and a minimal description of the accessibility structure. Do not attach sensitive screenshots or private app data.

## License

MIT. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
