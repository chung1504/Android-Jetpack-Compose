package com.example.androidjetpackcompose.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.androidjetpackcompose.app.navigation.AppNavGraph
import com.example.androidjetpackcompose.ui.theme.AndroidJetpackComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidJetpackComposeTheme {
                AppNavGraph()
            }
        }
    }
}
