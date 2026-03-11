package com.github.seijind.fliptowin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.seijind.fliptowin.FlipToWinScreen
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
    FlipToWinTheme {

    }
}