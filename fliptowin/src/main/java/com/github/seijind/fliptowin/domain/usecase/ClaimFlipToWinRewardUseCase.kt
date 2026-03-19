package com.github.seijind.fliptowin.domain.usecase

import com.github.seijind.fliptowin.domain.model.FlipToWinClaimResult
import com.github.seijind.fliptowin.domain.repository.FlipToWinRepository
import javax.inject.Inject

/**
 * Use case that claims a FlipToWin reward after the user selects a card.
 */
class ClaimFlipToWinRewardUseCase @Inject constructor(
    private val repository: FlipToWinRepository
) {
    suspend operator fun invoke(rewardType: Int): FlipToWinClaimResult = repository.claimReward(rewardType)
}
