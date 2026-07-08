import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}


val appVersion = providers.gradleProperty("appVersion")
    .orElse("0.1.0")
    .get()
val macOSPackageVersion = appVersion
    .split(".")
    .mapIndexed { index, value ->
        val number = value.toIntOrNull()
            ?: error("Expected numeric appVersion component, got '$appVersion'")
        if (index == 0) number + 1 else number
    }
    .joinToString(".")
group = "eu.revq"
version = appVersion

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.kotlinx.coroutinesSwing)

    testImplementation(libs.kotlin.test)
}


compose.desktop {
    application {
        mainClass = "eu.revq.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.Exe)
            packageName = "RevQ"
            packageVersion = appVersion

            linux {
                iconFile.set(project.file("../icon.png"))
            }

            macOS {
                bundleID = "eu.revq.desktop"
                dockName = "RevQ"
                iconFile.set(project.file("../icon.png"))
                packageVersion = macOSPackageVersion
                packageBuildVersion = macOSPackageVersion
                // Explicitly disable signing to prevent jpackage exit code 1 on macOS GitHub Actions runners
                signing {
                    sign.set(false)
                }
            }
        }

        jvmArgs += listOf(
            "--enable-native-access=ALL-UNNAMED",
            "-Dsun.stdout.encoding=UTF-8",
            "-Dsun.stderr.encoding=UTF-8"
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
