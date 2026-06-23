package com.segnities007.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.segnities007.client.network.RetrofitInstance
import com.segnities007.client.ui.screen.DetectionListScreen
import com.segnities007.client.ui.theme.ClientTheme
import com.segnities007.client.ui.viewmodel.DetectionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClientTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DetectionListScreen(
                        viewModel = DetectionViewModel(RetrofitInstance.api),
                        baseUrl = RetrofitInstance.BASE_URL,
                    )
                }
            }
        }
    }
}
