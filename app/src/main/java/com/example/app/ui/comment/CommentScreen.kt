// CommentScreen.kt
package com.example.app.ui.comment

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.app.ui.comment.components.CommentInputBar
import com.example.app.ui.comment.components.CommentList
import com.example.app.ui.comment.components.CommentTopBar
import com.example.app.ui.comment.data.sampleComments
import com.example.myapplication.domain.model.Comment
import com.example.myapplication.domain.model.CommentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(onClose: () -> Unit) {
    var replyingTo by remember { mutableStateOf<CommentItem?>(null) }
    var expandedParentIds by remember { mutableStateOf(setOf<String>()) }
    var visibleReplyCountMap by remember { mutableStateOf(mapOf<String, Int>()) }
    var commentItems by remember { mutableStateOf(sampleComments()) }

    BackHandler(enabled = replyingTo != null) {
        replyingTo = null
    }

    Scaffold(
        topBar = { CommentTopBar(onClose = onClose) },
        bottomBar = {
            CommentInputBar(
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null },
                onSend = { text, imageUri ->
                    val newItem = createNewCommentItem(text, imageUri, replyingTo)
                    addNewComment(newItem, commentItems) { commentItems = it }

                    if (newItem is CommentItem.Reply) {
                        expandedParentIds = expandedParentIds + newItem.comment.parentId!!
                    }
                    replyingTo = null
                }
            )
        }
    ) { paddingValues ->
        CommentList(
            commentItems = commentItems,
            expandedParentIds = expandedParentIds,
            visibleReplyCountMap = visibleReplyCountMap,
            onToggleExpand = { id ->
                expandedParentIds = if (expandedParentIds.contains(id))
                    expandedParentIds - id else expandedParentIds + id
                if (!visibleReplyCountMap.containsKey(id)) {
                    visibleReplyCountMap = visibleReplyCountMap + (id to 5)
                }
            },
            onLoadMoreReplies = { id ->
                val current = visibleReplyCountMap[id] ?: 5
                visibleReplyCountMap = visibleReplyCountMap + (id to current + 5)
            },
            onReplyClick = { replyingTo = it },
            modifier = Modifier.padding(paddingValues)
        )
    }
}
private fun createNewCommentItem(
    text: String,
    imageUri: Uri?,
    replyingTo: CommentItem?
): CommentItem {
    val currentTime = System.currentTimeMillis()

    return if (replyingTo != null) {
        // Đây là REPLY (con)
        CommentItem.Reply(
            comment = Comment(
                id = currentTime.toString(),
                postId = "p1",
                userId = "me",
                userName = "Bạn",
                content = text,
                imageUri = imageUri,
                parentId = replyingTo.comment.id,
                replyToUserName = replyingTo.comment.userName,
                createdAt = currentTime,
                likeCount = 0
            ),
            replyToUserName = replyingTo.comment.userName,
            level = minOf(replyingTo.level + 1, 2) // Giới hạn level tối đa 2
        )
    } else {
        // Đây là COMMENT CHA
        CommentItem.Parent(
            comment = Comment(
                id = currentTime.toString(),
                postId = "p1",
                userId = "me",
                userName = "Bạn",
                content = text,
                imageUri = imageUri,
                parentId = null,
                replyToUserName = null,
                createdAt = currentTime,
                likeCount = 0
            )
        )
    }
}
private fun addNewComment(
    newItem: CommentItem,
    currentList: List<CommentItem>,
    onUpdate: (List<CommentItem>) -> Unit
) {
    val newList = currentList.toMutableList()

    if (newItem is CommentItem.Reply) {
        // Tìm bình luận cuối cùng cùng parent (có thể là parent hoặc reply cùng nhánh)
        val parentId = newItem.comment.parentId!!
        val lastIndex = currentList.indexOfLast { item ->
            item.comment.id == parentId ||
                    (item is CommentItem.Reply && item.comment.parentId == parentId)
        }

        // Chèn ngay sau vị trí đó (nếu không tìm thấy thì chèn đầu danh sách)
        val insertIndex = if (lastIndex == -1) 0 else lastIndex + 1
        newList.add(insertIndex, newItem)
    } else {
        // Bình luận cha mới → thêm vào cuối danh sách
        newList.add(newItem)
    }

    onUpdate(newList)
}