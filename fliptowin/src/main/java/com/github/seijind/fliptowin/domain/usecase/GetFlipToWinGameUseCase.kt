package com.github.seijind.fliptowin.domain.usecase

import com.github.seijind.fliptowin.domain.model.FlipToWinLoadResult
import com.github.seijind.fliptowin.domain.repository.FlipToWinRepository
import javax.inject.Inject

/**
 * Use case that retrieves the current FlipToWin game configuration and rewards.
 */
class GetFlipToWinGameUseCase @Inject constructor(
    private val repository: FlipToWinRepository,
) {
    suspend operator fun invoke(): FlipToWinLoadResult = repository.getGame()
}
