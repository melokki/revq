package eu.revq.ui.commandpalette

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PaletteDimensions(
    val width: Dp,
    val maxHeight: Dp,
)

data class PaletteFooterHintSpec(
    val key: String,
    val label: String,
)

data class PaletteChromePresentation(
    val footerHints: List<PaletteFooterHintSpec>,
    val showTypePills: Boolean,
)

data class PaletteRowAccessibility(
    val label: String,
    val stateDescription: String,
    val selected: Boolean,
    val button: Boolean,
)

fun PaletteResult.accessibility(selected: Boolean): PaletteRowAccessibility =
    PaletteRowAccessibility(
        label = "${typeLabel()}: $title",
        stateDescription = when {
            !enabled -> "Unavailable. ${subtitle ?: "This result cannot run now."}"
            !actionable -> "Reference only."
            selected -> "Selected. Action available."
            else -> "Action available."
        },
        selected = selected,
        button = actionable,
    )

fun paletteDimensions(
    availableWidth: Dp,
    availableHeight: Dp,
): PaletteDimensions {
    val boundedWidth = availableWidth.takeIf { it.value.isFinite() } ?: 832.dp
    val boundedHeight = availableHeight.takeIf { it.value.isFinite() } ?: 652.dp
    return PaletteDimensions(
        width = (boundedWidth - 32.dp).coerceIn(320.dp, 800.dp),
        maxHeight = (boundedHeight - 32.dp).coerceIn(280.dp, 620.dp),
    )
}

fun paletteChromePresentation(
    availableWidth: Dp,
    acceptsTextQuery: Boolean,
    confirming: Boolean,
    shortcutLabels: PaletteShortcutLabels,
): PaletteChromePresentation {
    val compact = availableWidth < 600.dp
    val primaryHints = listOf(
        PaletteFooterHintSpec("↑↓", "Move"),
        PaletteFooterHintSpec("Enter", if (confirming) "Confirm" else "Run"),
        PaletteFooterHintSpec("Esc", "Close"),
    )
    if (compact) {
        return PaletteChromePresentation(
            footerHints = primaryHints,
            showTypePills = false,
        )
    }

    return PaletteChromePresentation(
        footerHints = buildList {
            add(primaryHints[0])
            add(PaletteFooterHintSpec(shortcutLabels.moveAlternative, "Move"))
            add(primaryHints[1])
            add(PaletteFooterHintSpec("${shortcutLabels.quickRun(1).dropLast(1)}1…9", "Run"))
            if (acceptsTextQuery) {
                add(PaletteFooterHintSpec(shortcutLabels.clear, "Clear"))
            }
            add(primaryHints[2])
        },
        showTypePills = true,
    )
}

fun paletteMatchRanges(
    text: String,
    query: String,
): List<IntRange> {
    val normalizedText = text.lowercase()
    val ranges = query
        .trim()
        .lowercase()
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)
        .flatMap { term ->
            buildList {
                var start = normalizedText.indexOf(term)
                while (start >= 0) {
                    add(start..<(start + term.length))
                    start = normalizedText.indexOf(term, startIndex = start + term.length)
                }
            }
        }
        .sortedBy(IntRange::first)

    if (ranges.isEmpty()) {
        val normalizedQuery = query.trim().lowercase()
        val words = Regex("[A-Za-z0-9]+").findAll(text).toList()
        val acronym = words.joinToString("") { it.value.first().lowercase() }
        if (normalizedQuery.isNotBlank() && acronym.startsWith(normalizedQuery)) {
            return words.take(normalizedQuery.length).map { it.range.first..it.range.first }
        }
        if (normalizedQuery.isNotBlank() && ' ' !in normalizedQuery) {
            words.firstOrNull { fuzzyWordMatch(normalizedQuery, it.value.lowercase()) }
                ?.let { return listOf(it.range) }
        }
        return emptyList()
    }
    return ranges.fold(mutableListOf()) { merged, range ->
        val previous = merged.lastOrNull()
        if (previous != null && range.first <= previous.last + 1) {
            merged[merged.lastIndex] = previous.first..maxOf(previous.last, range.last)
        } else {
            merged += range
        }
        merged
    }
}

fun paletteResultSummary(
    resultCount: Int,
    query: String,
): String = when {
    query.isBlank() -> "$resultCount ${if (resultCount == 1) "suggestion" else "suggestions"}"
    resultCount == 0 -> "No matches for “${query.trim()}”"
    else -> "$resultCount ${if (resultCount == 1) "match" else "matches"} for “${query.trim()}”"
}
