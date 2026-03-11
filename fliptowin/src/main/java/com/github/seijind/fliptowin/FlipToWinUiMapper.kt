package com.github.seijind.fliptowin

import androidx.compose.runtime.mutableStateOf

class FlipToWinUiMapper {

    fun mapResponse(response: FlipToWinResponse): FlipToWinUiStateData {
        val cardBackBrush = response.cardBackConfig.toBrush()
        val cardBackIcon = response.cardBackConfig.imgHistory

        val items = (0..8).map {
            FlipToWinUiItem(
                type = mutableStateOf(null),
                brush = mutableStateOf(cardBackBrush),
                image = cardBackIcon,
                clickable = mutableStateOf(true)
            )
        }

        val rewards = response.rewardsConfig.map { config ->
            FlipToWinUiItem(
                type = mutableStateOf(config.type),
                brush = mutableStateOf(config.toBrush()),
                image = config.imgHistory
            )
        }

        return FlipToWinUiStateData(
            items = items,
            rewards = rewards,
            winRewardType = response.winRewardType
        )
    }

    operator fun invoke(wonRewardType: Int?, items: List<FlipToWinUiItem>, rewards: List<FlipToWinUiItem>) {
        var remainingCount = 8
        val mutableContent = rewards.toMutableList()
        val finalContent = mutableListOf<FlipToWinUiItem>()

        mutableContent.find { it.type.value == wonRewardType }?.let { mutableContent.remove(it) }

        mutableContent.find { it.type.value == 13 }?.let {
            finalContent.add(it)
            mutableContent.remove(it)
            remainingCount--
        }

        val fillItems = if (mutableContent.size >= remainingCount) {
            mutableContent.shuffled().take(remainingCount)
        } else {
            List(remainingCount) { mutableContent.random() }
        }
        finalContent.addAll(fillItems)

        outerLoop@ for (reward in finalContent.shuffled()) {
            for (item in items) {
                if (item.type.value == null) {
                    item.type.value = reward.type.value
                    item.brush.value = reward.brush.value
                    item.bitmap.value = reward.bitmap.value
                    continue@outerLoop
                }
            }
        }
    }
}

data class FlipToWinUiStateData(
    val items: List<FlipToWinUiItem>,
    val rewards: List<FlipToWinUiItem>,
    val winRewardType: Int?
)
