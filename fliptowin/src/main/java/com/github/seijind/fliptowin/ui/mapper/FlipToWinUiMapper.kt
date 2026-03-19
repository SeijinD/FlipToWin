package com.github.seijind.fliptowin.ui.mapper

import com.github.seijind.fliptowin.domain.model.FlipToWinConstants
import com.github.seijind.fliptowin.domain.model.FlipToWinResponse
import com.github.seijind.fliptowin.domain.model.toBrush
import com.github.seijind.fliptowin.ui.model.FlipToWinUiCard
import com.github.seijind.fliptowin.ui.model.FlipToWinUiRewardVisual

internal class FlipToWinUiMapper {

    /**
     * Creates all UI models from the API response.
     */
    fun mapResponse(response: FlipToWinResponse): FlipToWinMappingResult {
        val cardBackBrush = response.config.cardBack.toBrush()
        val cardBackIcon = response.config.cardBack.imageUrl

        val cards = List(response.config.cardCount) {
            FlipToWinUiCard(
                brush = cardBackBrush,
                imageUrl = cardBackIcon,
                isClickable = true,
            )
        }

        val rewardCatalog = response.rewards.map { config ->
            FlipToWinUiRewardVisual(
                type = config.type,
                brush = config.toBrush(),
                imageUrl = config.imageUrl,
            )
        }

        return FlipToWinMappingResult(
            cards = cards,
            rewardCatalog = rewardCatalog,
            winningRewardType = response.winningRewardType,
            wiggleDelay = response.config.wiggleDelayMillis,
            revealAllAtEnd = response.config.revealAllAtEnd,
            gridColumns = response.config.gridColumns,
        )
    }

    /**
     * Fills unselected cards (where [FlipToWinUiCard.type] is null) with reward data
     * for the end-of-game reveal.
     *
     * This logic ensures:
     * 1. The pool of rewards excludes the user's won type to prevent duplicates.
     * 2. At least one losing reward is included if available in the pool.
     * 3. The distribution is shuffled so the player sees a varied grid.
     */
    fun assignRemainingRewards(
        winningRewardType: Int,
        cards: List<FlipToWinUiCard>,
        rewardCatalog: List<FlipToWinUiRewardVisual>,
    ): List<FlipToWinUiCard> {
        val availableRewards = rewardCatalog.filter { it.type != winningRewardType }.toMutableList()
        val losingReward = availableRewards.find { it.type == FlipToWinConstants.LOSING_REWARD_TYPE }

        val emptySlotsCount = cards.count { it.type == null }
        if (emptySlotsCount == 0) return cards

        val fallback = losingReward
            ?: rewardCatalog.firstOrNull()
            ?: return cards

        val distributionList = mutableListOf<FlipToWinUiRewardVisual>().apply {
            losingReward?.let {
                add(it)
                availableRewards.remove(it)
            }

            val remainingNeeded = emptySlotsCount - size
            if (remainingNeeded > 0) {
                if (availableRewards.size >= remainingNeeded) {
                    addAll(availableRewards.shuffled().take(remainingNeeded))
                } else if (availableRewards.isNotEmpty()) {
                    addAll(List(remainingNeeded) { availableRewards.random() })
                } else {
                    addAll(List(remainingNeeded) { fallback })
                }
            }
        }.shuffled()

        val rewardIterator = distributionList.iterator()
        return cards.map { card ->
            if (card.type == null && rewardIterator.hasNext()) {
                val nextReward = rewardIterator.next()
                card.copy(
                    type = nextReward.type,
                    brush = nextReward.brush,
                    imageUrl = nextReward.imageUrl,
                )
            } else {
                card
            }
        }
    }
}

/**
 * Intermediate data structure used during the mapping phase.
 */
internal data class FlipToWinMappingResult(
    val cards: List<FlipToWinUiCard>,
    val rewardCatalog: List<FlipToWinUiRewardVisual>,
    val winningRewardType: Int?,
    val wiggleDelay: Long,
    val revealAllAtEnd: Boolean,
    val gridColumns: Int = 3,
)