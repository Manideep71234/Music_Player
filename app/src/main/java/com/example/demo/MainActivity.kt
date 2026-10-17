package com.example.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.demo.ui.screens.MainScaffold
import com.example.demo.ui.screens.MainViewModel
import com.example.demo.ui.screens.PermissionScreen
import com.example.demo.ui.theme.DemoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DemoTheme {
                MusicPlayerApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MusicPlayerApp(viewModel: MainViewModel) {
    val permissionGranted by viewModel.permissionGranted.collectAsState()

    if (!permissionGranted) {
        PermissionScreen(
            onPermissionGranted = { viewModel.onPermissionGranted() }
        )
    } else {
        MainScaffold(viewModel = viewModel)
    }
}
