package com.example.app.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.app.domain.model.Profile
import com.example.app.ui.feed.post.PostItem
import com.example.app.ui.profile.ProfileViewModel
import com.example.app.ui.profile.components.*
import kotlinx.coroutines.flow.collectLatest

private val bg = Color(0xFF1F1F1F)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: String,
    navController: NavHostController? = null  // Optional: dùng cho Navigation Compose
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Load profile khi userId thay đổi
    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    // Tự động load more khi scroll gần cuối
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collectLatest { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull() ?: return@collectLatest
                val totalItemCount = listState.layoutInfo.totalItemsCount

                if (uiState !is ProfileUiState.Success) return@collectLatest
                val successState = uiState as ProfileUiState.Success
                if (!successState.hasMore || successState.isLoadingMore) return@collectLatest

                if (lastVisibleItem.index >= totalItemCount - 3 && totalItemCount > 0) {
                    viewModel.loadMorePosts()
                }
            }
    }

    // BackHandler: Quay về màn trước (hoặc tab Feed nếu dùng tab switch)
    BackHandler {
        navController?.popBackStack() ?: Unit
    }

    Scaffold(
        topBar = {
            ProfileTopBar(onBack = { navController?.popBackStack() })
        },
        containerColor = bg,
        contentColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }

                is ProfileUiState.Error -> {
                    Text(
                        text = (uiState as ProfileUiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ProfileUiState.Success -> {
                    val data = uiState as ProfileUiState.Success

                    LazyColumn(state = listState) {
                        stickyHeader(key = "profile_sticky_section") {
                            Column(modifier = Modifier.background(bg)) {
                                ProfileHeader(
                                    profile = data.profile,
                                    onBack = { navController?.popBackStack() },
                                    onFollowerClick = {
                                        navController?.navigate("followers/${data.profile.id}?tab=0")
                                    },
                                    onFollowingClick = {
                                        navController?.navigate("followers/${data.profile.id}?tab=1")
                                    },
                                    onFollowClick = { viewModel.toggleFollow(userId) },
                                    onUnfollowClick = { viewModel.toggleFollow(userId) },
                                    onUpdateProfileClick = { navController?.navigate("edit_profile") }
                                )
                                ProfileTabsSection()
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        items(items = data.posts, key = { it.id }) { post ->
                            PostItem(
                                mediaList = post.media,
                                post = post,
                                profile = data.profile,
                                onCommentClick = { /* TODO: mở comment */ },
                                onProfileClick = { targetId ->
                                    navController?.navigate("profile/$targetId")
                                },
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                onShareClick = { /* TODO: share logic */ }
                            )
                        }

                        if (data.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                        }

                        if (!data.hasMore && data.posts.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Đã hết bài viết", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}