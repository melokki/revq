package eu.revq.keyboard

/** Result of feeding a lower-case g into the sequence tracker. */
enum class GSequenceResult {
    Pending,
    Complete,
}

/**
 * Small sequence tracker used by Phase A for Vim-style `gg` navigation.
 * Phase C can reuse/extend this mechanism for palette-backed prefixes.
 */
class KeySequenceTracker(
    private val timeoutMillis: Long = 1_000L,
) {
    private var pendingGAtMillis: Long? = null

    fun pushG(nowMillis: Long = System.currentTimeMillis()): GSequenceResult {
        val previous = pendingGAtMillis
        return if (previous != null && nowMillis - previous <= timeoutMillis) {
            pendingGAtMillis = null
            GSequenceResult.Complete
        } else {
            pendingGAtMillis = nowMillis
            GSequenceResult.Pending
        }
    }

    fun clear() {
        pendingGAtMillis = null
    }
}
