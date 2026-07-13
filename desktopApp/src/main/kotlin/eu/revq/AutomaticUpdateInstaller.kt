package eu.revq

import java.nio.file.Files
import java.nio.file.Path

data class AutomaticUpdatePlan(
    val script: String,
    val arguments: List<String>,
)

fun planAutomaticUpdateInstall(
    request: UpdateInstallRequest,
    statusPath: Path,
): AutomaticUpdatePlan = when (request.platform) {
    UpdatePlatform.LinuxX64 -> AutomaticUpdatePlan(
        script = linuxUpdateHelper,
        arguments = listOf(
            request.launchContext.processId.toString(),
            request.path.toString(),
            statusPath.toString(),
        ) + request.launchContext.restartCommand,
    )

    UpdatePlatform.MacArm64,
    UpdatePlatform.MacX64 -> AutomaticUpdatePlan(
        script = macUpdateHelper,
        arguments = listOf(
            request.launchContext.processId.toString(),
            request.path.toString(),
            statusPath.toString(),
            macApplicationBundle(request.launchContext.restartCommand),
        ),
    )

    UpdatePlatform.WindowsX64,
    UpdatePlatform.Unsupported -> throw IllegalArgumentException(
        "Automatic update installation is not supported on ${request.platform}.",
    )
}

class AutomaticUpdateInstallStrategy(
    private val statusPath: Path,
) : UpdateInstallStrategy {
    override fun launch(request: UpdateInstallRequest) {
        require(Files.isRegularFile(request.path)) { "The verified update package no longer exists." }
        require(request.launchContext.restartCommand.isNotEmpty()) {
            "RevQ could not determine how to restart after the update."
        }

        val helperPath = Files.createTempFile("revq-update-helper-", ".sh")
        Files.createDirectories(statusPath.parent)
        Files.deleteIfExists(statusPath)
        val plan = planAutomaticUpdateInstall(request, statusPath)
        Files.writeString(helperPath, plan.script)

        ProcessBuilder(listOf("/bin/sh", helperPath.toString()) + plan.arguments)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
    }
}

private fun macApplicationBundle(restartCommand: List<String>): String {
    val executable = restartCommand.firstOrNull()
        ?: throw IllegalArgumentException("RevQ could not determine how to restart after the update.")
    val marker = "/Contents/MacOS/"
    val markerIndex = executable.indexOf(marker)
    require(markerIndex > 0) { "RevQ is not running from a macOS application bundle." }
    return executable.substring(0, markerIndex)
}

private val linuxUpdateHelper = """
    #!/bin/sh
    set -u
    PATH=/usr/bin:/bin
    export PATH

    pid="${'$'}1"
    package="${'$'}2"
    status="${'$'}3"
    shift 3

    while kill -0 "${'$'}pid" 2>/dev/null; do
        sleep 1
    done

    if [ -x /usr/bin/pkexec ] && /usr/bin/pkexec /usr/bin/dpkg --install "${'$'}package"; then
        printf '%s\n' success > "${'$'}status"
    else
        printf '%s\n' failure > "${'$'}status"
    fi

    nohup "${'$'}@" >/dev/null 2>&1 &
    rm -f "${'$'}package" "${'$'}0"
""".trimIndent()

private val macUpdateHelper = """
    #!/bin/sh
    set -u

    pid="${'$'}1"
    dmg="${'$'}2"
    status="${'$'}3"
    target_app="${'$'}4"

    while kill -0 "${'$'}pid" 2>/dev/null; do
        sleep 1
    done

    mount_dir="${'$'}(/usr/bin/mktemp -d /tmp/revq-update.XXXXXX)"
    mounted=false
    update_succeeded=false

    cleanup() {
        if [ "${'$'}mounted" = true ]; then
            /usr/bin/hdiutil detach "${'$'}mount_dir" -quiet >/dev/null 2>&1 || true
        fi
        /bin/rm -rf "${'$'}mount_dir"
        /bin/rm -f "${'$'}dmg" "${'$'}0"
    }
    trap cleanup EXIT

    if /usr/bin/hdiutil attach -nobrowse -readonly -mountpoint "${'$'}mount_dir" "${'$'}dmg" >/dev/null; then
        mounted=true
        source_app=""
        for candidate in "${'$'}mount_dir"/*.app; do
            if [ -d "${'$'}candidate" ]; then
                source_app="${'$'}candidate"
                break
            fi
        done
        target_parent="${'$'}(/usr/bin/dirname "${'$'}target_app")"
        staging_app="${'$'}target_app.update"

        if [ -n "${'$'}source_app" ]; then
            if [ -w "${'$'}target_parent" ]; then
                /bin/rm -rf "${'$'}staging_app" && \
                    /usr/bin/ditto "${'$'}source_app" "${'$'}staging_app" && \
                    /bin/rm -rf "${'$'}target_app" && \
                    /bin/mv "${'$'}staging_app" "${'$'}target_app" && \
                    update_succeeded=true
            else
                /usr/bin/osascript - "${'$'}source_app" "${'$'}target_app" "${'$'}staging_app" <<'APPLESCRIPT'
    on run argv
        set sourceApp to item 1 of argv
        set targetApp to item 2 of argv
        set stagingApp to item 3 of argv
        set installCommand to "/bin/rm -rf " & quoted form of stagingApp & " && /usr/bin/ditto " & quoted form of sourceApp & " " & quoted form of stagingApp & " && /bin/rm -rf " & quoted form of targetApp & " && /bin/mv " & quoted form of stagingApp & " " & quoted form of targetApp
        do shell script installCommand with administrator privileges
    end run
    APPLESCRIPT
                if [ "${'$'}?" -eq 0 ]; then
                    update_succeeded=true
                fi
            fi
        fi
    fi

    if [ "${'$'}update_succeeded" = true ]; then
        printf '%s\n' success > "${'$'}status"
    else
        printf '%s\n' failure > "${'$'}status"
    fi

    /usr/bin/open "${'$'}target_app"
""".trimIndent()
