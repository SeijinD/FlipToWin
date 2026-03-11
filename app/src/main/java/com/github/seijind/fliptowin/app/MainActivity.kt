package com.github.seijind.fliptowin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.github.seijind.fliptowin.FlipToWinContent
import com.github.seijind.fliptowin.FlipToWinScreen
import com.github.seijind.fliptowin.FlipToWinUiItem
import com.github.seijind.fliptowin.FlipToWinUiState
import com.github.seijind.fliptowin.app.ui.theme.FlipToWinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlipToWinTheme {
                FlipToWinScreen()
            }
        }
    }
}

@Preview(showBackground = true, locale = "el", fontScale = 1.5f)
@Preview(showBackground = true, locale = "en")
@Composable
private fun LoyaltyDailyRewardsContentPreview() {
    val mockItems = remember {
        mutableStateListOf<FlipToWinUiItem>().apply {
            repeat(9) {
                add(
                    FlipToWinUiItem(
                        brush = mutableStateOf(Brush.verticalGradient(colors = listOf(Color(0xFFEBD197), Color(0xFFA2790D)))),
                        clickable = mutableStateOf(true)
                    )
                )
            }
        }
    }

    val mockUiState = FlipToWinUiState(
        items = mockItems,
        onCardClicked = {},
        onMoveInCenterAnimationEnded = {}
    )

    FlipToWinTheme {
        FlipToWinContent(uiState = mockUiState)
    }
}