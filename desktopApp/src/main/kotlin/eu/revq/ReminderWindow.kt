package eu.revq

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant

@Composable
fun ReminderWindow(state: AppState) {
    val queue = state.reviewQueue()
    val first = queue.firstOrNull()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                handleReminderWindowKeyEvent(
                    event = event,
                    state = state,
                    hasReviews = queue.isNotEmpty(),
                )
            },
        color = Color(0xFF15181C),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            ReminderHeader(
                state = state,
                queueSize = queue.size,
            )

            Divider(color = Border)

            if (first == null) {
                ReminderEmptyState(
                    state = state,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    ReminderQueueMetrics(queue)

                    ReminderStartHereCard(
                        pr = first,
                        onOpen = { openUrl(first.url) },
                    )

                    val upcoming = queue.drop(1).take(3)
                    if (upcoming.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "UP NEXT",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )

                            upcoming.forEachIndexed { index, pr ->
                                ReminderQueueRow(
                                    position = index + 2,
                                    pr = pr,
                                )
                            }

                            if (queue.size > 4) {
                                Text(
                                    text = "+${queue.size - 4} more in your review queue",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 42.dp),
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = Border)

            ReminderActions(
                state = state,
                hasReviews = queue.isNotEmpty(),
            )
        }
    }
}

@Composable
private fun ReminderHeader(
    state: AppState,
    queueSize: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 22.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Olive),
                )

                Text(
                    text = if (state.reminderWindowIsPreview) "REMINDER PREVIEW" else "REVIEW REMINDER",
                    color = Olive,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = when {
                    queueSize == 0 -> "Your queue is clear"
                    queueSize == 1 -> "One review is waiting"
                    else -> "$queueSize reviews are waiting"
                },
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = when {
                    queueSize == 0 -> "Nothing needs your review right now."
                    state.reminderWindowIsPreview -> "This is how RevQ will surface your review queue when a reminder is due."
                    else -> "Take a focused pass through the queue, or snooze this reminder until later."
                },
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        IconButton(onClick = { state.closeReminderWindow() }) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = if (state.reminderWindowIsPreview) "Close reminder preview" else "Dismiss reminder",
                tint = TextMuted,
            )
        }
    }
}

@Composable
private fun ReminderQueueMetrics(queue: List<PullRequest>) {
    val oldest = queue
        .minByOrNull { instantOrNull(it.updatedAt) ?: Instant.now() }
        ?.let { staleOrRelativeLabel(it.updatedAt) }
        ?: "—"
    val repositories = queue.map { it.repository.toString() }.distinct().size

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ReminderMetric(
            label = "WAITING",
            value = queue.size.toString(),
            modifier = Modifier.weight(1f),
        )
        ReminderMetric(
            label = "OLDEST",
            value = oldest,
            modifier = Modifier.weight(1f),
        )
        ReminderMetric(
            label = "REPOSITORIES",
            value = repositories.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ReminderMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF191D22),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = value,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ReminderStartHereCard(
    pr: PullRequest,
    onOpen: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1D221C),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Olive.copy(alpha = 0.16f))
                    .border(1.dp, Olive.copy(alpha = 0.24f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = repoMonogram(pr),
                    color = Olive,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "START HERE",
                    color = Olive,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = pr.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = reminderPrMetadata(pr),
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            TextButton(onClick = onOpen) {
                Text("Open", color = Olive)
            }
        }
    }
}

@Composable
private fun ReminderQueueRow(
    position: Int,
    pr: PullRequest,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF191D22))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(PanelElevated),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = position.toString(),
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = pr.title,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = reminderPrMetadata(pr),
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = staleOrRelativeLabel(pr.updatedAt),
            color = if (isStale(pr.updatedAt)) Amber else TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ReminderEmptyState(
    state: AppState,
    modifier: Modifier = Modifier,
) {
    val spec = emptyStateSpec(View.NeedsReview)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EmptyStateIllustration(spec)

            Text(
                text = "You're caught up.",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = "Nothing is waiting for your review right now.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ReminderActions(
    state: AppState,
    hasReviews: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121519))
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (hasReviews) {
            Button(
                onClick = { state.openReviewQueueFromReminder() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Olive,
                    contentColor = Color(0xFF151812),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                ShortcutActionLabel(
                    label = "Open review queue",
                    shortcut = "Enter",
                    tone = ShortcutKeycapTone.OnAccent,
                    labelColor = Color(0xFF151812),
                )
            }
        }

        if (state.reminderWindowIsPreview) {
            Button(
                onClick = { state.closeReminderWindow() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PanelElevated,
                    contentColor = TextPrimary,
                ),
                border = BorderStroke(1.dp, Border),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                ShortcutActionLabel(
                    label = "Close preview",
                    shortcut = NotificationDismissShortcutLabel,
                    labelColor = TextPrimary,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { state.snoozeReminder() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PanelElevated,
                        contentColor = TextPrimary,
                    ),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    ShortcutActionLabel(
                        label = reminderSnoozeLabel(state.reminderSnoozeMinutesText),
                        shortcut = "S",
                        labelColor = TextPrimary,
                    )
                }

                TextButton(
                    onClick = { state.dismissReminderToday() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    ShortcutActionLabel(
                        label = "Dismiss for today",
                        shortcut = NotificationDismissShortcutLabel,
                    )
                }
            }
        }

        if (state.notificationSoundMode == NotificationSoundMode.Off) {
            NotificationSoundDisabledHint(
                onConfigure = state::openNotificationSettingsFromReminder,
            )
        }
    }
}

private fun handleReminderWindowKeyEvent(
    event: KeyEvent,
    state: AppState,
    hasReviews: Boolean,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false

    if (isNotificationDismissKey(event.key)) {
        state.closeReminderWindow()
        return true
    }

    if (
        state.notificationSoundMode == NotificationSoundMode.Off &&
        isNotificationSettingsKey(event.key)
    ) {
        state.openNotificationSettingsFromReminder()
        return true
    }

    return when (event.key) {
        Key.Enter -> {
            if (hasReviews) {
                state.openReviewQueueFromReminder()
            } else {
                state.closeReminderWindow()
            }
            true
        }
        Key.S -> {
            if (state.reminderWindowIsPreview) {
                false
            } else {
                state.snoozeReminder()
                true
            }
        }
        else -> false
    }
}

private fun reminderPrMetadata(pr: PullRequest): String {
    return buildString {
        append("${pr.repository} #${pr.number}")
        pr.authorLogin?.takeIf { it.isNotBlank() }?.let { append(" · @$it") }
        if (pr.comments > 0) {
            append(" · ${pr.comments} comment")
            if (pr.comments != 1) append("s")
        }
    }
}

private fun reminderSnoozeLabel(rawMinutes: String): String {
    val minutes = rawMinutes.toIntOrNull()?.coerceAtLeast(1) ?: 60
    return when {
        minutes % 60 == 0 -> {
            val hours = minutes / 60
            "Snooze ${hours}h"
        }
        else -> "Snooze ${minutes}m"
    }
}
