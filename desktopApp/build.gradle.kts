import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}


val appVersion = providers.gradleProperty("appVersion")
    .orElse("0.1.0")
    .get()

val supportedVersion = Regex(
    """^\d+\.\d+\.\d+(?:-(?:alpha|beta|rc)\.\d+)?$"""
)

require(supportedVersion.matches(appVersion)) {
    "Expected appVersion like 0.2.0 or 0.2.0-alpha.1, got '$appVersion'"
}

version = appVersion

// Compose validates every configured native distribution format during project configuration.
// Keep the shared package version acceptable to macOS/Windows, and apply Debian prerelease
// ordering only to the DEB-specific package version.
val packageBaseVersion = appVersion.substringBefore("-")

val linuxPackageVersion =
    appVersion.replaceFirst("-", "~")

val macOSPackageVersion = packageBaseVersion
    .split(".")
    .mapIndexed { index, value ->
        val number = value.toIntOrNull()
            ?: error(
                "Expected numeric appVersion component, got '$appVersion'"
            )

        if (index == 0) {
            number + 1
        } else {
            number
        }
    }
    .joinToString(".")

tasks.register("verifyReleaseVersionPolicy") {
    group = "verification"
    description = "Verifies per-target package version mapping for release builds."

    doLast {
        val expectedDebVersion = appVersion.replaceFirst("-", "~")
        check(packageBaseVersion == appVersion.substringBefore("-")) {
            "Shared package version must stay macOS/Windows-safe; got '$packageBaseVersion' for '$appVersion'."
        }
        check(linuxPackageVersion == expectedDebVersion) {
            "DEB package version should be '$expectedDebVersion', got '$linuxPackageVersion'."
        }
        check(!packageBaseVersion.contains("-") && !packageBaseVersion.contains("~")) {
            "Shared package version must not use prerelease separators: '$packageBaseVersion'."
        }
        check(!macOSPackageVersion.contains("-") && !macOSPackageVersion.contains("~")) {
            "macOS package version must be numeric: '$macOSPackageVersion'."
        }
        check(!packageBaseVersion.contains("-") && !packageBaseVersion.contains("~")) {
            "Windows package version must be numeric semver base: '$packageBaseVersion'."
        }
    }
}

group = "eu.revq"
version = appVersion

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.components.resources)
    implementation(libs.compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.dbus.java.core)
    runtimeOnly(libs.dbus.java.transport.nativeUnixsocket)
    implementation(libs.kotlinx.coroutinesSwing)

    testImplementation(libs.kotlin.test)
}

compose.resources {
    packageOfResClass = "eu.revq.resources"
}


compose.desktop {
    application {
        mainClass = "eu.revq.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.Exe)
            packageName = "RevQ"
            packageVersion = packageBaseVersion

            linux {
                iconFile.set(project.file("src/main/resources/icon-app.png"))
                debPackageVersion = linuxPackageVersion
            }

            macOS {
                bundleID = "eu.revq.desktop"
                dockName = "RevQ"
                iconFile.set(project.file("src/main/resources/icon-app.icns"))
                packageVersion = macOSPackageVersion
                packageBuildVersion = macOSPackageVersion
                // Explicitly disable signing to prevent jpackage exit code 1 on macOS GitHub Actions runners
                signing {
                    sign.set(false)
                }
            }

            windows {
                iconFile.set(project.file("src/main/resources/icon-app.ico"))
                menuGroup = "RevQ"
                shortcut = true
                exePackageVersion = packageBaseVersion
            }
        }

        jvmArgs += listOf(
            "--enable-native-access=ALL-UNNAMED",
            "-Dsun.stdout.encoding=UTF-8",
            "-Dsun.stderr.encoding=UTF-8",
            "-Drevq.app.version=$appVersion",
            "-Drevq.repositoryUrl=https://codeberg.org/melokki/revq"
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions>>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

val isolatedTestHome = layout.buildDirectory.dir("test-user-home")

tasks.withType<Test>().configureEach {
    val testHome = isolatedTestHome.get().asFile
    systemProperty("user.home", testHome.absolutePath)
    doFirst {
        project.delete(testHome)
        testHome.mkdirs()
    }
}
