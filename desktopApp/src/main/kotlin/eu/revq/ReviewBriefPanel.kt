package eu.revq

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val InspectorBg = Color(0xFF181B1F)
private val InspectorFooterBg = Color(0xFF171A1E)
private val RecommendationBg = Color(0xFF20251F)
private val SubtleSectionBg = Color(0xFF1A1E22)

private enum class BriefStatusTone {
    Positive,
    Neutral,
    Warning,
    Negative,
}

@Composable
fun ReviewBriefPanel(state: AppState) {
    val pr = state.selectedPullRequest ?: return

    Column(
        modifier = Modifier
            .width(470.dp)
            .fillMaxHeight()
            .background(InspectorBg),
    ) {
        ReviewBriefHeader(
            state = state,
            pr = pr,
            onClose = { state.selectedPullRequest = null },
        )

        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (pr.source == PullRequestSource.Mine) {
                item { OwnPullRequestHero(pr) }
                item { OwnPullRequestSignals(pr) }
                item { OwnPullRequestReviewSection(pr) }
                item { OwnPullRequestChecksSection(pr) }
                item { OwnPullRequestMergeabilitySection(pr) }
                if (pr.comments > 0 || pr.discussionNeedsResponse) {
                    item { OwnPullRequestDiscussionSection(pr) }
                }
                item {
                    ReviewBriefSection(
                        label = "ACTIVITY",
                        body = activitySummary(pr),
                        detail = activityDetail(state, pr),
                    )
                }
            } else {
                item { ReviewRequestHero(state, pr) }
                item { ReviewRequestSection(pr) }
                item { ReviewReadinessSection(pr) }
                item {
                    ReviewBriefSection(
                        label = "ACTIVITY",
                        body = activitySummary(pr),
                        detail = activityDetail(state, pr),
                    )
                }
                if (state.isHandledCurrent(pr)) {
                    item {
                        ReviewBriefSection(
                            label = "LOCAL STATE",
                            body = "Reviewed locally",
                            detail = "This review stays out of Needs Review until GitHub reports new activity on the pull request.",
                        )
                    }
                } else {
                    item { ReviewQueueSection(state, pr) }
                }
            }

            item { Spacer(Modifier.height(2.dp)) }
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)

        ReviewBriefActionBar(
            state = state,
            pr = pr,
        )
    }
}

@Composable
private fun ReviewBriefHeader(
    state: AppState,
    pr: PullRequest,
    onClose: () -> Unit,
) {
    val ownStatus = if (pr.source == PullRequestSource.Mine) ownPullRequestPrimaryStatus(pr) else null
    val eyebrow = when {
        pr.source == PullRequestSource.Mine -> "YOUR PULL REQUEST"
        state.isHandledCurrent(pr) -> "REVIEWED REQUEST"
        else -> "REVIEW BRIEF"
    }
    val statusColor = when {
        ownStatus != null -> colorForOwnPullRequestStatus(ownStatus)
        state.isHandledCurrent(pr) -> TextMuted
        else -> Olive
    }
    val statusText = when {
        ownStatus != null -> ownPullRequestStatusTitle(ownStatus)
        state.isHandledCurrent(pr) -> "Reviewed locally"
        else -> "Needs your review"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = eyebrow,
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )

            BriefIconAction(
                label = if (state.isPinned(pr)) "Unpin pull request" else "Pin pull request",
                icon = if (state.isPinned(pr)) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                tint = if (state.isPinned(pr)) Olive else TextMuted,
                state = state,
                onClick = { state.togglePin(pr) },
            )

            BriefIconAction(
                label = "Close review brief",
                icon = Icons.Rounded.Close,
                state = state,
                onClick = onClose,
            )
        }

        Text(
            text = pr.title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = "${pr.repository} · #${pr.number}",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(statusColor),
            )

            Text(
                text = "$statusText · updated ${staleOrRelativeLabel(pr.updatedAt)}",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun OwnPullRequestHero(pr: PullRequest) {
    val status = ownPullRequestPrimaryStatus(pr)
    val label = when (status) {
        OwnPullRequestStatus.ChangesRequested,
        OwnPullRequestStatus.MergeConflict,
        OwnPullRequestStatus.ChecksFailing,
        OwnPullRequestStatus.DiscussionNeedsResponse -> "ACTION REQUIRED"

        OwnPullRequestStatus.ApprovedAndReady -> "READY TO MOVE"
        OwnPullRequestStatus.WaitingForReviewer -> "WAITING"
        OwnPullRequestStatus.Draft -> "DRAFT"
        OwnPullRequestStatus.NoActionNeeded -> "CURRENT SIGNAL"
    }

    InsightHeroCard(
        label = label,
        title = ownPullRequestStatusTitle(status),
        detail = ownPullRequestStatusBody(status, pr),
        accent = colorForOwnPullRequestStatus(status),
    )
}

@Composable
private fun OwnPullRequestSignals(pr: PullRequest) {
    val signals = ownPullRequestSignals(pr)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SubtleSectionBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionLabel("SIGNALS")

        signals.forEach { status ->
            BriefStatusItem(
                tone = toneForOwnStatus(status),
                text = ownPullRequestRowStatus(pr, status),
            )
        }
    }
}

@Composable
private fun OwnPullRequestReviewSection(pr: PullRequest) {
    val decision = when (pr.reviewDecision?.uppercase()) {
        "APPROVED" -> "Approved"
        "CHANGES_REQUESTED" -> "Changes requested"
        "REVIEW_REQUIRED" -> "Review required"
        else -> "No review decision yet"
    }

    val detail = when {
        pr.reviewDecision.equals("CHANGES_REQUESTED", ignoreCase = true) -> {
            formatIdentityList(pr.changeRequestReviewers)
                ?.let { "Requested by $it." }
                ?: "A reviewer has requested changes."
        }

        pr.reviewDecision.equals("APPROVED", ignoreCase = true) -> {
            formatIdentityList(pr.approvingReviewers)
                ?.let { "Approved by $it." }
                ?: "The current review decision is approved."
        }

        pr.reviewRequestsCount > 0 -> {
            formatIdentityList(pr.requestedReviewers)
                ?.let { "Waiting on $it." }
                ?: "${pr.reviewRequestsCount} ${if (pr.reviewRequestsCount == 1) "reviewer is" else "reviewers are"} still requested."
        }

        else -> "No explicit reviewer request is currently reported by GitHub."
    }

    ReviewBriefSection(
        label = "REVIEW",
        body = decision,
        detail = detail,
    )
}

@Composable
private fun OwnPullRequestChecksSection(pr: PullRequest) {
    val body = when {
        pr.checksFailing > 0 -> "${pr.checksFailing} ${if (pr.checksFailing == 1) "check is" else "checks are"} failing"
        pr.checksPending > 0 -> "${pr.checksPending} ${if (pr.checksPending == 1) "check is" else "checks are"} still running"
        pr.checksTotal > 0 -> "All ${pr.checksTotal} checks are clear"
        else -> "No checks reported"
    }

    val detail = when {
        pr.checksFailing > 0 -> "Open GitHub to inspect the failing checks and logs."
        pr.checksPending > 0 -> "The pull request may change state when the remaining checks finish."
        pr.checksTotal > 0 -> "RevQ did not detect a failing or pending check."
        else -> "GitHub did not return status checks for this pull request."
    }

    ReviewBriefSection(
        label = "CHECKS",
        body = body,
        detail = detail,
    )
}

@Composable
private fun OwnPullRequestMergeabilitySection(pr: PullRequest) {
    val body = when {
        pr.mergeable.equals("CONFLICTING", ignoreCase = true) ||
                pr.mergeStateStatus.equals("DIRTY", ignoreCase = true) -> "Merge conflict detected"

        pr.mergeable.equals("MERGEABLE", ignoreCase = true) -> "No merge conflict detected"
        else -> "Mergeability not determined yet"
    }

    val detail = pr.mergeStateStatus
        ?.takeIf { it.isNotBlank() }
        ?.let { "GitHub merge state: ${friendlyEnumValue(it)}." }

    ReviewBriefSection(
        label = "MERGEABILITY",
        body = body,
        detail = detail,
    )
}

@Composable
private fun OwnPullRequestDiscussionSection(pr: PullRequest) {
    val unresolved = pr.unresolvedDiscussionCount
    val authors = formatIdentityList(pr.unresolvedDiscussionAuthors)

    val body = when {
        unresolved == null -> "Discussion state unavailable"
        unresolved == 1 -> "1 unresolved review discussion"
        unresolved > 1 -> "$unresolved unresolved review discussions"
        pr.comments == 1 -> "1 comment · no unresolved review discussions"
        else -> "${pr.comments} comments · no unresolved review discussions"
    }

    val detail = when {
        unresolved == null ->
            "RevQ could not determine the review-thread resolution state during the latest refresh. Comment count alone is not treated as an action item."
        unresolved > 0 && authors != null ->
            "Open ${if (unresolved == 1) "thread involves" else "threads involve"} $authors. Check the unresolved discussion before moving the pull request forward."
        unresolved > 0 ->
            "At least one GitHub review thread is still open. Check the unresolved discussion before moving the pull request forward."
        else ->
            "GitHub reports no unresolved review threads. Resolved comments do not create an action item in RevQ."
    }

    ReviewBriefSection(
        label = "DISCUSSION",
        body = body,
        detail = detail,
    )
}

@Composable
private fun ReviewRequestHero(state: AppState, pr: PullRequest) {
    val handled = state.isHandledCurrent(pr)
    val title = when {
        handled -> "Reviewed locally"
        pr.isDraft -> "Review requested on a draft"
        else -> "Review this pull request"
    }
    val detail = when {
        handled ->
            "You marked this review handled. It will return to Needs Review when GitHub reports new activity."
        pr.isDraft ->
            "Your review is requested, but the pull request is still a draft. You can inspect it now, but it may change before it is ready."
        pr.checksFailing > 0 ->
            "Your review is explicitly requested${pr.authorLogin?.let { " on @$it's pull request" }.orEmpty()}. ${pr.checksFailing} ${if (pr.checksFailing == 1) "check is" else "checks are"} failing, so review the changes with that context."
        pr.mergeable.equals("CONFLICTING", ignoreCase = true) ->
            "Your review is explicitly requested${pr.authorLogin?.let { " on @$it's pull request" }.orEmpty()}. GitHub also reports a merge conflict that the author will need to resolve."
        else ->
            "Your review is explicitly requested${pr.authorLogin?.let { " on @$it's pull request" }.orEmpty()}. Open the diff and leave an approval, comment, or change request."
    }

    InsightHeroCard(
        label = if (handled) "LOCAL STATE" else "RECOMMENDED ACTION",
        title = title,
        detail = detail,
        accent = if (handled) TextMuted else Olive,
    )
}

@Composable
private fun ReviewRequestSection(pr: PullRequest) {
    val author = pr.authorLogin?.takeIf { it.isNotBlank() }
    val requested = formatIdentityList(pr.requestedReviewers)

    val detail = buildString {
        when {
            author != null -> append("Opened by @$author.")
            else -> append("This pull request is explicitly in your review queue.")
        }

        if (requested != null) {
            append(" GitHub currently lists $requested as requested ${if (pr.requestedReviewers.distinct().size == 1) "reviewer" else "reviewers"}.")
        }
    }

    ReviewBriefSection(
        label = "REQUEST",
        body = "Your review is requested",
        detail = detail,
    )
}

@Composable
private fun ReviewReadinessSection(pr: PullRequest) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SubtleSectionBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionLabel("READINESS")

        BriefStatusItem(
            tone = if (pr.isDraft) BriefStatusTone.Warning else BriefStatusTone.Positive,
            text = if (pr.isDraft) "Pull request is still a draft" else "Pull request is open for review",
        )

        BriefStatusItem(
            tone = when {
                pr.checksFailing > 0 -> BriefStatusTone.Negative
                pr.checksPending > 0 -> BriefStatusTone.Warning
                pr.checksTotal > 0 -> BriefStatusTone.Positive
                else -> BriefStatusTone.Neutral
            },
            text = when {
                pr.checksFailing > 0 -> "${pr.checksFailing} checks failing"
                pr.checksPending > 0 -> "${pr.checksPending} checks still running"
                pr.checksTotal > 0 -> "All reported checks are clear"
                else -> "No checks reported"
            },
        )

        BriefStatusItem(
            tone = when {
                pr.mergeable.equals("CONFLICTING", ignoreCase = true) -> BriefStatusTone.Negative
                pr.mergeable.equals("MERGEABLE", ignoreCase = true) -> BriefStatusTone.Positive
                else -> BriefStatusTone.Neutral
            },
            text = when {
                pr.mergeable.equals("CONFLICTING", ignoreCase = true) -> "Merge conflict detected"
                pr.mergeable.equals("MERGEABLE", ignoreCase = true) -> "No merge conflict detected"
                else -> "Mergeability not determined yet"
            },
        )

        when (pr.reviewDecision?.uppercase()) {
            "CHANGES_REQUESTED" -> BriefStatusItem(
                tone = BriefStatusTone.Warning,
                text = formatIdentityList(pr.changeRequestReviewers)
                    ?.let { "Changes requested by $it" }
                    ?: "Changes have already been requested",
            )
            "APPROVED" -> BriefStatusItem(
                tone = BriefStatusTone.Positive,
                text = formatIdentityList(pr.approvingReviewers)
                    ?.let { "Approved by $it" }
                    ?: "Current review decision is approved",
            )
            "REVIEW_REQUIRED" -> BriefStatusItem(
                tone = BriefStatusTone.Neutral,
                text = formatIdentityList(pr.requestedReviewers)
                    ?.let { "Review requested from $it" }
                    ?: "Review is still required",
            )
        }
    }
}

@Composable
private fun InsightHeroCard(
    label: String,
    title: String,
    detail: String,
    accent: Color,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RecommendationBg),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent),
                )

                Text(
                    text = label,
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = detail,
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ReviewBriefSection(
    label: String,
    body: String,
    detail: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        SectionLabel(label)

        Text(
            text = body,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (!detail.isNullOrBlank()) {
            Text(
                text = detail,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = TextMuted,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun BriefStatusItem(
    tone: BriefStatusTone,
    text: String,
) {
    val icon = when (tone) {
        BriefStatusTone.Positive -> Icons.Rounded.CheckCircle
        BriefStatusTone.Neutral -> Icons.AutoMirrored.Rounded.KeyboardArrowRight
        BriefStatusTone.Warning -> Icons.Rounded.WarningAmber
        BriefStatusTone.Negative -> Icons.Rounded.WarningAmber
    }

    val tint = when (tone) {
        BriefStatusTone.Positive -> ReadyGreen
        BriefStatusTone.Neutral -> InfoBlue
        BriefStatusTone.Warning -> Amber
        BriefStatusTone.Negative -> MaterialTheme.colorScheme.error
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )

        Text(
            text = text,
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun toneForOwnStatus(status: OwnPullRequestStatus): BriefStatusTone = when (status) {
    OwnPullRequestStatus.ChangesRequested,
    OwnPullRequestStatus.MergeConflict,
    OwnPullRequestStatus.ChecksFailing -> BriefStatusTone.Negative

    OwnPullRequestStatus.DiscussionNeedsResponse -> BriefStatusTone.Warning
    OwnPullRequestStatus.ApprovedAndReady -> BriefStatusTone.Positive
    OwnPullRequestStatus.Draft,
    OwnPullRequestStatus.WaitingForReviewer,
    OwnPullRequestStatus.NoActionNeeded -> BriefStatusTone.Neutral
}

@Composable
private fun ReviewQueueSection(
    state: AppState,
    pr: PullRequest,
) {
    val queue = state.reviewQueue()
    val index = queue.indexOfFirst { it.key == pr.key }
    val position = if (index >= 0) index + 1 else null
    val remaining = when {
        position == null -> queue.size
        else -> (queue.size - position).coerceAtLeast(0)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ReviewBriefSection(
            label = "QUEUE",
            body = when {
                position != null -> "Review $position of ${queue.size}"
                else -> queuePositionCopy(state, pr)
            },
            detail = when {
                state.reviewSessionActive && remaining > 0 -> "$remaining reviews remain after this one."
                state.reviewSessionActive -> "This is the last review in the current queue."
                else -> "Start or continue a review session from the main workspace."
            },
        )

        if (state.reviewSessionActive && position != null && queue.isNotEmpty()) {
            ReviewQueueRail(position = position, total = queue.size)
        }
    }
}

@Composable
private fun ReviewQueueRail(
    position: Int,
    total: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(total.coerceAtMost(12)) { index ->
            val active = index < position
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) Olive else Border),
            )
        }
    }
}

@Composable
private fun ReviewBriefActionBar(
    state: AppState,
    pr: PullRequest,
) {
    val canMarkReviewed =
        pr.source == PullRequestSource.ReviewRequest &&
                !state.isHandledCurrent(pr)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(InspectorFooterBg)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BriefIconAction(
                label = "Open selected PR in GitHub",
                icon = Icons.Rounded.OpenInBrowser,
                tint = Olive,
                state = state,
                onClick = { openUrl(pr.url) },
            )

            Text(
                text = "Open in GitHub",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )

            ReviewBriefOverflowMenu(
                state = state,
                pr = pr,
            )
        }

        if (canMarkReviewed) {
            if (state.reviewSessionActive) {
                Button(
                    onClick = { state.markReviewed(pr) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PanelElevated,
                        contentColor = TextPrimary,
                    ),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Mark reviewed & next")
                    Spacer(Modifier.weight(1f))
                    ShortcutPill("Space")
                }

                TextButton(
                    onClick = { state.nextReview() },
                    enabled = state.reviewQueue().size > 1,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Skip for now")
                    Spacer(Modifier.weight(1f))
                    ShortcutPill("N")
                }
            } else {
                Button(
                    onClick = { state.markReviewed(pr) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PanelElevated,
                        contentColor = TextPrimary,
                    ),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Mark reviewed")
                    Spacer(Modifier.weight(1f))
                    ShortcutPill("Space")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BriefIconAction(
    label: String,
    icon: ImageVector,
    state: AppState,
    tint: Color = TextMuted,
    onClick: () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        state = androidx.compose.material3.rememberTooltipState(),
        tooltip = {
            PlainTooltip(
                containerColor = PanelElevated,
                contentColor = TextPrimary,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
    ) {
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .semantics { contentDescription = label }
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) state.statusLine = "Enter $label"
                }
                .clickable(onClick = onClick),
            color = PanelElevated,
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(10.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

@Composable
private fun ReviewBriefOverflowMenu(
    state: AppState,
    pr: PullRequest,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { expanded = true },
            color = PanelElevated,
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(10.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More actions",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(270.dp)
                .background(PanelBg),
        ) {
            ThemedMenuHeader("ACTIONS")

            ThemedMenuItem(
                icon = Icons.Rounded.Link,
                title = "Copy URL",
                onClick = {
                    expanded = false
                    state.copySelectedUrl()
                },
            )

            ThemedMenuItem(
                icon = Icons.Rounded.ContentCopy,
                title = "Copy as Markdown",
                onClick = {
                    expanded = false
                    state.copySelectedMarkdown()
                },
            )

            ThemedMenuItem(
                icon = Icons.Rounded.FolderOpen,
                title = "Open repository",
                onClick = {
                    expanded = false
                    openUrl("https://github.com/${pr.repository}")
                },
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = Border,
            )

            ThemedMenuItem(
                icon = if (state.isPinned(pr)) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                title = if (state.isPinned(pr)) "Unpin pull request" else "Pin pull request",
                onClick = {
                    expanded = false
                    state.togglePin(pr)
                },
            )

            ThemedMenuItem(
                icon = Icons.AutoMirrored.Rounded.VolumeOff,
                title = "Mute repository",
                onClick = {
                    expanded = false
                    state.toggleMuteSelectedRepository()
                },
                destructive = true,
            )
        }
    }
}

@Composable
private fun ThemedMenuHeader(title: String) {
    Text(
        text = title,
        color = TextMuted,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
    )
}

@Composable
private fun ThemedMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    val contentColor = if (destructive) Amber else TextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PanelElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (destructive) Amber else TextMuted,
                modifier = Modifier.size(17.dp),
            )
        }

        Text(
            text = title,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ShortcutPill(text: String) {
    Surface(
        color = Color(0xFF2C3138),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Border),
    ) {
        Text(
            text = text,
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

private fun activitySummary(pr: PullRequest): String =
    "Updated ${staleOrRelativeLabel(pr.updatedAt)} · ${commentCountLabel(pr.comments)}"

private fun activityDetail(state: AppState, pr: PullRequest): String? {
    val handledAt = state.handledReviewRecords[pr.key]

    return when {
        handledAt == null -> null
        handledAt != pr.updatedMarker -> "Updated after you last marked it reviewed in RevQ."
        else -> "No new activity since you marked it reviewed in RevQ."
    }
}

private fun commentCountLabel(count: Int): String = when (count) {
    0 -> "no comments"
    1 -> "1 comment"
    else -> "$count comments"
}

private fun friendlyEnumValue(value: String): String = value
    .lowercase()
    .replace('_', ' ')
    .replaceFirstChar { it.uppercase() }
