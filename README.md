# Distraction-Free Minimalist Launcher (Bento & E-Ink Edition)

[![Android CI](https://github.com/owner/distraction-free-launcher/actions/workflows/build.yml/badge.svg)](https://github.com/owner/distraction-free-launcher/actions/workflows/build.yml)
[![GitHub Pages](https://img.shields.io/badge/Guide-GitHub%20Pages-black?logo=github)](https://owner.github.io/distraction-free-launcher/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-green.svg)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20M3-4285F4.svg)](https://developer.android.com/jetpack/compose)

> **Reclaim your attention.** A minimalist, executive Android home launcher blending high-contrast E-Ink aesthetics and modern Bento Grid architecture to eliminate digital clutter, silence unessential interruptions, and foster deep work.

---

## 📖 Live Guide & Documentation Website

Explore the interactive web guide, setup instructions, and feature deep-dives on the GitHub Pages website:
👉 **[View Interactive Guide Website](https://owner.github.io/distraction-free-launcher/)** *(or open `docs/index.html` locally)*

---

## ✨ Core Pillars

### 🍱 1. Bento Grid & E-Ink Design Language
- **Bento Card Hierarchy**: High-contrast, tactile 28dp rounded containers for quick scanning without cognitive overload.
- **Visual Detox**: Replaces flashy, saturated app icons with typography-first squircle tiles and single-glyph monograms.
- **4 E-Ink Palettes**:
  - `Pure Paper`: Crisp carbon text on clean off-white canvas.
  - `Warm E-Paper`: Sepia matte comfort for late-night executive reading.
  - `Charcoal Slate`: Inverted dark e-ink mode for low-light environments.
  - `Brutalist Ink`: Stark, pure black-and-white high-contrast mode.
- **System Grayscale Bridge**: Quick toggle to launch Android's native Bedtime/Monochrome color correction.

### 🛡️ 2. Distraction Shield & Notification Suppressor
- **`NotificationSuppressorService`**: Intercepts and mutes notifications from non-essential apps during active focus sessions.
- **VIP Communication Whitelist**: Direct passthrough for urgent contacts and mission-critical apps (e.g., Slack, Calendar, Phone).
- **Quiet Notification Digest**: Intercepted notifications are silently archived for batched review when your focus block ends.
- **Foreground Shield & Boot Receiver**: Persistent focus state across reboots (`RECEIVE_BOOT_COMPLETED`).

### 🎯 3. Executive Productivity & Daily Priorities
- **Executive Priorities Agenda**: Keep your top 3–5 strategic objectives pinned directly on your home screen with one-tap completion.
- **Focus Mode Engine**: Preset work blocks (*Deep Work 25m*, *Strategic Focus 45m*, *Executive Meeting 60m*, or *Continuous Shield*) with live countdown timers and +15m quick-extensions.
- **Home Screen App Widget**: Dedicated `FocusModeWidgetProvider` for instant toggling from any launcher screen.

### 📊 4. Digital Wellbeing & Analytics
- **Focus Quotient Index**: A 0–100% daily rating calculated from deep work ratio, screen time vs. goal, and pickup frequency.
- **24-Hour Activity Canvas**: Minimalist monochrome hourly chart of device pickups and screen engagement.
- **Per-App Usage Limits**: Set hard 10m / 20m / 45m timers on time-sink apps.

---

## 📱 Architecture & Tech Stack

```
com.example
├── MainActivity.kt               # Entry point, Edge-to-Edge scaffold, Bento Bottom Nav
├── DistractionFreeApp.kt         # Application lifecycle, Notification channels, Service init
├── data/
│   ├── AppPreferencesRepository  # StateFlow-backed local preferences & DataStore
│   └── AppDatabase.kt            # Room database for persistent analytics & task storage
├── model/
│   └── AppModel.kt               # Models for FocusState, AppShortcut, PriorityTask, ThemePreset
├── system/
│   ├── NotificationSuppressorService.kt # Android NotificationListenerService integration
│   ├── BootReceiver.kt           # On-boot service starter
│   └── UsageTracker.kt           # Android UsageStatsManager bridge
├── ui/
│   ├── MainViewModel.kt          # Unified executive state manager
│   ├── launcher/HomeScreen.kt    # Bento Grid home dashboard & status cards
│   ├── drawer/AppDrawerScreen.kt # Filterable app drawer with time limits
│   ├── analytics/WellbeingAnalyticsScreen.kt # 24h usage & Focus Quotient graphs
│   ├── settings/SettingsScreen.kt# E-ink themes, VIP whitelist & permissions
│   ├── components/EInkComponents.kt # Reusable Bento Cards, Squircles & Checkboxes
│   └── theme/                    # Color, Type, Shape & M3 Theme definition
└── widget/
    └── FocusModeWidgetProvider.kt# Android AppWidget quick-toggle provider
```

- **UI Framework**: 100% Jetpack Compose with Material 3
- **Language**: Kotlin 2.0+ with Coroutines & StateFlow
- **Architecture**: Single-Activity MVVM + Unidirectional Data Flow (UDF)
- **Local Persistence**: Room SQLite Database & Jetpack DataStore Preferences
- **System Integrations**: `NotificationListenerService`, `UsageStatsManager`, `AppWidgetProvider`, `BroadcastReceiver`

---

## 🚀 Getting Started & Installation

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17+
- Android SDK 35 (compileSdk 35, minSdk 26)

### Build from Source
```bash
# Clone the repository
git clone https://github.com/owner/distraction-free-launcher.git
cd distraction-free-launcher

# Build debug APK
gradle assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Run Unit Tests
```bash
gradle :app:testDebugUnitTest
```

---

## ⚙️ Android Permissions & Setup

To enable full distraction shielding, grant the following permissions on your device:

1. **Set as Default Launcher**:
   - Go to `Settings > Apps > Default Apps > Home App` and select **Distraction Free Launcher**.
2. **Notification Access (Quiet Digest)**:
   - Go to `Settings > Apps > Special App Access > Device & app notifications` and enable the launcher.
3. **Usage Access (Digital Wellbeing)**:
   - Go to `Settings > Apps > Special App Access > Usage Access` and enable the launcher.
4. **Battery Optimization Exemption**:
   - Exclude the launcher from aggressive battery killing to ensure the background distraction shield stays active.

*(Optional ADB command for automated provisioning in enterprise environments)*:
```bash
adb shell cmd notification allow_listener com.example/.system.NotificationSuppressorService
```

---

## 🤝 Contributing

Contributions are warmly welcomed! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) for development workflows, branch naming rules, and code style guidelines.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
