package com.example.app.ui.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun NotificationsRoute(
    viewModel: NotificationViewModel = viewModel(),
    navController: NavHostController
) {

    val notifications by viewModel.allNotifications.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    NotificationsScreen(
        rootNavController = navController,
        allNotifications = notifications,
        loading = isLoading,
        onMarkAllRead = { viewModel.markAllRead() },
        markAsRead = { id -> viewModel.markAsRead(id) },
        onClickItem = { item ->

        }
    )
}