package com.github.seijind.fliptowin

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Brush

data class FlipToWinUiState(
    val items: SnapshotStateList<FlipToWinUiItem> = mutableStateListOf(),
    val rewards: SnapshotStateList<FlipToWinUiItem> = mutableStateListOf(),
    val winRewardType: MutableState<Int?> = mutableStateOf(null),
    val config: MutableState<FlipToWinUiConfig> = mutableStateOf(FlipToWinUiConfig()),
    val isGameActive: MutableState<Boolean> = mutableStateOf(false),
    val showConfigErrorDialog: MutableState<Int?> = mutableStateOf(null),
    val onCardClicked: (FlipToWinUiItem) -> Unit,
    val onMoveInCenterAnimationEnded: (FlipToWinUiItem) -> Unit,
)

data class FlipToWinUiConfig(
    val wiggleDelay: Long = 3000L,
    val revealAllAtEnd: Boolean = true,
    val cardBackBrush: Brush? = null,
    val cardBackImage: String = "",
    val gridColumns: Int = 3,
)

data class FlipToWinUiItem(
    val image: MutableState<String> = mutableStateOf(""),
    val type: MutableState<Int?> = mutableStateOf(null),
    val brush: MutableState<Brush?> = mutableStateOf(null),
    val bitmap: MutableState<Bitmap?> = mutableStateOf(null),
    val isFlipped: MutableState<Boolean> = mutableStateOf(false),
    val isWiggling: MutableState<Boolean> = mutableStateOf(false),
    val isScaling: MutableState<Boolean> = mutableStateOf(false),
    val moveInCenter: MutableState<Boolean> = mutableStateOf(false),
    val isSelected: MutableState<Boolean> = mutableStateOf(false),
    val clickable: MutableState<Boolean> = mutableStateOf(false),
)
