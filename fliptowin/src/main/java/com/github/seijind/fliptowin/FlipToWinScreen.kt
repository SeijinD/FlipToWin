package com.github.seijind.fliptowin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FlipToWinScreen(
    viewModel: FlipToWinViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FlipToWinContent(
        uiState = uiState,
        onCardClicked = viewModel::onCardClicked,
        onMoveInCenterAnimationEnded = viewModel::onMoveInCenterAnimationEnded,
    )
}

@Composable
fun FlipToWinContent(
    uiState: FlipToWinUiState,
    onCardClicked: (Int) -> Unit,
    onMoveInCenterAnimationEnded: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlipToWinGrid(
        uiState = uiState,
        onCardClicked = onCardClicked,
        onMoveInCenterAnimationEnded = onMoveInCenterAnimationEnded,
        modifier = modifier,
    )
}
