package com.github.seijind.fliptowin.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.seijind.fliptowin.ui.model.FlipToWinUiState
import com.github.seijind.fliptowin.ui.viewmodel.FlipToWinViewModel
import kotlinx.coroutines.launch

/**
 * Main entry point for the FlipToWin game screen.
 *
 * This Composable observes the [viewModel]'s state and handles the game result emission.
 * The consumer is responsible for providing the [FlipToWinViewModel] instance,
 * typically via its Hilt-injected activity or using `hiltViewModel()` from the navigation layer.
 *
 * Example Usage:
 * ```kotlin
 * FlipToWinScreen(
 *     viewModel = hiltViewModel(),
 *     onResult = { rewardId -> println("User won: $rewardId") }
 * )
 * ```
 *
 * @param viewModel The state owner for this game session.
 * @param onResult Callback triggered once a prize is revealed. Receives the prize [Int] type.
 * @param onError Optional callback triggered when a configuration error occurs.
 *   Receives the error code (e.g., [com.github.seijind.fliptowin.domain.model.FlipToWinConstants.CONFIGURATION_ERROR_CODE])
 * @param onClaimError Optional callback triggered when a claim attempt fails.
 *   Receives the error message string. If not provided, claim errors are silent.
 * @param cardContentDescription Localized prefix for card accessibility (e.g., "Game card").
 * @param modifier Modifier applied to the root layout.
 */
@Composable
fun FlipToWinScreen(
    viewModel: FlipToWinViewModel,
    modifier: Modifier = Modifier,
    onResult: (Int) -> Unit = {},
    onError: ((Int) -> Unit)? = null,
    onClaimError: ((String) -> Unit)? = null,
    cardContentDescription: String = "Game card",
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        launch { viewModel.resultEvent.collect { onResult(it) } }
        launch { viewModel.claimErrorEvent.collect { onClaimError?.invoke(it) } }
        launch { viewModel.errorEvent.collect { onError?.invoke(it) } }
    }

    FlipToWinContent(
        uiState = uiState,
        onCardClicked = viewModel::onCardClicked,
        onCenteringAnimationEnded = viewModel::onCenteringAnimationEnded,
        cardContentDescription = cardContentDescription,
        modifier = modifier,
    )
}

/**
 * Stateless content version of the FlipToWin grid.
 * Useful for custom screen structures or previews.
 *
 * @param uiState Current game state produced by the ViewModel.
 * @param onCardClicked Triggered when a non-flipped card is tapped.
 * @param onCenteringAnimationEnded Triggered when the selected card reaches the center.
 * @param cardContentDescription Localized prefix for card accessibility.
 * @param modifier Modifier applied to the root layout.
 */
@Composable
fun FlipToWinContent(
    uiState: FlipToWinUiState,
    onCardClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onCenteringAnimationEnded: (Int) -> Unit,
    cardContentDescription: String = "Game card",
) {
    FlipToWinGrid(
        uiState = uiState,
        onCardClicked = onCardClicked,
        onCenteringAnimationEnded = onCenteringAnimationEnded,
        cardContentDescription = cardContentDescription,
        modifier = modifier,
    )
}
