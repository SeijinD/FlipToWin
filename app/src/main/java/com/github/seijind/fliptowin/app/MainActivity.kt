package com.github.seijind.fliptowin.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.seijind.fliptowin.app.ui.theme.FlipToWinTheme
import com.github.seijind.fliptowin.ui.composable.FlipToWinContent
import com.github.seijind.fliptowin.ui.composable.FlipToWinScreen
import com.github.seijind.fliptowin.ui.model.FlipToWinUiCard
import com.github.seijind.fliptowin.ui.model.FlipToWinUiState
import com.github.seijind.fliptowin.ui.viewmodel.FlipToWinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.toPersistentList

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlipToWinTheme {
                val viewModel = hiltViewModel<FlipToWinViewModel>()

                LaunchedEffect(viewModel) {
                    viewModel.loadGame()
                }

                FlipToWinScreen(
                    viewModel = viewModel,
                    onResult = { result ->
                        Log.d("FlipToWin", "Game Ended! Won type: $result")
                        Toast.makeText(this@MainActivity, "Won reward: $result", Toast.LENGTH_SHORT).show()
                    },
                    onError = { errorCode ->
                        Log.e("FlipToWin", "Game error, code: $errorCode")
                        Toast.makeText(this@MainActivity, "Error loading game (code: $errorCode)", Toast.LENGTH_SHORT).show()
                    },
                    onClaimError = { errorMessage ->
                        Log.e("FlipToWin", "Claim error: $errorMessage")
                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "el", fontScale = 1.5f)
@Preview(showBackground = true, locale = "en")
@Composable
private fun FlipToWinContentPreview() {
    val mockItems = List(9) {
        FlipToWinUiCard(
            type = 1,
            imageUrl = "url_back_icon",
            brush = Brush.verticalGradient(colors = listOf(Color(0xFFEBD197), Color(0xFFA2790D))),
        )
    }.toPersistentList()

    val mockUiState = FlipToWinUiState(cards = mockItems)

    FlipToWinTheme {
        FlipToWinContent(
            uiState = mockUiState,
            onCardClicked = {},
            onCenteringAnimationEnded = {},
        )
    }
}
