package com.example.app.ui.feed

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.app.ui.feed.components.FeedTabContent
import com.example.app.ui.feed.components.FeedTopBar

@Composable
fun FeedScreen(
    navController: NavHostController,
    onCreatePostClick: () -> Unit,
    onSearchClick: () -> Unit,
    feedViewModel: FeedViewModel = viewModel(),
) {
    val uiState by feedViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val onlineUsers by feedViewModel.onlineUsers.collectAsState()
    val isLoadingOnline by feedViewModel.isLoadingOnline.collectAsState()


    LaunchedEffect(feedViewModel) {
        feedViewModel.scrollToTopAndRefreshEvent.collect {
            listState.animateScrollToItem(0)
            feedViewModel.refresh()
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull() ?: return@collect
                val totalItemsCount = listState.layoutInfo.totalItemsCount

                if (lastVisibleItem.index >= totalItemsCount - 3 && totalItemsCount > 0) {
                    feedViewModel.loadMore()
                }
            }
    }

    LaunchedEffect(Unit) {
        feedViewModel.loadOnlineUsers()
    }


    Scaffold(
        topBar = {
            FeedTopBar(
                onCreatePostClick = onCreatePostClick,
                onSearchClick = onSearchClick,
                selectedTabIndex = 0,
            )
        },
        containerColor = Color(0xFF1F1F1F)
    ) { paddingValues ->
        FeedTabContent(
            postState = uiState,
            paddingValues = paddingValues,
            listState = listState,
            navController = navController,
            feedViewModel = feedViewModel,
            onInputClick = onCreatePostClick,
            onlineUsers = onlineUsers,
            isLoadingOnline = isLoadingOnline,
            onOnlineUserClick = { user ->
                navController.navigate("profile/${user.id}")
            }
        )
    }
}