package com.github.seijind.fliptowin

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            delay(600)

            val mockResult: FlipToWinResult = FlipToWinResult.Success(
                response = FlipToWinResponse(
                    winRewardType = 1,
                    rewardsConfig = listOf(
                        FlipToWinRewardType(1, "#FF5722", "#E64A19", "url_points"),
                        FlipToWinRewardType(2, "#2196F3", "#1976D2", "url_gift"),
                        FlipToWinRewardType(13, "#9E9E9E", "#616161", "url_lost")
                    ),
                    cardBackConfig = FlipToWinRewardType(0, "#EBD197", "#A2790D", "url_back_icon")
                )
            )

            if (mockResult is FlipToWinResult.Success) {
                val data = mockResult.response
                
                if (data.winRewardType == null || data.rewardsConfig.isEmpty()) {
                    _uiState.value.showConfigErrorDialog.value = CONFIGURATION_ERROR_CODE
                    return@launch
                }

                val mappedData = mapper.mapResponse(data)

                _uiState.value.winRewardType.value = mappedData.winRewardType
                
                _uiState.value.rewards.clear()
                _uiState.value.rewards.addAll(mappedData.rewards)

                _uiState.value.items.clear()
                _uiState.value.items.addAll(mappedData.items)

                startWiggleWithDelay()
            }
        }
    }

    private fun startWiggleWithDelay() {
        wiggleJob = viewModelScope.launch {
            delay(3000)
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

                delay(500)
                item.isScaling.value = true
                item.moveInCenter.value = true
            }
        }
    }

    private fun onMoveInCenterAnimationEnded(item: FlipToWinUiItem) {
        val wonReward = _uiState.value.rewards.find { reward -> reward.type.value == item.type.value }
        if (wonReward == null) return

        viewModelScope.launch {
            delay(500)
            item.isFlipped.value = true
            delay(300)
            item.bitmap.value = wonReward.bitmap.value
            item.brush.value = wonReward.brush.value

            delay(2000)
            item.isScaling.value = false
            item.moveInCenter.value = false

            delay(500)
            _uiState.value.items.forEach { card -> card.isFlipped.value = true }
            delay(250)
            mapper(wonReward.type.value ?: 0, _uiState.value.items, _uiState.value.rewards)

            delay(1400)
            _uiState.value.items.forEach { card ->
                if (!card.isSelected.value) {
                    card.isFlipped.value = false
                    card.bitmap.value = null
                    card.brush.value = Brush.verticalGradient(colors = listOf(Color.White, Color.Gray))
                }
            }

            delay(1000)
            _uiState.value.isGameActive.value = false
        }
    }
}
