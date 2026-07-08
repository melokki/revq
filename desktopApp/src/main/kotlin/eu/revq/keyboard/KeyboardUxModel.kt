package eu.revq.keyboard

import eu.revq.AppState
import eu.revq.AttentionKind
import eu.revq.PullRequestSource
import eu.revq.View
import eu.revq.attentionKind

data class KeyboardHint(
    val key: String,
    val label: String,
)

fun keyboardHints(
    state: AppState,
    paletteOpen: Boolean,
): List<KeyboardHint> {
    if (paletteOpen) {
        return listOf(
            KeyboardHint("↑↓", "Move"),
            KeyboardHint("Enter", "Run"),
            KeyboardHint("Esc", "Close"),
        )
    }

    if (state.keyboardMode == KeyboardMode.Insert) {
        return listOf(KeyboardHint("Esc", "Normal"))
    }

    if (state.view == View.Settings) {
        return listOf(
            KeyboardHint("j/k", "Rows"),
            KeyboardHint("h/l", "Sections"),
            KeyboardHint("Enter", "Edit"),
            KeyboardHint("Esc", "Back"),
            KeyboardHint("Ctrl+S", "Save"),
            KeyboardHint("Space", "More"),
        )
    }

    return when (state.keyboardFocusRegion) {
        FocusRegion.Sidebar -> listOf(
            KeyboardHint("j/k", "Move"),
            KeyboardHint("Enter", "Open"),
            KeyboardHint("Space", "More"),
        )

        FocusRegion.PullRequestList -> buildList {
            add(KeyboardHint("j/k", "Move"))
            val selected = state.selectedPullRequest
            if (selected != null) {
                add(KeyboardHint("o", "GitHub"))
                if (selected.source == PullRequestSource.ReviewRequest) {
                    add(KeyboardHint("m", "Reviewed"))
                }
                add(KeyboardHint("p", "Pin"))
                add(KeyboardHint("c", "Copy"))
            }
            add(KeyboardHint("Space", "More"))
        }

        FocusRegion.ReviewBrief -> buildList {
            add(KeyboardHint("h", "PR list"))
            add(KeyboardHint("o", "GitHub"))
            if (state.selectedPullRequest?.source == PullRequestSource.ReviewRequest) {
                add(KeyboardHint("m", "Reviewed"))
            }
            add(KeyboardHint("Space", "More"))
        }
    }
}

fun nextKeyboardAction(state: AppState): KeyboardHint? {
    val selected = state.selectedPullRequest
    if (selected != null) {
        return when {
            selected.source == PullRequestSource.ReviewRequest -> {
                KeyboardHint("m", "Mark reviewed & next")
            }

            attentionKind(selected) in setOf(AttentionKind.Action, AttentionKind.Blocked) ||
                    selected.checksFailing > 0 -> {
                KeyboardHint("o", "Open PR")
            }

            state.isPinned(selected) -> KeyboardHint("p", "Unpin PR")
            else -> KeyboardHint("p", "Pin PR")
        }
    }

    return when {
        state.reviewQueue().isNotEmpty() && !state.reviewSessionActive -> {
            KeyboardHint("Space", "Start review session")
        }

        state.visiblePullRequests().isNotEmpty() -> KeyboardHint("j", "Select first PR")
        else -> null
    }
}

fun activeKeyboardRegionLabel(
    state: AppState,
    paletteOpen: Boolean,
): String = when {
    paletteOpen -> "Palette"
    state.keyboardMode == KeyboardMode.Insert -> "Typing"
    state.view == View.Settings -> "Settings"
    state.keyboardFocusRegion == FocusRegion.Sidebar -> "Sidebar"
    state.keyboardFocusRegion == FocusRegion.PullRequestList -> "Pull requests"
    state.keyboardFocusRegion == FocusRegion.ReviewBrief -> "Review brief"
    else -> "Workspace"
}

fun compactKeyboardHints(
    state: AppState,
    paletteOpen: Boolean,
): List<KeyboardHint> {
    if (paletteOpen) {
        return keyboardHints(state, paletteOpen = true)
    }

    return keyboardHints(state, paletteOpen = false)
        .filterNot { it.key == "Space" }
        .take(2)
}
