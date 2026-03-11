package com.github.seijind.fliptowin

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class FlipToWinUiState(
    val items: PersistentList<FlipToWinUiCardData> = persistentListOf(),
    val rewards: PersistentList<FlipToWinUiCardData> = persistentListOf(),
    val winRewardType: Int? = null,
    val config: FlipToWinUiConfig = FlipToWinUiConfig(),
    val isGameActive: Boolean = false,
    val showConfigErrorDialog: Int? = null,
)

@Immutable
data class FlipToWinUiConfig(
    /** Internal wiggle animation timing (defaults to value from API). */
    val wiggleDelay: Long = 3000L,
    /** Whether to reveal unselected cards at the end of a round. */
    val revealAllAtEnd: Boolean = true,
    /** Background brush for the card back. */
    val cardBackBrush: Brush? = null,
    /** Optional image URL/asset for the card back. */
    val cardBackImage: String = "",
    /** Number of columns in the grid. */
    val gridColumns: Int = 3,
)

@Immutable
data class FlipToWinUiCardData(
    // --- Visual content ---
    val type: Int? = null,
    val image: String = "",
    val brush: Brush? = null,
    val bitmap: Bitmap? = null,
    val clickable: Boolean = false,
    // --- Animation triggers ---
    val isWiggling: Boolean = false,
    val isScaling: Boolean = false,
    val moveInCenter: Boolean = false,
    val isFlipped: Boolean = false,
    val isSelected: Boolean = false,
)
