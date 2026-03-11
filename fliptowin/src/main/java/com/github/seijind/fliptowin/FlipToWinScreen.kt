package com.github.seijind.fliptowin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FlipToWinScreen(
    viewModel: FlipToWinViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FlipToWinContent(uiState = uiState)
}

@Composable
fun FlipToWinContent(
    uiState: FlipToWinUiState,
    modifier: Modifier = Modifier,
) {
    FlipToWinGrid(
        uiState = uiState,
        modifier = modifier,
    )
}
