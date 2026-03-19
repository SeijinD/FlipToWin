package com.github.seijind.fliptowin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.seijind.fliptowin.domain.model.FlipToWinClaimResult
import com.github.seijind.fliptowin.domain.model.FlipToWinConstants
import com.github.seijind.fliptowin.domain.model.FlipToWinLoadResult
import com.github.seijind.fliptowin.domain.model.toBrush
import com.github.seijind.fliptowin.domain.usecase.ClaimFlipToWinRewardUseCase
import com.github.seijind.fliptowin.domain.usecase.GetFlipToWinGameUseCase
import com.github.seijind.fliptowin.ui.mapper.FlipToWinUiMapper
import com.github.seijind.fliptowin.ui.model.FlipToWinUiCard
import com.github.seijind.fliptowin.ui.model.FlipToWinUiConfig
import com.github.seijind.fliptowin.ui.model.FlipToWinUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for orchestrating the FlipToWin game logic.
 *
 * It manages the immutable [FlipToWinUiState], handles one-time [resultEvent] emissions,
 * and processes user interactions such as card clicks.
 *
 * @param getGameUseCase Use case to fetch game configuration from the repository.
 * @param claimRewardUseCase Use case to claim a reward after card selection.
 */
@HiltViewModel
class FlipToWinViewModel @Inject constructor(
    private val getGameUseCase: GetFlipToWinGameUseCase,
    private val claimRewardUseCase: ClaimFlipToWinRewardUseCase,
) : ViewModel() {

    private val mapper = FlipToWinUiMapper()

    private val _uiState = MutableStateFlow(FlipToWinUiState())
    val uiState: StateFlow<FlipToWinUiState> = _uiState.asStateFlow()

    private val _resultEvent = MutableSharedFlow<Int>()
    val resultEvent: SharedFlow<Int> = _resultEvent.asSharedFlow()

    private val _claimErrorEvent = MutableSharedFlow<String>()
    val claimErrorEvent: SharedFlow<String> = _claimErrorEvent.asSharedFlow()

    private val _errorEvent = MutableSharedFlow<Int>()
    val errorEvent: SharedFlow<Int> = _errorEvent.asSharedFlow()

    private var initJob: Job? = null
    private var wiggleJob: Job? = null

    /**
     * Loads the game configuration from the repository using [getGameUseCase].
     *
     * @param force If true, forces a re-fetch and re-initialization even if the game is already loaded.
     */
    fun loadGame(force: Boolean = false) {
        if (!force && _uiState.value.cards.isNotEmpty()) return

        initJob?.cancel()
        wiggleJob?.cancel()

        _uiState.update { FlipToWinUiState() }

        initJob = viewModelScope.launch {
            delay(INITIAL_LOAD_DELAY_MS)
            handleGameResult(getGameUseCase())
        }
    }

    private suspend fun handleGameResult(result: FlipToWinLoadResult) {
        when (result) {
            is FlipToWinLoadResult.Success -> {
                val data = result.response
                if (data.winningRewardType == null || data.rewards.isEmpty()) {
                    _errorEvent.emit(FlipToWinConstants.CONFIGURATION_ERROR_CODE)
                    return
                }
                val mappedData = mapper.mapResponse(data)
                _uiState.update {
                    it.copy(
                        winningRewardType = mappedData.winningRewardType,
                        config = FlipToWinUiConfig(
                            wiggleDelay = mappedData.wiggleDelay,
                            revealAllAtEnd = mappedData.revealAllAtEnd,
                            cardBackBrush = data.config.cardBack.toBrush(),
                            cardBackImageUrl = data.config.cardBack.imageUrl,
                            gridColumns = mappedData.gridColumns,
                        ),
                        rewardCatalog = mappedData.rewardCatalog.toPersistentList(),
                        cards = mappedData.cards.toPersistentList(),
                    )
                }
                startWiggleWithDelay()
            }
            is FlipToWinLoadResult.Error -> {
                _errorEvent.emit(result.code)
            }
        }
    }

    private fun startWiggleWithDelay() {
        wiggleJob = viewModelScope.launch {
            delay(_uiState.value.config.wiggleDelay)
            updateCards { it.copy(isWiggling = true) }
        }
    }

    fun onCardClicked(index: Int) {
        viewModelScope.launch {
            if (_uiState.value.cards.getOrNull(index) == null) return@launch
            if (!_uiState.value.isGameActive) {
                _uiState.update { it.copy(isGameActive = true) }
                wiggleJob?.cancel()
                updateCards { it.copy(isWiggling = false, isClickable = false) }

                val winType = _uiState.value.winningRewardType ?: return@launch

                // Call claim API via use case
                when (val claimResult = claimRewardUseCase(winType)) {
                    is FlipToWinClaimResult.Success -> {
                        // Claim succeeded — proceed with the animation flow
                        updateCard(index) { it.copy(isSelected = true, type = winType) }

                        delay(BEFORE_SELECTED_CARD_MOVE_MS)
                        updateCard(index) { it.copy(isZoomed = true, isCentered = true) }
                    }

                    is FlipToWinClaimResult.Error -> {
                        // Claim failed — reset game state so user can retry
                        _uiState.update { it.copy(isGameActive = false) }
                        updateCards { it.copy(isClickable = true) }
                        startWiggleWithDelay()
                        _claimErrorEvent.emit(claimResult.message)
                    }
                }
            }
        }
    }

    /**
     * Orchestrates the sequential animation steps after the selected card reaches the center.
     *
     * Flow:
     * 1. Failsafe checks for card/reward existence.
     * 2. Flips the selected card and swaps the card-back for the actual reward image.
     * 3. Displays the result for a set duration.
     * 4. Moves the card back and reveals all other cards (if configured).
     * 5. Resets game state and emits the final result event.
     *
     * @param index The index of the card that just finished its translation animation.
     */
    fun onCenteringAnimationEnded(index: Int) {
        viewModelScope.launch {
            val card = _uiState.value.cards.getOrNull(index) ?: return@launch
            val wonReward = _uiState.value.rewardCatalog.find { it.type == card.type } ?: return@launch

            delay(BEFORE_SELECTED_CARD_FLIP_MS)
            updateCard(index) { it.copy(isFlipped = true) }

            delay(CARD_FLIP_MIDPOINT_MS)
            updateCard(index) {
                it.copy(imageUrl = wonReward.imageUrl, brush = wonReward.brush)
            }

            delay(SHOW_RESULT_MS)
            updateCard(index) { it.copy(isZoomed = false, isCentered = false) }

            delay(BEFORE_REVEAL_ALL_MS)
            updateCards { card -> if (!card.isSelected) card.copy(isFlipped = true) else card }

            delay(CARD_FLIP_MIDPOINT_MS)
            val assignedCards = mapper.assignRemainingRewards(
                wonReward.type,
                _uiState.value.cards,
                _uiState.value.rewardCatalog,
            )
            _uiState.update { it.copy(cards = assignedCards.toPersistentList()) }

            delay(AFTER_REVEAL_ALL_MS)

            if (!_uiState.value.config.revealAllAtEnd) {
                updateCards { card -> if (!card.isSelected) card.copy(isFlipped = false) else card }

                delay(CARD_FLIP_MIDPOINT_MS)
                val cardBackImageUrl = _uiState.value.config.cardBackImageUrl
                val cardBackBrush = _uiState.value.config.cardBackBrush
                updateCards { card ->
                    if (!card.isSelected) card.copy(
                        imageUrl = cardBackImageUrl,
                        brush = cardBackBrush,
                    ) else card
                }
            }

            delay(BEFORE_GAME_RESET_MS)
            val wonType = card.type ?: 0
            _uiState.update { it.copy(isGameActive = false) }
            _resultEvent.emit(wonType)
        }
    }

    /** Applies [transform] to every card and emits a new state. */
    private fun updateCards(transform: (FlipToWinUiCard) -> FlipToWinUiCard) {
        _uiState.update { state ->
            state.copy(
                cards = state.cards.mutate { list ->
                    for (i in list.indices) {
                        list[i] = transform(list[i])
                    }
                }
            )
        }
    }

    /** Applies [transform] to the card at [index] and emits a new state. */
    private fun updateCard(index: Int, transform: (FlipToWinUiCard) -> FlipToWinUiCard) {
        _uiState.update { state ->
            if (index !in state.cards.indices) return@update state
            state.copy(cards = state.cards.set(index, transform(state.cards[index])))
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