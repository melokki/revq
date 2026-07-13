package eu.revq

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AboutDialogActionLayout {
    Inline,
    Wrapped,
}

data class AboutDialogPresentation(
    val width: Dp,
    val actionLayout: AboutDialogActionLayout,
)

fun aboutDialogPresentation(availableWidth: Dp): AboutDialogPresentation {
    val boundedWidth = availableWidth.takeIf { it.value.isFinite() } ?: 552.dp
    val width = (boundedWidth - 32.dp).coerceIn(320.dp, 520.dp)
    return AboutDialogPresentation(
        width = width,
        actionLayout = if (width >= 520.dp) {
            AboutDialogActionLayout.Inline
        } else {
            AboutDialogActionLayout.Wrapped
        },
    )
}
