package com.example.app.ui.comment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.app.domain.model.Comment
import com.example.app.ui.comment.components.CommentInputBar
import com.example.app.ui.comment.components.CommentList
import com.example.app.ui.comment.components.CommentTopBar
import com.example.myapplication.domain.model.CommentItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    postId: String,
    onClose: () -> Unit,
    viewModel: CommentViewModel,
    onProfileClick: (String) -> Unit,
) {
    // Khai báo bảng màu
    val bg = Color(0xFF1F1F1F)

    val uiState by viewModel.uiState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // State quản lý việc Trả lời
    var replyingTo by remember { mutableStateOf<CommentItem?>(null) }

    // State quản lý việc mở rộng Reply
    var expandedParentIds by remember { mutableStateOf(setOf<String>()) }

    // --- State MỚI: Chỉ cần lưu comment đang chờ xóa ---
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    // --------------------------------------------------

    // Logic load thêm reply khi mở rộng
    LaunchedEffect(expandedParentIds) {
        expandedParentIds.forEach { parentId ->
            val hasAnyReply = uiState.comments.any {
                it is CommentItem.Reply && it.comment.parentId == parentId
            }
            if (!hasAnyReply) {
                viewModel.loadMoreReplies(postId, parentId)
            }
        }
    }

    // Load comment ban đầu
    LaunchedEffect(postId) {
        viewModel.loadComments(postId)
    }

    // Logic Load More khi cuộn xuống đáy
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 2)
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !uiState.isLoading && uiState.hasMore) {
            viewModel.loadMoreComments(postId)
        }
    }

    // Nút Back vật lý: Hủy trả lời nếu đang reply
    BackHandler(enabled = replyingTo != null) {
        replyingTo = null
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            CommentTopBar(onClose = onClose)
        },
        bottomBar = {
            CommentInputBar(
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null },
                onSend = { text, _ ->
                    replyingTo?.let { targetItem ->
                        val actualParentId = targetItem.comment.id
                        viewModel.sendReply(
                            postId = postId,
                            parentId = actualParentId,
                            content = text,
                            replyToUserName = targetItem.comment.user.username
                        )
                    } ?: run {
                        viewModel.sendComment(postId = postId, content = text)
                    }
                    replyingTo = null
                }
            )
        }
    ) { paddingValues ->

        // Bọc toàn bộ nội dung trong Box
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(bg)
        ) {
            CommentList(
                listState = listState,
                commentItems = uiState.comments,
                expandedParentIds = expandedParentIds,
                onToggleExpand = { id ->
                    val isOpening = !expandedParentIds.contains(id)
                    expandedParentIds =
                        if (isOpening) expandedParentIds + id else expandedParentIds - id
                },
                onShowReplyClick = { parentId ->
                    uiState.comments.count {
                        it is CommentItem.Reply && it.comment.parentId == parentId
                    }
                    viewModel.loadMoreReplies(postId, parentId)
                },
                onReplyClick = { replyingTo = it },
                onProfileClick = onProfileClick,

                modifier = Modifier.matchParentSize(),

                onLikeClick = { commentId ->
                    viewModel.toggleLike(commentId)
                },


                onDeleteClick = { item ->
                    commentToDelete = item
                }
            )

            // --- CHỈ CÒN DIALOG XÁC NHẬN XÓA ---
            if (commentToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        commentToDelete = null
                    },
                    containerColor = Color(0xFF1F1F1F),
                    title = {
                        Text("Xóa bình luận?", color = Color.White, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text("Bạn có chắc muốn xóa bình luận này không?", color = Color.White)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // Gọi lệnh xóa
                                viewModel.deleteComment(commentToDelete!!.id)
                                commentToDelete = null
                            }
                        ) {
                            Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                commentToDelete = null
                            }
                        ) {
                            Text("Hủy", color = Color.White)
                        }
                    }
                )
            }
        }
    }
}