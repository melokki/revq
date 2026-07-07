package eu.revq.ui.commandpalette

internal fun filterPaletteResults(
    results: List<PaletteResult>,
    query: String,
): List<PaletteResult> {
    val normalizedQuery = query.trim().lowercase()
    if (normalizedQuery.isBlank()) return results

    val terms = normalizedQuery.split(Regex("\\s+")).filter { it.isNotBlank() }

    val scored = results.mapNotNull { result ->
        val haystack = result.searchableText.lowercase()
        if (terms.any { term -> !haystack.contains(term) }) {
            return@mapNotNull null
        }

        val title = result.title.lowercase()
        val score = when {
            title == normalizedQuery -> 0
            title.startsWith(normalizedQuery) -> 10
            title.contains(normalizedQuery) -> 20
            terms.all { term -> title.contains(term) } -> 30
            else -> 50
        }

        ScoredPaletteResult(result = result, score = score)
    }

    // Order sections by their best match, then keep results grouped within each
    // section so the UI never repeats section headers while still prioritizing
    // the strongest result groups globally.
    return scored
        .groupBy { it.result.section }
        .entries
        .sortedWith(
            compareBy<Map.Entry<PaletteSection, List<ScoredPaletteResult>>> { entry ->
                entry.value.minOf { it.score }
            }.thenBy { it.key.ordinal },
        )
        .flatMap { entry ->
            entry.value
                .sortedWith(
                    compareBy<ScoredPaletteResult> { it.score }
                        .thenByDescending { it.result.enabled }
                        .thenBy { it.result.title.lowercase() },
                )
                .map { it.result }
        }
}

private data class ScoredPaletteResult(
    val result: PaletteResult,
    val score: Int,
)
