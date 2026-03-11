package com.github.seijind.fliptowin

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

sealed class FlipToWinResult {
    data class Success(val response: FlipToWinResponse) : FlipToWinResult()
    data class Error(val message: String, val code: Int = 0) : FlipToWinResult()
}

/**
 * The full response from the API
 */
data class FlipToWinResponse(
    val winRewardType: Int?,
    val rewards: List<FlipToWinRewardType>,
    val config: FlipToWinConfig
)

/**
 * General game configuration
 */
data class FlipToWinConfig(
    val cardBack: FlipToWinRewardType,
    val wiggleDelayMillis: Long = 3000L,
    val revealAllAtEnd: Boolean = true,
    val cardCount: Int = 9,
    val gridColumns: Int = 3,
)

/**
 * Visual data for rewards and card back
 */
data class FlipToWinRewardType(
    val type: Int,
    val startColor: String,
    val endColor: String,
    val imgHistory: String,
)

/**
 * Helper to convert config to Brush
 */
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
