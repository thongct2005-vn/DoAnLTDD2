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
import com.example.app.ui.feed.FeedViewModel
import com.example.app.ui.feed.post.PostItem
import com.example.app.ui.profile.components.*
import kotlinx.coroutines.flow.collectLatest
import com.example.app.domain.model.Post

private val bg = Color(0xFF1F1F1F)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: String,
    navController: NavHostController? = null,
    feedViewModel: FeedViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val canPop = navController?.previousBackStackEntry != null

    // Tab: 0 = Bài viết, 1 = Đã lưu (chỉ owner)
    var selectedTab by remember { mutableIntStateOf(0) }
    var prevTab by remember { mutableIntStateOf(0) }
    var savedSnapshot by remember { mutableStateOf<List<Post>>(emptyList()) }

    // ✅ chỉ đánh dấu: có thay đổi saved (save/unsave) → khi rời tab saved mới refresh
    var needRefreshSaved by remember { mutableStateOf(false) }
    var showUnfollowDialog by remember { mutableStateOf(false) }


    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    // ✅ Khi vào tab "Đã lưu" lần đầu mà rỗng thì load
    LaunchedEffect(selectedTab, uiState) {
        val state = uiState as? ProfileUiState.Success ?: return@LaunchedEffect
        if (selectedTab == 1 && state.profile.isOwner && state.savedPosts.isEmpty() && !state.isLoadingMoreSaved) {
            viewModel.refreshSavedPosts()
            needRefreshSaved = false
        }
    }

    // ✅ Nhận event savedChanged từ FeedViewModel: CHỈ ĐÁNH DẤU, KHÔNG refresh ngay
    LaunchedEffect(Unit) {
        feedViewModel.savedChanged.collect {
            needRefreshSaved = true
        }
    }

    // ✅ Chỉ refresh khi RỜI tab "Đã lưu"
    LaunchedEffect(selectedTab, uiState) {
        val state = uiState as? ProfileUiState.Success ?: return@LaunchedEffect
        if (!state.profile.isOwner) return@LaunchedEffect

        if (prevTab == 1 && selectedTab != 1 && needRefreshSaved) {
            viewModel.refreshSavedPosts()
            needRefreshSaved = false
        }

        prevTab = selectedTab
    }

    // Load more khi scroll gần cuối
    LaunchedEffect(listState, selectedTab) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collectLatest { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull() ?: return@collectLatest
                val totalItemCount = listState.layoutInfo.totalItemsCount

                if (uiState !is ProfileUiState.Success) return@collectLatest
                val successState = uiState as ProfileUiState.Success

                val isNearBottom = lastVisibleItem.index >= totalItemCount - 3 && totalItemCount > 0

                when (selectedTab) {
                    0 -> { // Bài viết
                        if (isNearBottom && successState.hasMore && !successState.isLoadingMore) {
                            viewModel.loadMorePosts()
                        }
                    }
                    1 -> { // Đã lưu
                        if (
                            successState.profile.isOwner &&
                            isNearBottom &&
                            successState.hasMoreSaved &&
                            !successState.isLoadingMoreSaved
                        ) {
                            viewModel.loadPostSave()
                        }
                    }
                }
            }
    }

    BackHandler(enabled = canPop) {
        // ✅ Nếu đang ở tab Đã lưu và có thay đổi saved → refresh trước khi thoát
        val state = uiState as? ProfileUiState.Success
        if (selectedTab == 1 && state?.profile?.isOwner == true && needRefreshSaved) {
            viewModel.refreshSavedPosts()
            needRefreshSaved = false
        }
        navController?.popBackStack()
    }

    Scaffold(
        topBar = {
            ProfileTopBar(
                onBack = if (canPop) { { navController.popBackStack() } } else null
            )
        },
        containerColor = bg,
        contentColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (showUnfollowDialog) {
                AlertDialog(
                    onDismissRequest = { showUnfollowDialog = false },

                    containerColor = Color(0xFF2A2A2A), // nền dialog tối (giống hình)
                    titleContentColor = Color.White,
                    textContentColor = Color(0xFFBDBDBD),

                    title = { Text("Bỏ theo dõi?") },
                    text = { Text("Bạn có muốn bỏ theo dõi không?") },

                    confirmButton = {
                        TextButton(
                            onClick = {
                                showUnfollowDialog = false
                                viewModel.toggleFollow(userId) // unfollow thật
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFE53935) // đỏ
                            )
                        ) { Text("Bỏ theo dõi") }
                    },

                    dismissButton = {
                        TextButton(
                            onClick = { showUnfollowDialog = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF9E9E9E) // xám
                            )
                        ) { Text("Hủy") }
                    }
                )
            }
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

                    // ✅ Chụp snapshot khi vừa vào tab "Đã lưu"
                    LaunchedEffect(selectedTab) {
                        val state = uiState as? ProfileUiState.Success ?: return@LaunchedEffect
                        if (state.profile.isOwner && selectedTab == 1) {
                            savedSnapshot = state.savedPosts
                        }
                    }

                    // ✅ Map trạng thái saved mới nhất để icon đổi ngay (dù list là snapshot)
                    val latestSavedMap = remember(data.posts, data.savedPosts) {
                        (data.posts + data.savedPosts).associateBy({ it.id }, { it.isSaved })
                    }

                    // Lọc bài viết công khai nếu không phải owner
                    val visiblePosts = remember(data.posts, data.profile.isOwner) {
                        if (data.profile.isOwner) data.posts
                        else data.posts.filter { it.privacy.trim().equals("public", ignoreCase = true) }
                    }

                    LazyColumn(state = listState) {
                        item(key = "profile_header") {
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
                                    onUnfollowClick = { showUnfollowDialog = true },
                                    onUpdateProfileClick = {
                                        navController?.navigate("edit_profile/${data.profile.id}")
                                    }
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        item(key = "profile_tabs") {
                            ProfileTabsSection(
                                isOwner = data.profile.isOwner,
                                selectedTab = selectedTab,
                                onTabChange = { selectedTab = it }
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        when (selectedTab) {
                            0 -> { // Tab Bài viết
                                items(visiblePosts, key = { it.id }) { post ->
                                    val targetId = post.sharedPost?.id ?: post.id
                                    PostItem(
                                        mediaList = post.media,
                                        post = post,
                                        onCommentClick = { navController?.navigate("comments/${post.id}") },
                                        onProfileClick = { targetId -> navController?.navigate("profile/$targetId") },
                                        onLikeClick = { viewModel.toggleLike(post.id) },
                                        onShareClick = { navController?.navigate("share_post/$targetId") },
                                        onDeletePost = { postId -> viewModel.deletePost(postId) },
                                        onChangePrivacy = { postId, privacy -> viewModel.updatePostPrivacy(postId, privacy) },
                                        onSaveClick = { viewModel.toggleSave(post.id) }
                                    )
                                }

                                if (data.isLoadingMore) item { LoadingFooter() }
                                if (!data.hasMore && visiblePosts.isNotEmpty()) item { EndOfListFooter("Đã hết bài viết") }
                            }

                            1 -> { // Tab Đã lưu (chỉ owner)
                                if (!data.profile.isOwner) {
                                    item {
                                        Text(
                                            "Không có quyền xem",
                                            color = Color.Gray,
                                            modifier = Modifier.padding(24.dp)
                                        )
                                    }
                                } else {

                                    // ✅ Render theo snapshot -> unsave không biến mất ngay
                                    items(savedSnapshot, key = { it.id }) { post ->
                                        // ✅ lấy trạng thái saved mới nhất để icon update ngay
                                        val displayPost = post.copy(
                                            isSaved = latestSavedMap[post.id] ?: post.isSaved
                                        )

                                        val targetId = displayPost.sharedPost?.id ?: displayPost.id
                                        PostItem(
                                            mediaList = displayPost.media,
                                            post = displayPost,
                                            onCommentClick = { navController?.navigate("comments/${displayPost.id}") },
                                            onProfileClick = { navController?.navigate("profile/$targetId") },
                                            onLikeClick = { viewModel.toggleLike(displayPost.id) },
                                            onShareClick = { navController?.navigate("share_post/$targetId") },
                                            onDeletePost = { viewModel.deletePost(displayPost.id) },
                                            onChangePrivacy = { _, privacy -> viewModel.updatePostPrivacy(displayPost.id, privacy) },
                                            onSaveClick = { viewModel.toggleSave(displayPost.id) }
                                        )
                                    }

                                    if (data.isLoadingMoreSaved) {
                                        item { LoadingFooter() }
                                    }

                                    if (!data.hasMoreSaved && savedSnapshot.isNotEmpty()) {
                                        item { EndOfListFooter("Đã hết bài viết đã lưu") }
                                    }

                                    if (savedSnapshot.isEmpty() && !data.isLoadingMoreSaved) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "Bạn chưa lưu bài viết nào",
                                                    color = Color.Gray,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun EndOfListFooter(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.Gray, fontSize = 14.sp)
    }
}
