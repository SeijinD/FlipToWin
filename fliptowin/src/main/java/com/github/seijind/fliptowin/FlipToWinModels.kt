package com.github.seijind.fliptowin

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

/**
 * Represents the result of a game initialization or claim attempt.
 */
sealed class FlipToWinResult {
    /** Successful response with full game data. */
    data class Success(val response: FlipToWinResponse) : FlipToWinResult()
    /** Error state with a message and optional numeric code. */
    data class Error(val message: String, val code: Int = 0) : FlipToWinResult()
}

/**
 * The raw response data from the API.
 */
data class FlipToWinResponse(
    val winRewardType: Int?,
    val rewards: List<FlipToWinRewardType>,
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
    /** Icon URL or identifier. */
    val imgHistory: String,
)

fun FlipToWinRewardType.toBrush(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color(this.startColor.convertToIntColor),
            Color(this.endColor.convertToIntColor)
        )
    )
}

val String.convertToIntColor: Int
    get() = try {
        this.toColorInt()
    } catch (e: IllegalArgumentException) {
        android.util.Log.e("FlipToWin", "Invalid color string: \"$this\", falling back to white", e)
        android.graphics.Color.WHITE
    }

const val CONFIGURATION_ERROR_CODE = 1001
const val LOSING_REWARD_TYPE = 0
