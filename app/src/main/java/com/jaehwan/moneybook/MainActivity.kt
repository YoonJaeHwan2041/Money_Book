package com.jaehwan.moneybook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jaehwan.moneybook.ui.root.MoneyBookApp
import com.jaehwan.moneybook.ui.theme.MoneyBookTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyBookTheme {
                MoneyBookApp()
            }
        }
    }
}
