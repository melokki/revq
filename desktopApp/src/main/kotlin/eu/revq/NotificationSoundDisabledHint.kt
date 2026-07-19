package eu.revq

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
internal fun NotificationSoundDisabledHint(
    onConfigure: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .clickable(
                role = Role.Button,
                onClick = onConfigure,
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Notification sound is off",
            color = TextMuted.copy(alpha = 0.62f),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = " · ",
            color = TextMuted.copy(alpha = 0.48f),
            style = MaterialTheme.typography.labelSmall,
        )
        ShortcutHint(
            shortcut = NotificationSettingsShortcutLabel,
            label = "Sound settings",
            modifier = Modifier.alpha(0.68f),
            labelColor = TextMuted,
        )
    }
}
