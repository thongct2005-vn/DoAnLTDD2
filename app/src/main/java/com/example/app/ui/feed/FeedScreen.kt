package com.example.app.ui.feed

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.app.ui.feed.components.FeedTabContent
import com.example.app.ui.feed.components.FeedTopBar
import com.example.app.ui.notifications.NotificationsRoute
import com.example.app.ui.profile.ProfileScreen
import com.example.app.ui.profile.ProfileViewModel

@Composable
fun FeedScreen(
    navController: NavHostController? = null,
    onClickLogout: () -> Unit,
    onProfileClick: (String) -> Unit,
    onCreatePostClick: () -> Unit,
    onSearchClick: () -> Unit,
    feedViewModel: FeedViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val postState by feedViewModel.uiState.collectAsState()

    // Logic xử lý side-effect
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) profileViewModel.loadProfile("me")
    }

    BackHandler(enabled = selectedTab == 1) { selectedTab = 0 }

    Scaffold(
        topBar = {
            FeedTopBar(
                onCreatePostClick = onCreatePostClick,
                onSearchClick = onSearchClick,
                onClickLogout = onClickLogout,
                selectedTabIndex = selectedTab,
                onTabClick = { selectedTab = it }
            )
        },
        containerColor = Color(0xFF1F1F1F)
    ) { paddingValues ->
        // Bây giờ khối when cực kỳ ngắn gọn
        when (selectedTab) {
            0 -> FeedTabContent(postState, paddingValues, onCreatePostClick)
            1 -> ProfileScreen(viewModel = profileViewModel, userId = "me", navController = navController)
            2 -> PlaceholderScreen("Màn hình Yêu thích")
            else -> NotificationsRoute()
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = Color.White)
    }
}