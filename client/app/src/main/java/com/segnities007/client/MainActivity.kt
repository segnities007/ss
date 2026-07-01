package com.segnities007.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.segnities007.client.network.RetrofitInstance
import com.segnities007.client.ui.ClientApp
import com.segnities007.client.ui.theme.ClientTheme
import com.segnities007.client.ui.viewmodel.DetectionViewModel
import com.segnities007.client.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val detectionViewModel = ViewModelProvider(
            this,
            DetectionViewModel.Factory(application, RetrofitInstance.api),
        )[DetectionViewModel::class.java]
        val settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModel.Factory(application, RetrofitInstance.iotControlApi),
        )[SettingsViewModel::class.java]

        setContent {
            ClientTheme {
                ClientApp(
                    detectionViewModel = detectionViewModel,
                    settingsViewModel = settingsViewModel,
                    baseUrl = RetrofitInstance.BASE_URL,
                )
            }
        }
    }
}
