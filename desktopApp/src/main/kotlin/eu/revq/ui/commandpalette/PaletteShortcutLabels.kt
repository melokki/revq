package eu.revq.ui.commandpalette

data class PaletteShortcutLabels(
    val primaryModifier: String,
) {
    fun quickRun(number: Int): String = "$primaryModifier$number"

    val moveAlternative: String = "${primaryModifier}N/P"
    val clear: String = "${primaryModifier}U"

    fun localize(label: String): String = label
        .replace("Ctrl/⌘+", primaryModifier)
        .replace("Ctrl+", primaryModifier)
}

fun paletteShortcutLabels(
    osName: String = System.getProperty("os.name").orEmpty(),
): PaletteShortcutLabels = PaletteShortcutLabels(
    primaryModifier = if (osName.contains("mac", ignoreCase = true)) "⌘" else "Ctrl+",
)
