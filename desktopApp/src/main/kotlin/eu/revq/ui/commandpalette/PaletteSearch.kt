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
        val title = result.title.lowercase()
        val aliases = (result as? PaletteResult.CommandResult)
            ?.command
            ?.aliases
            ?.map(String::lowercase)
            .orEmpty()
        val titleWords = title.split(Regex("[^a-z0-9]+")).filter(String::isNotBlank)
        val searchableWords = haystack.split(Regex("[^a-z0-9]+")).filter(String::isNotBlank)
        val acronym = title
            .split(Regex("[^a-z0-9]+"))
            .filter(String::isNotBlank)
            .joinToString("") { it.first().toString() }
        if (terms.any { term ->
                !haystack.contains(term) &&
                        !acronym.startsWith(term) &&
                        !searchableWords.any { word -> fuzzyWordMatch(term, word) }
            }
        ) {
            return@mapNotNull null
        }

        val score = when {
            title == normalizedQuery -> 0
            title.startsWith(normalizedQuery) -> 10
            aliases.any { it == normalizedQuery } -> 15
            title.contains(normalizedQuery) -> 20
            aliases.any { it.startsWith(normalizedQuery) } -> 22
            acronym.startsWith(normalizedQuery) -> 25
            terms.all { term -> titleWords.any { it.startsWith(term) } } -> 30
            terms.all { term -> title.contains(term) } -> 35
            terms.all { term -> searchableWords.any { word -> fuzzyWordMatch(term, word) } } -> 80
            else -> 50
        }

        ScoredPaletteResult(result = result, score = score + result.relevanceBoost)
    }

    // A typed query is ordered globally by relevance. Section grouping remains
    // useful for the curated blank state, but should not bury a stronger match.
    return scored
        .sortedWith(
            compareBy<ScoredPaletteResult> { it.score }
                .thenByDescending { it.result.enabled }
                .thenBy { it.result.title.lowercase() },
        )
        .map { it.result }
}

internal fun fuzzyWordMatch(query: String, word: String): Boolean {
    if (query.length < 4 || kotlin.math.abs(query.length - word.length) > 1) return false
    val previous = IntArray(word.length + 1) { it }
    for (queryIndex in query.indices) {
        var diagonal = previous[0]
        previous[0] = queryIndex + 1
        var rowMinimum = previous[0]
        for (wordIndex in word.indices) {
            val above = previous[wordIndex + 1]
            val substitution = diagonal + if (query[queryIndex] == word[wordIndex]) 0 else 1
            val value = minOf(
                previous[wordIndex] + 1,
                above + 1,
                substitution,
            )
            diagonal = above
            previous[wordIndex + 1] = value
            rowMinimum = minOf(rowMinimum, value)
        }
        if (rowMinimum > 1) return false
    }
    return previous[word.length] <= 1
}

private data class ScoredPaletteResult(
    val result: PaletteResult,
    val score: Int,
)
