package com.github.seijind.fliptowin.domain.repository

import com.github.seijind.fliptowin.domain.model.FlipToWinClaimResult
import com.github.seijind.fliptowin.domain.model.FlipToWinLoadResult

/**
 * Contract for fetching game data and claiming rewards.
 *
 * Implementations may hit a real network API or return mock data for demos.
 */
interface FlipToWinRepository {

    /**
     * Fetches the current game configuration and available rewards.
     */
    suspend fun getGame(): FlipToWinLoadResult

    /**
     * Attempts to claim the reward after the user selects a card.
     *
     * @param rewardType The type identifier of the reward being claimed.
     */
    suspend fun claimReward(rewardType: Int): FlipToWinClaimResult
}
