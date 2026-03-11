package com.github.seijind.fliptowin

import androidx.compose.runtime.mutableStateOf

class FlipToWinUiMapper {

    /**
     * Creates all UI models from the API response
     */
    fun mapResponse(response: FlipToWinResponse): FlipToWinUiStateData {
        // 1. Mapping for the card back side
        val cardBackBrush = response.config.cardBack.toBrush()
        val cardBackIcon = response.config.cardBack.imgHistory

        // 2. Creating the 9 initial cards
        val items = (0..8).map {
            FlipToWinUiItem(
                type = mutableStateOf(null),
                brush = mutableStateOf(cardBackBrush),
                image = mutableStateOf(cardBackIcon),
                clickable = mutableStateOf(true)
            )
        }

        // 3. Mapping of the available rewards
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
            revealAllAtEnd = response.config.revealAllAtEnd
        )
    }

    /**
     * Reveals the remaining cards at the end of the game
     */
    operator fun invoke(wonRewardType: Int?, items: List<FlipToWinUiItem>, rewards: List<FlipToWinUiItem>) {
        var remainingCount = 8
        val mutableContent = rewards.toMutableList()
        val finalContent = mutableListOf<FlipToWinUiItem>()

        // Remove the won reward to avoid duplicates
        mutableContent.find { it.type.value == wonRewardType }?.let { mutableContent.remove(it) }

        // Always include a "Lost" reward (type 13) if available
        mutableContent.find { it.type.value == 13 }?.let {
            finalContent.add(it)
            mutableContent.remove(it)
            remainingCount--
        }

        // Fill remaining slots randomly
        val fillItems = if (mutableContent.size >= remainingCount) {
            mutableContent.shuffled().take(remainingCount)
        } else {
            List(remainingCount) { mutableContent.random() }
        }
        finalContent.addAll(fillItems)

        // Assign to cards that haven't been revealed yet
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

/**
 * Data class to hold all mapped data from the response
 */
data class FlipToWinUiStateData(
    val items: List<FlipToWinUiItem>,
    val rewards: List<FlipToWinUiItem>,
    val winRewardType: Int?,
    val wiggleDelay: Long,
    val revealAllAtEnd: Boolean,
)
