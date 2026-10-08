package com.aura.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aura.app.core.AuraController
import com.aura.app.ui.AuraScreen
import com.aura.app.ui.theme.AuraTheme

class MainActivity : ComponentActivity() {

    private lateinit var controller: AuraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        controller = AuraController(applicationContext)
        setContent {
            AuraTheme {
                AuraScreen(controller)
            }
        }
    }

    override fun onStop() {
        // Privacy: when the app leaves the screen, AURA stops listening and speaking.
        controller.stop()
        super.onStop()
    }

    override fun onDestroy() {
        controller.release()
        super.onDestroy()
    }
}
