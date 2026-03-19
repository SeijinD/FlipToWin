package com.github.seijind.fliptowin.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class FlipToWinUiState(
    /** Current state of all cards in the grid. */
    val cards: PersistentList<FlipToWinUiCard> = persistentListOf(),
    /** Visual data for all possible reward outcomes. */
    val rewardCatalog: PersistentList<FlipToWinUiRewardVisual> = persistentListOf(),
    /** The actual reward type to be revealed upon selection. */
    val winningRewardType: Int? = null,
    /** Active game configuration (timing, grid size, etc.). */
    val config: FlipToWinUiConfig = FlipToWinUiConfig(),
    /** If true, the user has selected a card and animations are in progress. */
    val isGameActive: Boolean = false,
)

/**
 * Visual data for a reward entry in the catalog.
 * Used only for end-of-game reveal assignment — contains no animation state.
 */
@Immutable
data class FlipToWinUiRewardVisual(
    val type: Int,
    val imageUrl: String = "",
    val brush: Brush? = null,
)

@Immutable
data class FlipToWinUiCard(
    // --- Visual content ---
    /** Reward type identifier. Null means the card is unrevealed (face-down). */
    val type: Int? = null,
    val imageUrl: String = "",
    val brush: Brush? = null,
    val isClickable: Boolean = false,
    // --- Animation triggers ---
    val isWiggling: Boolean = false,
    val isZoomed: Boolean = false,
    val isCentered: Boolean = false,
    val isFlipped: Boolean = false,
    val isSelected: Boolean = false,
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
    val cardBackImageUrl: String = "",
    /** Number of columns in the grid. */
    val gridColumns: Int = 3,
)