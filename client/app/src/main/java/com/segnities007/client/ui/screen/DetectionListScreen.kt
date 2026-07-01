package com.segnities007.client.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.client.R
import com.segnities007.client.model.Detection
import com.segnities007.client.model.DetectionMetadata
import com.segnities007.client.ui.component.DetectionListItem
import com.segnities007.client.ui.viewmodel.DetectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionListScreen(
    viewModel: DetectionViewModel,
    baseUrl: String,
    onDetectionClick: (Detection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val detections by viewModel.detections.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detection_history)) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        DetectionListContent(
            detections = detections,
            isLoading = isLoading,
            baseUrl = baseUrl,
            onRefresh = { viewModel.refresh() },
            onDetectionClick = onDetectionClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Composable
private fun DetectionListContent(
    detections: List<Detection>,
    isLoading: Boolean,
    baseUrl: String,
    onRefresh: () -> Unit,
    onDetectionClick: (Detection) -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        if (detections.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.no_detection_history))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(detections, key = { it.id.toString() }) { detection ->
                    DetectionListItem(
                        detection = detection,
                        baseUrl = baseUrl,
                        onClick = { onDetectionClick(detection) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetectionListContentPreview() {
    MaterialTheme {
        DetectionListContent(
            detections = listOf(
                Detection(
                    id = 1u, type = "person", detectedAt = "2026/07/01 12:00:00",
                    confidence = 0.92, imageUrl = "", metadata = DetectionMetadata(trackId = 3),
                ),
                Detection(
                    id = 2u, type = "suspicious_vehicle", detectedAt = "2026/07/01 11:30:00",
                    confidence = 0.78, imageUrl = "",
                ),
                Detection(
                    id = 3u, type = "person", detectedAt = "2026/07/01 10:15:00",
                    confidence = 0.65, imageUrl = "", metadata = DetectionMetadata(trackId = 1),
                ),
            ),
            isLoading = false,
            baseUrl = "http://localhost:8080",
            onRefresh = {},
            onDetectionClick = {},
        )
    }
}
