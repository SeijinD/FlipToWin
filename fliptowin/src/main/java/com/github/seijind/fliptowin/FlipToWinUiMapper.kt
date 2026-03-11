package com.github.seijind.fliptowin

import androidx.compose.runtime.mutableStateOf

class FlipToWinUiMapper {

    /**
     * Creates all UI models from the API response
     */
    fun mapResponse(response: FlipToWinResponse): FlipToWinUiStateData {
        val cardBackBrush = response.config.cardBack.toBrush()
        val cardBackIcon = response.config.cardBack.imgHistory

        val items = (0 until response.config.cardCount).map {
            FlipToWinUiItem(
                type = mutableStateOf(null),
                brush = mutableStateOf(cardBackBrush),
                image = mutableStateOf(cardBackIcon),
                clickable = mutableStateOf(true)
            )
        }

        val rewards = response.rewards.map { config ->
            FlipToWinUiItem(
                type = mutableStateOf(config.type),
                brush = mutableStateOf(config.toBrush()),
                image = mutableStateOf(config.imgHistory)
            )
        }

        return FlipToWinUiStateData(
            items = items,
            rewards = rewards,
            winRewardType = response.winRewardType,
            wiggleDelay = response.config.wiggleDelayMillis,
            revealAllAtEnd = response.config.revealAllAtEnd,
            gridColumns = response.config.gridColumns,
        )
    }

    /**
     * Reveals the remaining cards at the end of the game
     */
    fun assignRemainingRewards(wonRewardType: Int?, items: List<FlipToWinUiItem>, rewards: List<FlipToWinUiItem>) {
        var remainingCount = items.count { it.type.value == null }
        val mutableContent = rewards.toMutableList()
        val finalContent = mutableListOf<FlipToWinUiItem>()

        mutableContent.find { it.type.value == wonRewardType }?.let { mutableContent.remove(it) }

        mutableContent.find { it.type.value == LOSING_REWARD_TYPE }?.let {
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
                    item.image.value = reward.image.value
                    continue@outerLoop
                }
            }
        }
    }
}

/**
 * Data class to hold all mapped data from the response
 */
data class FlipToWinUiStateData(
    val items: List<FlipToWinUiItem>,
    val rewards: List<FlipToWinUiItem>,
    val winRewardType: Int?,
    val wiggleDelay: Long,
    val revealAllAtEnd: Boolean,
    val gridColumns: Int = 3,
)
