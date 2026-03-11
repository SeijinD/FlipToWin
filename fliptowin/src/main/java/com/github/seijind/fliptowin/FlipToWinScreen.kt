package com.github.seijind.fliptowin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Main entry point for the FlipToWin game screen.
 * Observes the [viewModel]'s state and handles the game result emission.
 *
 * @param viewModel The state owner for this game session.
 * @param onResult Callback triggered once a prize is revealed. Receives the prize [Int] type.
 * @param cardContentDescription Localized prefix for card accessibility (e.g., "Game card").
 */
@Composable
fun FlipToWinScreen(
    viewModel: FlipToWinViewModel,
    onResult: (Int) -> Unit = {},
    cardContentDescription: String = "Game card",
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.resultEvent.collect { result ->
            onResult(result)
        }
    }

    FlipToWinContent(
        uiState = uiState,
        onCardClicked = viewModel::onCardClicked,
        onMoveInCenterAnimationEnded = viewModel::onMoveInCenterAnimationEnded,
        cardContentDescription = cardContentDescription,
    )
}

/**
 * Stateless content version of the FlipToWin grid.
 * Useful for custom screen structures or previews.
 *
 * @param uiState Current game state produced by the ViewModel.
 * @param onCardClicked Triggered when a non-flipped card is tapped.
 * @param onMoveInCenterAnimationEnded Triggered when the selected card reaches the center.
 * @param cardContentDescription Localized prefix for card accessibility.
 */
@Composable
fun FlipToWinContent(
    uiState: FlipToWinUiState,
    onCardClicked: (Int) -> Unit,
    onMoveInCenterAnimationEnded: (Int) -> Unit,
    cardContentDescription: String = "Game card",
    modifier: Modifier = Modifier,
) {
    FlipToWinGrid(
        uiState = uiState,
        onCardClicked = onCardClicked,
        onMoveInCenterAnimationEnded = onMoveInCenterAnimationEnded,
        cardContentDescription = cardContentDescription,
        modifier = modifier,
    )
}
