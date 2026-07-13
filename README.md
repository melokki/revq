<p align="center">
  <img src="./revq-icon-master.svg" alt="RevQ application icon" width="220" />
</p>

This is a Kotlin Multiplatform project targeting Desktop (JVM).

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
      folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and
options:

- Desktop app:
    - Hot reload: `./gradlew :desktopApp:hotRun --auto`
    - Standard run: `./gradlew :desktopApp:run`

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Desktop tests: `./gradlew :desktopApp:test`
- Shared JVM tests: `./gradlew :shared:jvmTest`

### Desktop packaging notes

RevQ packages platform-specific application icons (`.png` on Linux, `.icns` on
macOS, and `.ico` on Windows) and uses dedicated monochrome tray assets at
runtime. Linux tray availability still depends on the desktop environment
exposing the AWT system-tray/status-notifier bridge; RevQ continues without a
tray when that bridge is unavailable.

`revq-icon-master.svg` is the presentation master used by this README and other
marketing surfaces. `revq-icon-packaging.svg` is the packaging master used for
Linux, macOS, Windows, and light/dark tray exports. Regenerate every
application asset with:

```shell
python3 desktopApp/scripts/generate_icons.py
```

The generator requires Pillow and a Chromium-based browser. Set
`REVQ_SVG_RENDERER` when the browser executable is not named `google-chrome`,
`chromium`, or `chromium-browser`.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
