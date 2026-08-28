# Contributing to Distraction-Free Launcher

Thank you for your interest in improving the **Distraction-Free Minimalist Launcher**! We welcome bug fixes, performance improvements, UI refinements, and documentation enhancements.

---

## 🧭 Code of Conduct
We are committed to providing a friendly, safe, and welcoming environment for everyone, regardless of experience level.

---

## 🛠️ Development Setup

1. **Fork and clone** the repository:
   ```bash
   git clone https://github.com/<your-username>/distraction-free-launcher.git
   cd distraction-free-launcher
   ```
2. **Open in Android Studio**:
   - Select "Open an Existing Project" and choose the project directory.
   - Ensure JDK 17 is selected in `Settings > Build, Execution, Deployment > Build Tools > Gradle`.
3. **Verify Build**:
   ```bash
   gradle assembleDebug
   ```
4. **Run Unit Tests**:
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 🎨 Design & Code Principles

- **Minimalism & Calm**: Avoid flashy animations, skeuomorphic graphics, high-saturation accents, or gamification that triggers dopamine loops.
- **Bento Grid Layout**: Maintain generous 28dp card corner radiuses, clean border outlines (1dp/1.5dp), and spacious padding on an 8dp grid.
- **E-Ink & Low Contrast Safety**: All UI elements must maintain high legibility across all 4 themes (`Pure Paper`, `Warm E-Paper`, `Charcoal Slate`, `Brutalist Ink`).
- **Compose Best Practices**:
  - Prefer stateless composables where possible.
  - Hoist state up to `ViewModel`.
  - Always assign `Modifier.testTag("snake_case_name")` to interactive elements.
  - Maintain a minimum touch target size of 48dp.

---

## 🔀 Pull Request Process

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Write clean, idiomatic Kotlin code following the Android Kotlin Style Guide.
3. Commit with clear, descriptive commit messages (Conventional Commits preferred):
   - `feat: add custom focus preset duration selector`
   - `fix: correct hourly usage binning in 24h analytics`
   - `docs: update adb permission guide on website`
4. Push your branch and open a Pull Request against `main`.
5. Ensure all CI checks pass on GitHub Actions.

---

## 🐞 Reporting Issues

- Search existing issues to prevent duplicates.
- Use our **Bug Report** or **Feature Request** templates.
- Include device model, Android OS version, and reproducible steps where applicable.
