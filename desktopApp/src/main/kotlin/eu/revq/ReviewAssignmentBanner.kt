package eu.revq

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ReviewAssignmentBanner(
    state: AppState,
    alert: ReviewAssignmentAlert,
) {
    val first = alert.pullRequests.firstOrNull()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        color = Color(0xFF1D221C),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Olive.copy(alpha = 0.32f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = Olive.copy(alpha = 0.16f),
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = Olive,
                    modifier = Modifier.padding(9.dp).size(19.dp),
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = if (alert.count == 1) {
                        "A new review was assigned to you"
                    } else {
                        "${alert.count} new reviews were assigned to you"
                    },
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (first != null) {
                    Text(
                        text = if (alert.count == 1) {
                            "${first.repository} #${first.number} · ${first.title}"
                        } else {
                            "${first.repository} #${first.number} · ${first.title} · +${alert.count - 1} more"
                        },
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            TextButton(onClick = state::dismissReviewAssignmentAlert) {
                ShortcutActionLabel(
                    label = "Dismiss",
                    shortcut = NotificationDismissShortcutLabel,
                )
            }

            Button(
                onClick = state::openReviewQueueFromAssignmentAlert,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Olive,
                    contentColor = Color(0xFF151812),
                ),
            ) {
                ShortcutActionLabel(
                    label = "Open queue",
                    shortcut = "Enter",
                    tone = ShortcutKeycapTone.OnAccent,
                    labelColor = Color(0xFF151812),
                )
            }
        }
    }
}
