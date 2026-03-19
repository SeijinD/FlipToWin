package com.github.seijind.fliptowin.data.repository

import com.github.seijind.fliptowin.domain.model.FlipToWinClaimResult
import com.github.seijind.fliptowin.domain.model.FlipToWinConfig
import com.github.seijind.fliptowin.domain.model.FlipToWinConstants
import com.github.seijind.fliptowin.domain.model.FlipToWinLoadResult
import com.github.seijind.fliptowin.domain.model.FlipToWinResponse
import com.github.seijind.fliptowin.domain.model.FlipToWinRewardType
import com.github.seijind.fliptowin.domain.repository.FlipToWinRepository
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Mock implementation of [FlipToWinRepository] that simulates network behaviour for demo purposes.
 *
 * - [getGame] returns a realistic response with multiple reward types after a simulated delay.
 * - [claimReward] confirms the reward after a simulated delay.
 * - Both methods occasionally return errors ([errorRate]) to showcase error-handling flows.
 *
 * @param errorRate Probability (0f–1f) of a simulated failure. Default is 15 %.
 */
class MockFlipToWinRepository(
    private val errorRate: Float = 0.15f,
) : FlipToWinRepository {

    override suspend fun getGame(): FlipToWinLoadResult {
        delay(800L) // simulate network
        // Occasional failure to demo error flow
        if (Random.nextFloat() < errorRate) {
            return FlipToWinLoadResult.Error(
                message = "Unable to load game. Please try again.",
                code = MOCK_NETWORK_ERROR_CODE,
            )
        }

        val winningRewardType = REWARD_TYPES.filter { it.type != FlipToWinConstants.LOSING_REWARD_TYPE }.random().type

        return FlipToWinLoadResult.Success(
            response = FlipToWinResponse(
                winningRewardType = winningRewardType,
                rewards = REWARD_TYPES,
                config = FlipToWinConfig(
                    cardBack = CARD_BACK,
                    wiggleDelayMillis = 3000L,
                    revealAllAtEnd = false,
                    cardCount = 9,
                    gridColumns = 3,
                ),
            )
        )
    }

    override suspend fun claimReward(rewardType: Int): FlipToWinClaimResult {
        // Occasional failure to demo error / retry flow
        if (Random.nextFloat() < errorRate) {
            return FlipToWinClaimResult.Error(
                message = "Claim failed. Please try again.",
                code = MOCK_CLAIM_ERROR_CODE,
            )
        }

        return FlipToWinClaimResult.Success(rewardType = rewardType)
    }

    companion object {
        private const val MOCK_NETWORK_ERROR_CODE = 5001
        private const val MOCK_CLAIM_ERROR_CODE = 5002

        private val CARD_BACK = FlipToWinRewardType(
            type = -1,
            startColor = "#EBD197",
            endColor = "#A2790D",
            imageUrl = "https://cdn-icons-png.flaticon.com/512/2583/2583345.png",
        )

        private val REWARD_TYPES = listOf(
            FlipToWinRewardType(
                type = 1,
                startColor = "#FF9800",
                endColor = "#E65100",
                imageUrl = "https://cdn-icons-png.flaticon.com/512/2583/2583344.png",
            ),
            FlipToWinRewardType(
                type = 2,
                startColor = "#4CAF50",
                endColor = "#1B5E20",
                imageUrl = "https://cdn-icons-png.flaticon.com/512/4221/4221267.png",
            ),
            FlipToWinRewardType(
                type = 3,
                startColor = "#2196F3",
                endColor = "#0D47A1",
                imageUrl = "https://cdn-icons-png.flaticon.com/512/3132/3132693.png",
            ),
            FlipToWinRewardType(
                type = FlipToWinConstants.LOSING_REWARD_TYPE,
                startColor = "#9E9E9E",
                endColor = "#424242",
                imageUrl = "https://cdn-icons-png.flaticon.com/512/753/753345.png",
            ),
        )
    }
}
