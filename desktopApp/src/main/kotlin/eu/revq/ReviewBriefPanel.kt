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
fun InlineReviewBrief(
    state: AppState,
    pr: PullRequest,
) {
    val signals = compactInlineSignals(state, pr)
    val nextAction = compactInlineNextAction(state, pr)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF23282E))
            .padding(start = 76.dp, end = 22.dp, top = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompactSignalGrid(signals)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(RecommendationBg)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "NEXT",
                color = compactNextActionColor(state, pr),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = nextAction,
                color = TextPrimary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
        }

        CompactInlineActionStrip(
            state = state,
            pr = pr,
        )
    }
}

private data class CompactBriefSignal(
    val label: String,
    val value: String,
    val tone: BriefStatusTone = BriefStatusTone.Neutral,
)

@Composable
private fun CompactSignalGrid(signals: List<CompactBriefSignal>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SubtleSectionBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        signals.take(6).chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.Top,
            ) {
                CompactSignalCell(
                    signal = pair[0],
                    modifier = Modifier.weight(1f),
                )

                if (pair.size > 1) {
                    CompactSignalCell(
                        signal = pair[1],
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompactSignalCell(
    signal: CompactBriefSignal,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = signal.label,
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(88.dp),
            maxLines = 1,
        )

        Text(
            text = signal.value,
            color = compactToneColor(signal.tone),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (signal.tone == BriefStatusTone.Neutral) {
                FontWeight.Normal
            } else {
                FontWeight.SemiBold
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CompactInlineActionStrip(
    state: AppState,
    pr: PullRequest,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CompactInlineAction(
            keyLabel = "Enter",
            label = "Close",
            onClick = { state.togglePullRequestDetails(pr) },
        )
        CompactInlineAction(
            keyLabel = "o",
            label = "Open",
            onClick = { openUrl(pr.url) },
        )
        CompactInlineAction(
            keyLabel = "p",
            label = if (state.isPinned(pr)) "Unpin" else "Pin",
            onClick = { state.togglePin(pr) },
        )

        if (
            pr.source == PullRequestSource.ReviewRequest &&
            !state.isHandledCurrent(pr)
        ) {
            CompactInlineAction(
                keyLabel = "m",
                label = "Handled",
                onClick = { state.markReviewed(pr) },
            )
        }

        if (pr.source == PullRequestSource.Mine && state.isPullRequestReadyToMerge(pr)) {
            val merging = state.mergingPullRequestKey == pr.key
            CompactInlineAction(
                keyLabel = "m",
                label = if (merging) "Merging…" else "Merge",
                enabled = !merging && !state.isMergingPullRequest,
                onClick = { state.mergePullRequest(pr) },
            )
        }

        CompactInlineAction(
            keyLabel = "c",
            label = "Copy URL",
            onClick = { state.copySelectedUrl() },
        )
    }
}

@Composable
private fun CompactInlineAction(
    keyLabel: String,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val shortcutDescription = shortcutKeycapPresentation(keyLabel).accessibilityLabel
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = "$label. Shortcut $shortcutDescription"
            }
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ShortcutKeycaps(
            shortcut = keyLabel,
            tone = if (enabled) ShortcutKeycapTone.Accent else ShortcutKeycapTone.Neutral,
            announce = false,
        )
        Text(
            text = label,
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun compactToneColor(tone: BriefStatusTone): Color = when (tone) {
    BriefStatusTone.Positive -> ReadyGreen
    BriefStatusTone.Neutral -> TextPrimary
    BriefStatusTone.Warning -> Amber
    BriefStatusTone.Negative -> MaterialTheme.colorScheme.error
}

private fun compactInlineSignals(
    state: AppState,
    pr: PullRequest,
): List<CompactBriefSignal> {
    return if (pr.source == PullRequestSource.Mine) {
        compactOwnPullRequestSignals(pr)
    } else {
        compactReviewRequestSignals(state, pr)
    }
}

private fun compactReviewRequestSignals(
    state: AppState,
    pr: PullRequest,
): List<CompactBriefSignal> = listOf(
    CompactBriefSignal(
        label = "REVIEW",
        value = when {
            state.isHandledCurrent(pr) -> "Handled locally"
            pr.isDraft -> "Requested on draft"
            else -> "Waiting for you"
        },
        tone = when {
            state.isHandledCurrent(pr) -> BriefStatusTone.Neutral
            pr.isDraft -> BriefStatusTone.Warning
            else -> BriefStatusTone.Neutral
        },
    ),
    compactCiSignal(pr),
    CompactBriefSignal(
        label = "REVIEWS",
        value = compactReviewState(pr),
        tone = when (pr.reviewDecision?.uppercase()) {
            "APPROVED" -> BriefStatusTone.Positive
            "CHANGES_REQUESTED" -> BriefStatusTone.Warning
            else -> BriefStatusTone.Neutral
        },
    ),
    compactDiscussionSignal(pr),
    compactMergeSignal(pr),
    CompactBriefSignal(
        label = "ACTIVITY",
        value = when {
            state.handledReviewRecords[pr.key]
                ?.let { it != pr.updatedMarker } == true -> "Updated since handled"
            else -> "Updated ${staleOrRelativeLabel(pr.updatedAt)}"
        },
        tone = if (
            state.handledReviewRecords[pr.key]
                ?.let { it != pr.updatedMarker } == true
        ) {
            BriefStatusTone.Warning
        } else {
            BriefStatusTone.Neutral
        },
    ),
)

private fun compactOwnPullRequestSignals(
    pr: PullRequest,
): List<CompactBriefSignal> {
    val attention = PullRequestAttention.describe(pr)
    val status = attention.ownStatus ?: OwnPullRequestStatus.NoActionNeeded
    val peopleSignal = compactOwnPeopleSignal(status, pr)

    return listOfNotNull(
        CompactBriefSignal(
            label = "ATTENTION",
            value = attention.presentation.statusTitle,
            tone = toneForOwnStatus(status),
        ),
        peopleSignal,
        compactCiSignal(pr),
        CompactBriefSignal(
            label = "REVIEWS",
            value = compactReviewState(pr),
            tone = when (pr.reviewDecision?.uppercase()) {
                "APPROVED" -> BriefStatusTone.Positive
                "CHANGES_REQUESTED" -> BriefStatusTone.Negative
                else -> BriefStatusTone.Neutral
            },
        ),
        compactDiscussionSignal(pr),
        compactMergeSignal(pr),
    )
}

private fun compactOwnPeopleSignal(
    status: OwnPullRequestStatus,
    pr: PullRequest,
): CompactBriefSignal? {
    return when (status) {
        OwnPullRequestStatus.ChangesRequested ->
            formatIdentityList(pr.changeRequestReviewers)
                ?.let {
                    CompactBriefSignal(
                        label = "BY",
                        value = it,
                        tone = BriefStatusTone.Negative,
                    )
                }

        OwnPullRequestStatus.WaitingForReviewer ->
            formatIdentityList(pr.requestedReviewers)
                ?.let {
                    CompactBriefSignal(
                        label = "WAITING ON",
                        value = it,
                    )
                }

        OwnPullRequestStatus.ApprovedAndReady ->
            formatIdentityList(pr.approvingReviewers)
                ?.let {
                    CompactBriefSignal(
                        label = "APPROVED BY",
                        value = it,
                        tone = BriefStatusTone.Positive,
                    )
                }

        OwnPullRequestStatus.DiscussionNeedsResponse ->
            formatIdentityList(pr.unresolvedDiscussionAuthors)
                ?.let {
                    CompactBriefSignal(
                        label = "THREADS WITH",
                        value = it,
                        tone = BriefStatusTone.Warning,
                    )
                }

        else -> null
    }
}

private fun compactCiSignal(pr: PullRequest): CompactBriefSignal = when {
    pr.checksFailing > 0 -> CompactBriefSignal(
        label = "CI",
        value = "${pr.checksFailing} failing",
        tone = BriefStatusTone.Negative,
    )

    pr.checksPending > 0 -> CompactBriefSignal(
        label = "CI",
        value = "${pr.checksPending} pending",
        tone = BriefStatusTone.Warning,
    )

    pr.checksTotal > 0 -> CompactBriefSignal(
        label = "CI",
        value = "${pr.checksTotal} passing",
        tone = BriefStatusTone.Positive,
    )

    else -> CompactBriefSignal(
        label = "CI",
        value = "Not reported",
    )
}

private fun compactDiscussionSignal(pr: PullRequest): CompactBriefSignal {
    val unresolved = pr.unresolvedDiscussionCount

    return when {
        unresolved == null -> CompactBriefSignal(
            label = "DISCUSSIONS",
            value = "Unavailable",
        )

        unresolved > 0 -> CompactBriefSignal(
            label = "DISCUSSIONS",
            value = "$unresolved unresolved",
            tone = BriefStatusTone.Warning,
        )

        else -> CompactBriefSignal(
            label = "DISCUSSIONS",
            value = "None open",
            tone = BriefStatusTone.Positive,
        )
    }
}

private fun compactMergeSignal(pr: PullRequest): CompactBriefSignal = when {
    pr.mergeable.equals("CONFLICTING", ignoreCase = true) ||
            pr.mergeStateStatus.equals("DIRTY", ignoreCase = true) ->
        CompactBriefSignal(
            label = "MERGE",
            value = "Conflict",
            tone = BriefStatusTone.Negative,
        )

    pr.mergeable.equals("MERGEABLE", ignoreCase = true) ->
        CompactBriefSignal(
            label = "MERGE",
            value = "Clean",
            tone = BriefStatusTone.Positive,
        )

    else -> CompactBriefSignal(
        label = "MERGE",
        value = "Undetermined",
    )
}

private fun compactReviewState(pr: PullRequest): String {
    return when (pr.reviewDecision?.uppercase()) {
        "APPROVED" ->
            formatIdentityList(pr.approvingReviewers)
                ?.let { "Approved by $it" }
                ?: "Approved"

        "CHANGES_REQUESTED" ->
            formatIdentityList(pr.changeRequestReviewers)
                ?.let { "Changes requested by $it" }
                ?: "Changes requested"

        "REVIEW_REQUIRED" ->
            formatIdentityList(pr.requestedReviewers)
                ?.let { "Waiting on $it" }
                ?: "Review required"

        else -> when {
            pr.reviewRequestsCount > 0 ->
                "${pr.reviewRequestsCount} requested"
            else -> "No decision yet"
        }
    }
}

private fun compactInlineNextAction(
    state: AppState,
    pr: PullRequest,
): String = PullRequestAttention.describe(
    pullRequest = pr,
    handledMarker = state.handledReviewRecords[pr.key],
).nextAction

@Composable
private fun compactNextActionColor(
    state: AppState,
    pr: PullRequest,
): Color {
    if (pr.source == PullRequestSource.ReviewRequest) {
        return when {
            state.isHandledCurrent(pr) -> TextMuted
            pr.checksFailing > 0 -> MaterialTheme.colorScheme.error
            pr.mergeable.equals("CONFLICTING", ignoreCase = true) ||
                    pr.mergeStateStatus.equals("DIRTY", ignoreCase = true) ->
                MaterialTheme.colorScheme.error
            (pr.unresolvedDiscussionCount ?: 0) > 0 -> Amber
            else -> Olive
        }
    }

    return colorForOwnPullRequestStatus(
        PullRequestAttention.describe(pr).ownStatus ?: OwnPullRequestStatus.NoActionNeeded,
    )
}

@Composable
private fun OwnPullRequestHero(pr: PullRequest) {
    val attention = PullRequestAttention.describe(pr)
    val status = attention.ownStatus ?: OwnPullRequestStatus.NoActionNeeded
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
        title = attention.presentation.statusTitle,
        detail = attention.presentation.statusBody,
        accent = colorForOwnPullRequestStatus(status),
    )
}

@Composable
private fun OwnPullRequestSignals(pr: PullRequest) {
    val signals = PullRequestAttention.describe(pr).ownStatusPresentations

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SubtleSectionBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionLabel("SIGNALS")

        signals.forEach { presentation ->
            BriefStatusItem(
                tone = toneForOwnStatus(presentation.status),
                text = presentation.rowStatus,
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
            }
        }

        if (pr.source == PullRequestSource.Mine && state.isPullRequestReadyToMerge(pr)) {
            Button(
                onClick = { state.mergePullRequest(pr) },
                enabled = !state.isMergingPullRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Olive,
                    contentColor = Color(0xFF151812),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (state.mergingPullRequestKey == pr.key) {
                        "Merging…"
                    } else {
                        "Merge pull request"
                    },
                )
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
                shortcut = "C",
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
                shortcut = "P",
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
    shortcut: String? = null,
) {
    val contentColor = if (destructive) Amber else TextPrimary
    val accessibilityLabel = shortcut?.let {
        "$title. Shortcut ${shortcutKeycapPresentation(it).accessibilityLabel}"
    } ?: title

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
            }
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

        shortcut?.let {
            ShortcutKeycaps(
                shortcut = it,
                announce = false,
            )
        }
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
