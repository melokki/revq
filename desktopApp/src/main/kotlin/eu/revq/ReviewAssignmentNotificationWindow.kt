package eu.revq

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

private const val VisibleAssignmentRows = 4

@Composable
fun ReviewAssignmentNotificationWindow(
    state: AppState,
    alert: ReviewAssignmentAlert,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PanelBg,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Border),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            ReviewAssignmentHeader(
                count = alert.count,
                onDismiss = state::dismissReviewAssignmentAlert,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                alert.pullRequests.take(VisibleAssignmentRows).forEach { pullRequest ->
                    ReviewAssignmentRow(pullRequest)
                }

                val remaining = alert.count - VisibleAssignmentRows
                if (remaining > 0) {
                    Text(
                        text = "+$remaining more ${if (remaining == 1) "review" else "reviews"} in your queue",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                    )
                }
            }

            ReviewAssignmentActions(state)
        }
    }
}

@Composable
private fun ReviewAssignmentHeader(
    count: Int,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF171A1E))
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Olive.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = Olive,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(Modifier.width(13.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = if (count == 1) {
                    "A new review was assigned to you"
                } else {
                    "$count new reviews were assigned to you"
                },
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Found during the latest GitHub refresh",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Dismiss new review notification",
                tint = TextMuted,
            )
        }
    }
}

@Composable
private fun ReviewAssignmentRow(pullRequest: PullRequest) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF20242A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Olive.copy(alpha = 0.13f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "#${pullRequest.number}",
                    color = Olive,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = pullRequest.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = assignmentMetadata(pullRequest),
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ReviewAssignmentActions(state: AppState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121519))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = state::dismissReviewAssignmentAlert,
            modifier = Modifier.weight(1f).height(48.dp),
        ) {
            Text("Dismiss", color = TextMuted)
        }

        Button(
            onClick = state::openReviewQueueFromAssignmentAlert,
            colors = ButtonDefaults.buttonColors(
                containerColor = Olive,
                contentColor = Color(0xFF151812),
            ),
            modifier = Modifier.weight(1.4f).height(48.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Open review queue", fontWeight = FontWeight.Bold)
        }
    }
}

private fun assignmentMetadata(pullRequest: PullRequest): String = buildString {
    append(pullRequest.repository)
    pullRequest.authorLogin?.takeIf(String::isNotBlank)?.let {
        append(" · @")
        append(it.removePrefix("@"))
    }
    when (pullRequest.reviewRequestKind) {
        ReviewRequestKind.Direct -> append(" · Direct request")
        ReviewRequestKind.Team -> append(" · Team request")
        ReviewRequestKind.Assignment -> append(" · Assigned")
        null -> Unit
    }
}
