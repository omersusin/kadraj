package com.omersusin.kadraj

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.omersusin.kadraj.ui.theme.Kadraj
import com.omersusin.kadraj.ui.theme.KadrajTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.isNavigationBarContrastEnforced = false
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            KadrajTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Kadraj.Ink),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Kadraj",
                        style = Kadraj.Type.Display.copy(color = Kadraj.Paper),
                    )
                }
            }
        }
    }
}
