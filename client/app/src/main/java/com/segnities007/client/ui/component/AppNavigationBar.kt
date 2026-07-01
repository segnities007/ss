package com.segnities007.client.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segnities007.client.R

@Composable
fun AppNavigationBar(
    historySelected: Boolean,
    settingsSelected: Boolean,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = historySelected,
            onClick = onHistoryClick,
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_history)) },
        )
        NavigationBarItem(
            selected = settingsSelected,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_settings)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppNavigationBarPreview() {
    MaterialTheme {
        AppNavigationBar(
            historySelected = true,
            settingsSelected = false,
            onHistoryClick = {},
            onSettingsClick = {},
        )
    }
}
