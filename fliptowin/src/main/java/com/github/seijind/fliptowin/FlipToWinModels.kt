package com.github.seijind.fliptowin

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

sealed class FlipToWinResult {
    data class Success(val response: FlipToWinResponse) : FlipToWinResult()
    data class Error(val message: String, val code: Int = 0) : FlipToWinResult()
}

data class FlipToWinResponse(
    val winRewardType: Int?,
    val rewardsConfig: List<FlipToWinRewardType>,
    val cardBackConfig: FlipToWinRewardType
)

data class FlipToWinRewardType(
    val type: Int,
    val startColor: String,
    val endColor: String,
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
    } catch (e: Exception) {
        android.graphics.Color.WHITE
    }

const val CONFIGURATION_ERROR_CODE = 1001
