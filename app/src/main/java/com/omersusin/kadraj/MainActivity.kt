package com.omersusin.kadraj

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
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
                KadrajApp(
                    startWithDemo = BuildConfig.DEBUG &&
                        intent.getBooleanExtra("demoProject", false),
                    initialTab = intent.getStringExtra("demoTab"),
                )
            }
        }
    }
}
