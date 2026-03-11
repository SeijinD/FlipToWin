package com.github.seijind.fliptowin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FlipToWinViewModel : ViewModel() {

    private val mapper = FlipToWinUiMapper()

    private val _uiState = MutableStateFlow(FlipToWinUiState())
    val uiState: StateFlow<FlipToWinUiState> = _uiState.asStateFlow()

    private var wiggleJob: Job? = null

    /**
     * Entry point for the game. Call this after receiving the API response.
     * Safe to call multiple times — each call resets the game state.
     */
    fun init(result: FlipToWinResult) {
        wiggleJob?.cancel()

        _uiState.update { 
            FlipToWinUiState().copy(showConfigErrorDialog = null) 
        }

        viewModelScope.launch {
            delay(INITIAL_LOAD_DELAY_MS)

            when (result) {
                is FlipToWinResult.Success -> {
                    val data = result.response

                    if (data.winRewardType == null || data.rewards.isEmpty()) {
                        _uiState.update { it.copy(showConfigErrorDialog = CONFIGURATION_ERROR_CODE) }
                        return@launch
                    }

                    val mappedData = mapper.mapResponse(data)

                    _uiState.update {
                        it.copy(
                            winRewardType = mappedData.winRewardType,
                            config = FlipToWinUiConfig(
                                wiggleDelay = mappedData.wiggleDelay,
                                revealAllAtEnd = mappedData.revealAllAtEnd,
                                cardBackBrush = data.config.cardBack.toBrush(),
                                cardBackImage = data.config.cardBack.imgHistory,
                                gridColumns = mappedData.gridColumns,
                            ),
                            rewards = mappedData.rewards.toPersistentList(),
                            items = mappedData.items.toPersistentList(),
                        )
                    }

                    startWiggleWithDelay()
                }
                is FlipToWinResult.Error -> {
                    _uiState.update { it.copy(showConfigErrorDialog = result.code) }
                }
            }
        }
    }

    private fun startWiggleWithDelay() {
        wiggleJob = viewModelScope.launch {
            delay(_uiState.value.config.wiggleDelay)
            updateAllCards { it.copy(isWiggling = true) }
        }
    }

    fun onCardClicked(index: Int) {
        viewModelScope.launch {
            if (_uiState.value.items.getOrNull(index) == null) return@launch
            if (!_uiState.value.isGameActive) {
                _uiState.update { it.copy(isGameActive = true) }
                wiggleJob?.cancel()
                updateAllCards { it.copy(isWiggling = false, clickable = false) }

                val claimSuccess = true
                if (!claimSuccess) {
                    updateAllCards { it.copy(clickable = true) }
                    _uiState.update { it.copy(isGameActive = false) }
                    startWiggleWithDelay()
                    return@launch
                }

                val winType = _uiState.value.winRewardType
                updateCard(index) { it.copy(isSelected = true, type = winType ?: it.type) }

                delay(BEFORE_SELECTED_CARD_MOVE_MS)
                updateCard(index) { it.copy(isScaling = true, moveInCenter = true) }
            }
        }
    }

    fun onMoveInCenterAnimationEnded(index: Int) {
        val item = _uiState.value.items.getOrNull(index) ?: return
        val wonReward = _uiState.value.rewards.find { it.type == item.type } ?: return

        viewModelScope.launch {
            delay(BEFORE_SELECTED_CARD_FLIP_MS)
            updateCard(index) { it.copy(isFlipped = true) }

            // Wait for card to reach 90° (midpoint of flip) before swapping the image
            delay(CARD_FLIP_MIDPOINT_MS)
            updateCard(index) {
                it.copy(
                    image = wonReward.image,
                    bitmap = wonReward.bitmap,
                    brush = wonReward.brush,
                )
            }

            delay(SHOW_RESULT_MS)
            updateCard(index) { it.copy(isScaling = false, moveInCenter = false) }

            delay(BEFORE_REVEAL_ALL_MS)
            updateAllCards { card -> if (!card.isSelected) card.copy(isFlipped = true) else card }

            // Wait for remaining cards to reach 90° before assigning their reward visuals
            delay(CARD_FLIP_MIDPOINT_MS)
            val assignedItems = mapper.assignRemainingRewards(
                wonReward.type ?: 0,
                _uiState.value.items,
                _uiState.value.rewards,
            )
            _uiState.update { it.copy(items = assignedItems.toPersistentList()) }

            delay(AFTER_REVEAL_ALL_MS)

            if (!_uiState.value.config.revealAllAtEnd) {
                updateAllCards { card -> if (!card.isSelected) card.copy(isFlipped = false) else card }

                // Wait for cards to reach 90° before resetting their visuals to card back
                delay(CARD_FLIP_MIDPOINT_MS)
                val cardBackImage = _uiState.value.config.cardBackImage
                val cardBackBrush = _uiState.value.config.cardBackBrush
                updateAllCards { card ->
                    if (!card.isSelected) card.copy(
                        image = cardBackImage,
                        brush = cardBackBrush,
                        bitmap = null,
                    ) else card
                }
            }

            delay(BEFORE_GAME_RESET_MS)
            _uiState.update { it.copy(isGameActive = false) }
        }
    }

    /** Applies [transform] to every card and emits a new state. */
    private fun updateAllCards(transform: (FlipToWinUiCardData) -> FlipToWinUiCardData) {
        _uiState.update { state ->
            state.copy(
                items = state.items.mutate { list ->
                    for (i in list.indices) {
                        list[i] = transform(list[i])
                    }
                }
            )
        }
    }

    /** Applies [transform] to the card at [index] and emits a new state. */
    private fun updateCard(index: Int, transform: (FlipToWinUiCardData) -> FlipToWinUiCardData) {
        _uiState.update { state ->
            if (index !in state.items.indices) return@update state
            state.copy(items = state.items.set(index, transform(state.items[index])))
        }
    }

    companion object {
        /** Initial delay before processing the result, allows the UI to settle. */
        private const val INITIAL_LOAD_DELAY_MS = 600L

        /** Delay before the selected card starts scaling and moving to center. */
        private const val BEFORE_SELECTED_CARD_MOVE_MS = 500L

        /** Delay after the card reaches center before starting its flip animation. */
        private const val BEFORE_SELECTED_CARD_FLIP_MS = 500L

        /**
         * Half the flip animation duration (flip anim = 700ms).
         * Used to time image/brush swaps at the invisible midpoint of a flip (rotationY = 90°).
         */
        private const val CARD_FLIP_MIDPOINT_MS = 350L

        /** How long the revealed reward is shown to the user before the grid resets. */
        private const val SHOW_RESULT_MS = 2000L

        /** Delay before flipping the remaining (non-selected) cards. */
        private const val BEFORE_REVEAL_ALL_MS = 500L

        /** Delay after assigning remaining card visuals, waiting for flip animation to complete. */
        private const val AFTER_REVEAL_ALL_MS = 650L

        /** Final pause before resetting isGameActive, giving the user time to see the result. */
        private const val BEFORE_GAME_RESET_MS = 1000L
    }
}
