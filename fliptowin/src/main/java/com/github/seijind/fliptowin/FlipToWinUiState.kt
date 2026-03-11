package com.github.seijind.fliptowin

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Brush

data class FlipToWinUiState(
    val items: SnapshotStateList<FlipToWinUiCardData> = mutableStateListOf(),
    val rewards: SnapshotStateList<FlipToWinUiCardData> = mutableStateListOf(),
    val winRewardType: MutableState<Int?> = mutableStateOf(null),
    val config: MutableState<FlipToWinUiConfig> = mutableStateOf(FlipToWinUiConfig()),
    val isGameActive: MutableState<Boolean> = mutableStateOf(false),
    val showConfigErrorDialog: MutableState<Int?> = mutableStateOf(null),
    val onCardClicked: (Int) -> Unit,
    val onMoveInCenterAnimationEnded: (Int) -> Unit,
)

data class FlipToWinUiConfig(
    val wiggleDelay: Long = 3000L,
    val revealAllAtEnd: Boolean = true,
    val cardBackBrush: Brush? = null,
    val cardBackImage: String = "",
    val gridColumns: Int = 3,
)

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
