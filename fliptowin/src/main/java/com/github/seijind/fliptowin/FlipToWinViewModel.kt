package com.github.seijind.fliptowin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlipToWinViewModel : ViewModel() {

    private val mapper = FlipToWinUiMapper()

    private val _uiState = MutableStateFlow(FlipToWinUiState(
        onCardClicked = ::onCardClicked,
        onMoveInCenterAnimationEnded = ::onMoveInCenterAnimationEnded
    ))
    val uiState: StateFlow<FlipToWinUiState> = _uiState.asStateFlow()

    private var wiggleJob: Job? = null

    init {
        setup()
    }

    private fun setup() {
        viewModelScope.launch {
            delay(INITIAL_LOAD_DELAY_MS)

            val mockResult: FlipToWinResult = FlipToWinResult.Success(
                response = FlipToWinResponse(
                    winRewardType = 1,
                    rewards = listOf(
                        FlipToWinRewardType(1, "#FF5722", "#E64A19", "url_points"),
                        FlipToWinRewardType(2, "#2196F3", "#1976D2", "url_gift"),
                        FlipToWinRewardType(0, "#9E9E9E", "#616161", "url_lost")
                    ),
                    config = FlipToWinConfig(
                        cardBack = FlipToWinRewardType(0, "#EBD197", "#A2790D", "url_back_icon"),
                        wiggleDelayMillis = 3000L,
                        revealAllAtEnd = false,
                    )
                )
            )

            when (mockResult) {
                is FlipToWinResult.Success -> {
                    val data = mockResult.response

                    if (data.winRewardType == null || data.rewards.isEmpty()) {
                        _uiState.value.showConfigErrorDialog.value = CONFIGURATION_ERROR_CODE
                        return@launch
                    }

                    val mappedData = mapper.mapResponse(data)

                    _uiState.value.winRewardType.value = mappedData.winRewardType
                    
                    _uiState.value.config.value = FlipToWinUiConfig(
                        wiggleDelay = mappedData.wiggleDelay,
                        revealAllAtEnd = mappedData.revealAllAtEnd,
                        cardBackBrush = data.config.cardBack.toBrush(),
                        cardBackImage = data.config.cardBack.imgHistory,
                        gridColumns = mappedData.gridColumns,
                    )

                    _uiState.value.rewards.clear()
                    _uiState.value.rewards.addAll(mappedData.rewards)

                    _uiState.value.items.clear()
                    _uiState.value.items.addAll(mappedData.items)

                    startWiggleWithDelay()
                }
                is FlipToWinResult.Error -> {
                    _uiState.value.showConfigErrorDialog.value = mockResult.code
                    return@launch
                }
            }
        }
    }

    private fun startWiggleWithDelay() {
        wiggleJob = viewModelScope.launch {
            delay(_uiState.value.config.value.wiggleDelay)
            _uiState.value.items.forEach { item -> item.isWiggling.value = true }
        }
    }

    private fun onCardClicked(item: FlipToWinUiItem) {
        viewModelScope.launch {
            if (!_uiState.value.isGameActive.value) {
                _uiState.value.isGameActive.value = true
                wiggleJob?.cancel()
                _uiState.value.items.forEach { card ->
                    card.isWiggling.value = false
                    card.clickable.value = false
                }

                val claimSuccess = true
                if (!claimSuccess) {
                    _uiState.value.items.forEach { card -> card.clickable.value = true }
                    _uiState.value.isGameActive.value = false
                    startWiggleWithDelay()
                    return@launch
                }

                item.isSelected.value = true
                _uiState.value.winRewardType.value?.let { type -> item.type.value = type }

                delay(BEFORE_SELECTED_CARD_MOVE_MS)
                item.isScaling.value = true
                item.moveInCenter.value = true
            }
        }
    }

    private fun onMoveInCenterAnimationEnded(item: FlipToWinUiItem) {
        val wonReward = _uiState.value.rewards.find { reward -> reward.type.value == item.type.value }
        if (wonReward == null) return

        viewModelScope.launch {
            delay(BEFORE_SELECTED_CARD_FLIP_MS)
            item.isFlipped.value = true

            // Wait for card to reach 90° (midpoint of flip) before swapping the image
            delay(CARD_FLIP_MIDPOINT_MS)
            item.image.value = wonReward.image.value
            item.bitmap.value = wonReward.bitmap.value
            item.brush.value = wonReward.brush.value

            delay(SHOW_RESULT_MS)
            item.isScaling.value = false
            item.moveInCenter.value = false

            delay(BEFORE_REVEAL_ALL_MS)
            _uiState.value.items.forEach { card ->
                if (!card.isSelected.value) {
                    card.isFlipped.value = true
                }
            }

            // Wait for remaining cards to reach 90° before assigning their reward visuals
            delay(CARD_FLIP_MIDPOINT_MS)
            mapper.assignRemainingRewards(wonReward.type.value ?: 0, _uiState.value.items, _uiState.value.rewards)

            delay(AFTER_REVEAL_ALL_MS)

            if (!_uiState.value.config.value.revealAllAtEnd) {
                _uiState.value.items.forEach { card ->
                    if (!card.isSelected.value) {
                        card.isFlipped.value = false
                    }
                }

                // Wait for cards to reach 90° before resetting their visuals to card back
                delay(CARD_FLIP_MIDPOINT_MS)
                _uiState.value.items.forEach { card ->
                    if (!card.isSelected.value) {
                        card.image.value = _uiState.value.config.value.cardBackImage
                        card.brush.value = _uiState.value.config.value.cardBackBrush
                        card.bitmap.value = null
                    }
                }
            }

            delay(BEFORE_GAME_RESET_MS)
            _uiState.value.isGameActive.value = false
        }
    }

    companion object {
        /** Initial delay before loading game data, allows the UI to settle. */
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

