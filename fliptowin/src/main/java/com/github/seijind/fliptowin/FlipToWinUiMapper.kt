package com.github.seijind.fliptowin

class FlipToWinUiMapper {

    /**
     * Creates all UI models from the API response.
     */
    fun mapResponse(response: FlipToWinResponse): FlipToWinUiStateData {
        val cardBackBrush = response.config.cardBack.toBrush()
        val cardBackIcon = response.config.cardBack.imgHistory

        val items = (0 until response.config.cardCount).map {
            FlipToWinUiCardData(
                brush = cardBackBrush,
                image = cardBackIcon,
                clickable = true,
            )
        }

        val rewards = response.rewards.map { config ->
            FlipToWinUiCardData(
                type = config.type,
                brush = config.toBrush(),
                image = config.imgHistory,
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
     * Assigns rewards to all unselected cards (type == null) and returns the updated list.
     * Pure function — does not mutate the input collections.
     */
    fun assignRemainingRewards(
        wonRewardType: Int,
        items: List<FlipToWinUiCardData>,
        rewards: List<FlipToWinUiCardData>,
    ): List<FlipToWinUiCardData> {
        var remainingCount = items.count { it.type == null }
        val mutableContent = rewards.toMutableList()
        val finalContent = mutableListOf<FlipToWinUiCardData>()

        mutableContent.find { it.type == wonRewardType }?.let { mutableContent.remove(it) }

        mutableContent.find { it.type == LOSING_REWARD_TYPE }?.let {
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

        val updatedItems = items.toMutableList()
        outerLoop@ for (reward in finalContent.shuffled()) {
            for (i in updatedItems.indices) {
                if (updatedItems[i].type == null) {
                    updatedItems[i] = updatedItems[i].copy(
                        type = reward.type,
                        brush = reward.brush,
                        bitmap = reward.bitmap,
                        image = reward.image,
                    )
                    continue@outerLoop
                }
            }
        }
        return updatedItems
    }
}

/**
 * Holds all data produced by [FlipToWinUiMapper.mapResponse].
 */
data class FlipToWinUiStateData(
    val items: List<FlipToWinUiCardData>,
    val rewards: List<FlipToWinUiCardData>,
    val winRewardType: Int?,
    val wiggleDelay: Long,
    val revealAllAtEnd: Boolean,
    val gridColumns: Int = 3,
)
