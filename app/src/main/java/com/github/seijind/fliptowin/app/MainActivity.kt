package com.github.seijind.fliptowin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.seijind.fliptowin.FlipToWinConfig
import com.github.seijind.fliptowin.FlipToWinContent
import com.github.seijind.fliptowin.FlipToWinResponse
import com.github.seijind.fliptowin.FlipToWinResult
import com.github.seijind.fliptowin.FlipToWinRewardType
import com.github.seijind.fliptowin.FlipToWinScreen
import com.github.seijind.fliptowin.FlipToWinUiCardData
import com.github.seijind.fliptowin.FlipToWinUiState
import com.github.seijind.fliptowin.FlipToWinViewModel
import com.github.seijind.fliptowin.app.ui.theme.FlipToWinTheme
import kotlinx.collections.immutable.toPersistentList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlipToWinTheme {
                val viewModel = viewModel<FlipToWinViewModel>()

                LaunchedEffect(viewModel) {
                    viewModel.init(mockResult)
                }

                FlipToWinScreen(viewModel = viewModel)
            }
        }
    }
}

/** Sample result — replace with your real API call. */
private val mockResult = FlipToWinResult.Success(
    response = FlipToWinResponse(
        winRewardType = 1,
        rewards = listOf(
            FlipToWinRewardType(1, "#FF5722", "#E64A19", "url_points"),
            FlipToWinRewardType(2, "#2196F3", "#1976D2", "url_gift"),
            FlipToWinRewardType(0, "#9E9E9E", "#616161", "url_lost"),
        ),
        config = FlipToWinConfig(
            cardBack = FlipToWinRewardType(0, "#EBD197", "#A2790D", "url_back_icon"),
            wiggleDelayMillis = 3000L,
            revealAllAtEnd = false,
        )
    )
)

@Preview(showBackground = true, locale = "el", fontScale = 1.5f)
@Preview(showBackground = true, locale = "en")
@Composable
private fun LoyaltyDailyRewardsContentPreview() {
    val mockItems = List(9) {
        FlipToWinUiCardData(
            type = 1,
            image = "url_back_icon",
            brush = Brush.verticalGradient(colors = listOf(Color(0xFFEBD197), Color(0xFFA2790D))),
        )
    }.toPersistentList()

    val mockUiState = FlipToWinUiState(items = mockItems)

    FlipToWinTheme {
        FlipToWinContent(
            uiState = mockUiState,
            onCardClicked = {},
            onMoveInCenterAnimationEnded = {},
        )
    }
}
