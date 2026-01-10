package com.example.app.ui.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NotificationsRoute(viewModel: NotificationViewModel = viewModel()) {
    val notifications by viewModel.allNotifications.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    NotificationsScreen(
        allNotifications = notifications,
        loading = isLoading,
        onRefresh = { viewModel.refreshNotifications() },
        onMarkAllRead = { viewModel.markAllRead() },
        markAsRead = { id -> viewModel.markAsRead(id) },
        onClickItem = { item -> /* Chuyển hướng màn hình */ }
    )
}