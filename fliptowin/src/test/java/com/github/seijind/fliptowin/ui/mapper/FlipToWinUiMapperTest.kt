package com.github.seijind.fliptowin.ui.mapper

import com.github.seijind.fliptowin.domain.model.FlipToWinConstants
import com.github.seijind.fliptowin.ui.model.FlipToWinUiCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlipToWinUiMapperTest {

    private val mapper = FlipToWinUiMapper()

    @Test
    fun `assignRemainingRewards should preserve winning card data`() {
        val winningRewardType = 1
        val cards = listOf(
            FlipToWinUiCard(type = winningRewardType, isSelected = true),
            FlipToWinUiCard(type = null)
        )
        val rewardCatalog = listOf(FlipToWinUiCard(type = 2))

        val result = mapper.assignRemainingRewards(winningRewardType, cards, rewardCatalog)

        assertEquals("The winning card's type must remain unchanged", winningRewardType, result[0].type)
        assertTrue("The winning card should still be marked as selected", result[0].isSelected)
    }

    @Test
    fun `assignRemainingRewards should populate all empty slots`() {
        val cards = List(3) { FlipToWinUiCard(type = null) }
        val rewardCatalog = listOf(FlipToWinUiCard(type = 101))

        val result = mapper.assignRemainingRewards(winningRewardType = 999, cards, rewardCatalog)

        assertTrue("All null types should be replaced by reward types", result.all { it.type != null })
    }

    @Test
    fun `assignRemainingRewards should exclude winningRewardType from other slots`() {
        val winningRewardType = 7
        val cards = List(5) { FlipToWinUiCard(type = null) }
        val rewardCatalog = listOf(
            FlipToWinUiCard(type = 7), // Same as winner
            FlipToWinUiCard(type = 8)
        )

        val result = mapper.assignRemainingRewards(winningRewardType, cards, rewardCatalog)

        assertTrue(
            "The winningRewardType should never be assigned to other cards",
            result.all { it.type != winningRewardType }
        )
    }

    @Test
    fun `assignRemainingRewards should prioritize including a losing reward`() {
        val cards = listOf(FlipToWinUiCard(type = null))
        val rewardCatalog = listOf(
            FlipToWinUiCard(type = 10),
            FlipToWinUiCard(type = FlipToWinConstants.LOSING_REWARD_TYPE)
        )

        val result = mapper.assignRemainingRewards(winningRewardType = 1, cards, rewardCatalog)

        assertEquals(
            "If a losing reward exists, it should be the first one used for empty slots",
            FlipToWinConstants.LOSING_REWARD_TYPE, result[0].type
        )
    }

    @Test
    fun `assignRemainingRewards should return original list when no null slots exist`() {
        val cards = listOf(FlipToWinUiCard(type = 1), FlipToWinUiCard(type = 2))
        val rewardCatalog = listOf(FlipToWinUiCard(type = 3))

        val result = mapper.assignRemainingRewards(winningRewardType = 1, cards, rewardCatalog)

        assertEquals("List should be returned as-is if no slots are empty", cards, result)
    }

    @Test
    fun `assignRemainingRewards should handle empty rewards pool gracefully`() {
        val cards = listOf(FlipToWinUiCard(type = null))
        val rewardCatalog = emptyList<FlipToWinUiCard>()

        val result = mapper.assignRemainingRewards(winningRewardType = 1, cards, rewardCatalog)

        assertEquals("Should return the original list if no rewards are available to assign", cards, result)
    }
}
