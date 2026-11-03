package com.ucua.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ucua.app.ui.navigation.UcuaNavHost
import com.ucua.app.ui.theme.UcuaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val app = application as UcuaApplication

        setContent {
            UcuaTheme {
                UcuaNavHost(
                    authRepository = app.container.authRepository,
                    container = app.container,
                )
            }
        }
    }
}
