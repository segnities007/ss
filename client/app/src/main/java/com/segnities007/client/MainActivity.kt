package com.segnities007.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.segnities007.client.network.RetrofitInstance
import com.segnities007.client.ui.ClientApp
import com.segnities007.client.ui.theme.ClientTheme
import com.segnities007.client.ui.viewmodel.DashboardViewModel
import com.segnities007.client.ui.viewmodel.DetectionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val detectionViewModel = ViewModelProvider(
            this,
            DetectionViewModel.Factory(RetrofitInstance.api),
        )[DetectionViewModel::class.java]
        val dashboardViewModel = ViewModelProvider(
            this,
            DashboardViewModel.Factory(RetrofitInstance.iotControlApi),
        )[DashboardViewModel::class.java]

        setContent {
            ClientTheme {
                ClientApp(
                    detectionViewModel = detectionViewModel,
                    dashboardViewModel = dashboardViewModel,
                    baseUrl = RetrofitInstance.BASE_URL,
                )
            }
        }
    }
}
