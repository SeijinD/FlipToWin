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
    val wiggleDelay: Long = 3000L,
    val revealAllAtEnd: Boolean = true,
    val cardBackBrush: Brush? = null,
    val cardBackImage: String = "",
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
