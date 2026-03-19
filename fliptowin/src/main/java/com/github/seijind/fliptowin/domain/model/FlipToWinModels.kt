package com.github.seijind.fliptowin.domain.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

/**
 * Represents the result of a game initialization fetching process.
 */
sealed class FlipToWinLoadResult {
    /** Successful response with full game data. */
    data class Success(val response: FlipToWinResponse) : FlipToWinLoadResult()
    /** Error state with a message and optional numeric code. */
    data class Error(val message: String, val code: Int = 0) : FlipToWinLoadResult()
}

/**
 * Represents the result of claiming a reward after card selection.
 */
sealed class FlipToWinClaimResult {
    /** Reward successfully claimed, confirming the [rewardType]. */
    data class Success(val rewardType: Int) : FlipToWinClaimResult()
    /** Claim failed with a message and optional numeric code. */
    data class Error(val message: String, val code: Int = 0) : FlipToWinClaimResult()
}

/**
 * The raw response data from the API.
 */
data class FlipToWinResponse(
    /** The type of reward that the user is guaranteed to win in this round. */
    val winningRewardType: Int?,
    /** List of all available reward types for visualization. */
    val rewards: List<FlipToWinRewardType>,
    /** Game-specific visual and behavior configuration. */
    val config: FlipToWinConfig
)

/**
 * General game configuration settings.
 */
data class FlipToWinConfig(
    /** Icon and brush for the card back (face-down state). */
    val cardBack: FlipToWinRewardType,
    /** Delay before cards start wiggling to grab attention. */
    val wiggleDelayMillis: Long = 3000L,
    /** If true, reveal all remaining cards when a prize is claimed. */
    val revealAllAtEnd: Boolean = true,
    /** Total number of cards to display. */
    val cardCount: Int = 9,
    /** Number of columns in the grid. */
    val gridColumns: Int = 3,
)

/**
 * Visual data for a specific reward type or the card back.
 */
data class FlipToWinRewardType(
    /** Unique identifier for this reward type. Use -1 or 0 for "No Win". */
    val type: Int,
    /** Hex color code for the start of the gradient (e.g., "#FFFFFF"). */
    val startColor: String,
    /** Hex color code for the end of the gradient. */
    val endColor: String,
    /** Remote URL or local identifier for the reward icon. */
    val imageUrl: String,
)

internal fun FlipToWinRewardType.toBrush(): Brush =
    Brush.linearGradient(
        colors = listOf(
            Color(startColor.convertToIntColor),
            Color(endColor.convertToIntColor),
        )
    )

internal val String.convertToIntColor: Int
    get() = runCatching { toColorInt() }.getOrDefault(0xFFFFFFFF.toInt())

/**
 * Shared constants for the FlipToWin domain and UI layers.
 */
object FlipToWinConstants {
    const val CONFIGURATION_ERROR_CODE = 1001
    const val LOSING_REWARD_TYPE = 0
}
