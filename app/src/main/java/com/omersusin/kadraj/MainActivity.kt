package com.omersusin.kadraj

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.omersusin.kadraj.ui.theme.KadrajTheme
import com.omersusin.kadraj.ui.theme.ThemeMode
import com.omersusin.kadraj.ui.theme.ThemePreferences

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
            val demo = BuildConfig.DEBUG && intent.getBooleanExtra("demoProject", false)
            val savedTheme = remember {
                if (BuildConfig.DEBUG) {
                    ThemeMode.fromKey(intent.getStringExtra("demoTheme"))
                        ?: ThemePreferences.read(this@MainActivity)
                } else {
                    ThemePreferences.read(this@MainActivity)
                }
            }
            var themeMode by remember { mutableStateOf(savedTheme) }

            KadrajTheme(mode = themeMode) {
                KadrajApp(
                    startWithDemo = demo,
                    demoPhotoCount = if (demo) {
                        intent.getIntExtra("demoPhotoCount", 3)
                    } else {
                        3
                    },
                    initialTab = intent.getStringExtra("demoTab"),
                    startWithSettings = BuildConfig.DEBUG &&
                        intent.getBooleanExtra("demoSettings", false),
                    themeMode = themeMode,
                    onThemeChange = { mode ->
                        themeMode = mode
                        ThemePreferences.write(this@MainActivity, mode)
                    },
                )
            }
        }
    }
}
