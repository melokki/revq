package eu.revq

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal enum class ShortcutKeycapTone {
    Neutral,
    Accent,
    OnAccent,
}

internal data class ShortcutChord(
    val keys: List<String>,
)

internal data class ShortcutKeycapPresentation(
    val alternatives: List<ShortcutChord>,
) {
    val accessibilityLabel: String = alternatives.joinToString(" or ") { chord ->
        chord.keys.joinToString(" plus ", transform = ::accessibleKeyName)
    }
}

internal fun shortcutKeycapPresentation(
    shortcut: String,
    osName: String = System.getProperty("os.name").orEmpty(),
): ShortcutKeycapPresentation {
    val localized = localizeShortcutLabel(shortcut.trim(), osName)
    val alternatives = when (localized.replace(" ", "")) {
        "↑↓", "↑/↓" -> listOf("↑", "↓")
        else -> localized
            .split(Regex("\\s*/\\s*"))
            .filter(String::isNotBlank)
    }
        .let(::expandSharedPrefixes)
        .map { alternative ->
            ShortcutChord(
                keys = alternative
                    .split('+')
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .map(::displayKeyName),
            )
        }
        .filter { it.keys.isNotEmpty() }

    return ShortcutKeycapPresentation(
        alternatives = alternatives.ifEmpty {
            listOf(ShortcutChord(listOf(shortcut.trim())))
        },
    )
}

private fun localizeShortcutLabel(shortcut: String, osName: String): String {
    val isMac = osName.contains("mac", ignoreCase = true) ||
        osName.contains("darwin", ignoreCase = true)
    val primary = if (isMac) "⌘+" else "Ctrl+"

    return shortcut
        .replace("Ctrl/⌘+", primary)
        .replace("Primary+", primary)
        .replace("Cmd+", "⌘+")
        .let { label ->
            if (isMac) label.replace(Regex("(?i)Ctrl\\+"), "⌘+") else label
        }
}

private fun expandSharedPrefixes(alternatives: List<String>): List<String> {
    if (alternatives.size != 2) return alternatives

    val first = alternatives.first().trim()
    val second = alternatives.last().trim()
    return when {
        first.equals("Page Up", ignoreCase = true) && second.equals("Down", ignoreCase = true) ->
            listOf("Page Up", "Page Down")

        else -> alternatives
    }
}

private fun displayKeyName(raw: String): String = when {
    raw.length == 1 && raw[0].isLetter() -> raw.uppercase()
    raw.equals("escape", ignoreCase = true) -> "Esc"
    raw.equals("return", ignoreCase = true) -> "Enter"
    raw.equals("command", ignoreCase = true) -> "⌘"
    raw.equals("control", ignoreCase = true) -> "Ctrl"
    else -> raw
}

private fun accessibleKeyName(key: String): String = when (key) {
    "Esc" -> "Escape"
    "⌘" -> "Command"
    "Ctrl" -> "Control"
    "↑" -> "Up arrow"
    "↓" -> "Down arrow"
    "←" -> "Left arrow"
    "→" -> "Right arrow"
    else -> key
}

@Composable
internal fun ShortcutKeycaps(
    shortcut: String,
    modifier: Modifier = Modifier,
    tone: ShortcutKeycapTone = ShortcutKeycapTone.Neutral,
    selected: Boolean = false,
    announce: Boolean = true,
) {
    val presentation = shortcutKeycapPresentation(shortcut)
    val semanticModifier = if (announce) {
        Modifier.semantics {
            contentDescription = presentation.accessibilityLabel
        }
    } else {
        Modifier.clearAndSetSemantics { }
    }

    Row(
        modifier = modifier.then(semanticModifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        presentation.alternatives.forEachIndexed { alternativeIndex, chord ->
            if (alternativeIndex > 0) {
                Text(
                    text = "/",
                    color = TextMuted.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                chord.keys.forEachIndexed { keyIndex, key ->
                    if (keyIndex > 0) {
                        Text(
                            text = "+",
                            color = TextMuted.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    ShortcutKeycap(
                        key = key,
                        tone = tone,
                        selected = selected,
                    )
                }
            }
        }
    }
}

@Composable
internal fun ShortcutHint(
    shortcut: String,
    label: String,
    modifier: Modifier = Modifier,
    tone: ShortcutKeycapTone = ShortcutKeycapTone.Neutral,
    selected: Boolean = false,
    labelColor: Color = TextMuted,
) {
    val presentation = shortcutKeycapPresentation(shortcut)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "${presentation.accessibilityLabel}, $label"
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        ShortcutKeycaps(
            shortcut = shortcut,
            tone = tone,
            selected = selected,
            announce = false,
        )
        Text(
            text = label,
            color = labelColor,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            softWrap = false,
        )
    }
}


@Composable
internal fun ShortcutActionLabel(
    label: String,
    shortcut: String,
    modifier: Modifier = Modifier,
    tone: ShortcutKeycapTone = ShortcutKeycapTone.Neutral,
    labelColor: Color = TextMuted,
    labelStyle: TextStyle? = null,
) {
    val presentation = shortcutKeycapPresentation(shortcut)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label. Shortcut ${presentation.accessibilityLabel}"
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            color = labelColor,
            style = labelStyle ?: MaterialTheme.typography.labelLarge,
            maxLines = 1,
            softWrap = false,
        )
        ShortcutKeycaps(
            shortcut = shortcut,
            tone = tone,
            announce = false,
        )
    }
}

@Composable
private fun ShortcutKeycap(
    key: String,
    tone: ShortcutKeycapTone,
    selected: Boolean,
) {
    val containerColor: Color
    val contentColor: Color
    val borderColor: Color

    when (tone) {
        ShortcutKeycapTone.Neutral -> {
            containerColor = Color(0xFF15181C)
            contentColor = if (selected) TextPrimary else TextMuted
            borderColor = Border
        }

        ShortcutKeycapTone.Accent -> {
            containerColor = if (selected) Olive else Color(0xFF2C3323)
            contentColor = if (selected) Color(0xFF151812) else Olive
            borderColor = Olive.copy(alpha = 0.45f)
        }

        ShortcutKeycapTone.OnAccent -> {
            containerColor = Color(0xFF151812).copy(alpha = 0.16f)
            contentColor = Color(0xFF151812)
            borderColor = Color(0xFF151812).copy(alpha = 0.28f)
        }
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = key,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
