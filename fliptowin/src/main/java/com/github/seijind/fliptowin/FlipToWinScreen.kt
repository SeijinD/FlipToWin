package com.github.seijind.fliptowin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FlipToWinScreen() {
    val viewModel: FlipToWinViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoyaltyFlipGrid(
        uiState = uiState,
    )
}