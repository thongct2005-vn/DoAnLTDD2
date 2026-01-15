package com.example.app.ui.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.CommentRepository
import com.example.app.domain.model.Comment
import com.example.app.domain.model.User
import com.example.app.network.dto.auth.AuthManager
import com.example.app.utils.mapper.toParentItem
import com.example.myapplication.domain.model.CommentItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch




enum class SendStatus {
    SENDING, SENT, FAILED
}


data class CommentUiState(
    val isLoading: Boolean = false,
    val comments: List<CommentItem> = emptyList(),
    val error: String? = null,
    val hasMore: Boolean = true,
    val nextCursor: String? = null,
    val likingCommentIds: Set<String> = emptySet()
)

class CommentViewModel(
    private val repository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState = _uiState.asStateFlow()

    private var currentPostId: String? = null

    private val replyCursors = mutableMapOf<String, String?>() // Lưu nextCursor cho từng parentId
    fun loadMoreReplies(postId: String, parentId: String) {
        viewModelScope.launch {
            val currentCursor = replyCursors[parentId]

            repository.getReplies(postId, parentId, currentCursor)
                .onSuccess { response ->
                    val newReplies = response.comments

                    val hasTempReply = _uiState.value.comments.any {
                        it is CommentItem.Reply &&
                                it.comment.parentId == parentId &&
                                it.comment.id.startsWith("temp_")
                    }

                    if (newReplies.isEmpty() && !hasTempReply) return@launch

                    replyCursors[parentId] = response.nextCursor

                    _uiState.update { state ->
                        val list = state.comments.toMutableList()

                        val lastIndex = list.indexOfLast {
                            (it is CommentItem.Parent && it.comment.id == parentId) ||
                                    (it is CommentItem.Reply && it.comment.parentId == parentId)
                        }

                        val insertPosition = if (lastIndex >= 0) lastIndex + 1 else list.size

                        val newItems = newReplies.map { reply ->
                            CommentItem.Reply(
                                comment = reply.copy(parentId = parentId),
                                replyToUserName = reply.replyToUsername ?: "",
                                level = 1
                            )
                        }


                        val existingIds = list.map { it.comment.id }.toSet()
                        val toAdd = newItems.filter { it.comment.id !in existingIds }

                        if (toAdd.isNotEmpty()) {
                            list.addAll(insertPosition, toAdd)
                        }

                        state.copy(comments = list)
                    }
                }
                .onFailure { e ->
                    println("Load replies thất bại: ${e.message}")
                }
        }
    }

//

    fun loadComments(postId: String) {
        currentPostId = postId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getComments(postId, null)
                .onSuccess { response ->
                    val parentItems = response.comments.map { it.toParentItem() }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            comments = parentItems,
                            nextCursor = response.nextCursor,
                            hasMore = response.nextCursor != null
                        )
                    }
                }

                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }

        }
    }

    fun loadMoreComments(postId: String) {
        val currentState = _uiState.value
        // Nếu đang load, hoặc không còn dữ liệu (nextCursor == null), thì không làm gì cả
        if (currentState.isLoading || !currentState.hasMore || currentState.nextCursor == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getComments(postId, currentState.nextCursor)
                .onSuccess { response ->
                    val newParentItems = response.comments.map { it.toParentItem() }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            // CỘNG DỒN: List cũ + List mới
                            comments = state.comments + newParentItems,
                            nextCursor = response.nextCursor,
                            hasMore = response.nextCursor != null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }






// ... các phần khác của ViewModel

    fun sendComment(postId: String, content: String) {
        val tempItem = createTempCommentItem(content)
        val tempId = tempItem.comment.id

        viewModelScope.launch {
            // 1. Optimistic: thêm temp lên đầu list
            _uiState.update { current ->
                current.copy(comments = listOf(tempItem) + current.comments)
            }

            repository.sendComment(postId, content)
                .onSuccess { realComment ->
                    println("DEBUG: Gửi comment thành công - real id = ${realComment.id}")
                    viewModelScope.launch {
                        delay(500)
                        loadComments(postId)
                    }
                    _uiState.update { state ->

                        val updatedComments = state.comments.map { item ->
                            if (item.comment.id == tempId) {

                                (item as? CommentItem.Parent)?.copy(comment = realComment)
                                    ?: item
                            } else {
                                item
                            }
                        }
                        state.copy(comments = updatedComments)
                    }

                    // KHÔNG gọi loadComments() ở đây
                    // Nếu muốn reload thủ công → để người dùng pull-to-refresh
                    // Hoặc nếu bạn có nextCursor và muốn load thêm:
                    // loadMoreComments(postId)  // chỉ nếu cần
                }
                .onFailure { e ->
                    println("DEBUG: Gửi comment thất bại: ${e.message}")

                    _uiState.update { state ->
                        // Không xóa temp ngay → chuyển trạng thái failed (nếu có SendStatus)
                        val updatedComments = state.comments.map { item ->
                            if (item.comment.id == tempId) {
                                (item as? CommentItem.Parent)?.copy(
                                    comment = item.comment.copy(sendStatus = SendStatus.FAILED)
                                ) ?: item
                            } else {
                                item
                            }
                        }
                        state.copy(comments = updatedComments, error = e.message)
                    }
                }
        }
    }



    fun sendReply(
        postId: String,
        parentId: String,
        content: String,
        replyToUserName: String
    ) {
        val tempItem = createTempCommentItem(content, parentId, replyToUserName)
        val tempId = tempItem.comment.id

        viewModelScope.launch {
            println("DEBUG: Bắt đầu send reply - tempId = $tempId")

            // Optimistic add temp
            _uiState.update { state ->
                val list = state.comments.toMutableList()
                val targetIndex = list.indexOfFirst { it.comment.id == parentId }

                val insertPos = if (targetIndex == -1) list.size else {
                    var pos = targetIndex + 1
                    while (pos < list.size && list[pos].let { it is CommentItem.Reply && it.comment.parentId == parentId }) {
                        pos++
                    }
                    pos
                }

                list.add(insertPos, tempItem)
                println("DEBUG: Temp inserted at $insertPos, size = ${list.size}")
                state.copy(comments = list)
            }

            repository.sendReply(postId, parentId, content)
                .onSuccess { realComment ->
                    println("DEBUG: Success - real id = ${realComment.id}")

                    _uiState.update { state ->
                        val currentList = state.comments

                        if (!currentList.any { it.comment.id == tempId }) {
                            println("DEBUG: Temp $tempId đã mất → bỏ qua")
                            return@update state
                        }

                        // Thay temp bằng real
                        val updatedList = currentList.map { item ->
                            if (item.comment.id == tempId && item is CommentItem.Reply) {
                                item.copy(
                                    comment = realComment.copy(
                                        parentId = parentId,
                                        sendStatus = SendStatus.SENT  // nếu bạn đã thêm SendStatus
                                    ),
                                    replyToUserName = realComment.replyToUsername ?: replyToUserName
                                )
                            } else {
                                item
                            }
                        }

                        state.copy(comments = updatedList)
                    }


                    incrementReplyCount(parentId)

                     viewModelScope.launch { delay(500L)
                         loadComments(postId)
                         delay(500L)
                         loadMoreReplies(postId, parentId) }
                    print("Parent: $parentId - $postId")
                }
                .onFailure { e ->

                    print("Parent: $parentId - $postId")
                    println("DEBUG: Fail - tempId = $tempId, error: ${e.message}")
                    _uiState.update { state ->
                        val newList = state.comments.map { item ->
                            if (item is CommentItem.Reply && item.comment.id == tempId) {
                                item.copy(
                                    comment = item.comment.copy(sendStatus = SendStatus.FAILED)
                                )
                            } else item
                        }
                        state.copy(comments = newList)
                    }
                }
        }
    }


    fun incrementReplyCount(commentId: String) {
        _uiState.update { state ->
            val newList = state.comments.map { item ->
                if (item.comment.id == commentId) {
                    val updatedComment = item.comment.copy(
                        replyCount = item.comment.replyCount + 1
                    )
                    when (item) {
                        is CommentItem.Parent -> item.copy(comment = updatedComment)
                        is CommentItem.Reply -> item.copy(comment = updatedComment)
                    }
                } else {
                    item
                }
            }
            state.copy(comments = newList)
        }
    }




    private fun createTempCommentItem(
        content: String,
        parentId: String? = null,
        replyToUserName: String? = null
    ): CommentItem {

        val currentUser = User(
            id = AuthManager.getCurrentUserId() ?: "",
            username = AuthManager.getCurrentUsername() ?: "Bạn",
            avatar = AuthManager.getCurrentAvatarUrl() ?: "",
        )

        val tempComment = Comment(
            id = "temp_${System.currentTimeMillis()}",
            content = content,
            likeCount = 0,
            replyCount = 0,
            createdAt = "${System.currentTimeMillis()}",
            isOwner = true,
            user = currentUser,
            parentId = parentId,
            sendStatus = SendStatus.SENDING
        )

        return if (parentId == null) {
            CommentItem.Parent(
                comment = tempComment,
                level = 0
            )
        } else {
            CommentItem.Reply(
                comment = tempComment,
                replyToUserName = replyToUserName ?: "",
                level = 1
            )
        }
    }


    fun toggleLike(commentId: String) {
        val currentState = _uiState.value

        val targetItem = currentState.comments.find { it.comment.id == commentId }
        if (targetItem == null) {
            return
        }

        val currentlyLiked = targetItem.comment.isLiked

        val updatedComments = currentState.comments.map { item ->
            if (item.comment.id == commentId) {
                val newLikeCount = if (currentlyLiked) {
                    (item.comment.likeCount - 1).coerceAtLeast(0)
                } else {
                    item.comment.likeCount + 1
                }

                val updatedComment = item.comment.copy(
                    likeCount = newLikeCount,
                    isLiked = !currentlyLiked
                )

                when (item) {
                    is CommentItem.Parent -> item.copy(comment = updatedComment)
                    is CommentItem.Reply -> item.copy(comment = updatedComment)
                }
            } else {
                item
            }
        }

        _uiState.update { state ->
            state.copy(
                comments = updatedComments,
                likingCommentIds = state.likingCommentIds + commentId
            )
        }

        viewModelScope.launch {
            val result = if (currentlyLiked) {
                repository.unlikeComment(commentId)
            } else {
                repository.likeComment(commentId)
            }

            result.onSuccess {

            }.onFailure { e ->

                _uiState.update { state ->
                    val rollbackComments = state.comments.map { item ->
                        if (item.comment.id == commentId) {
                            val rollbackLikeCount = if (currentlyLiked) {
                                item.comment.likeCount + 1  // vì optimistic đã trừ → rollback cộng lại
                            } else {
                                (item.comment.likeCount - 1).coerceAtLeast(0)
                            }

                            val rollbackComment = item.comment.copy(
                                likeCount = rollbackLikeCount,
                                isLiked = currentlyLiked
                            )

                            when (item) {
                                is CommentItem.Parent -> item.copy(comment = rollbackComment)
                                is CommentItem.Reply -> item.copy(comment = rollbackComment)
                            }
                        } else {
                            item
                        }
                    }

                    state.copy(
                        comments = rollbackComments,
                        likingCommentIds = state.likingCommentIds - commentId,
                        error = e.message
                    )
                }
            }

            _uiState.update { it.copy(likingCommentIds = it.likingCommentIds - commentId) }
        }
    }


    fun deleteComment(commentId: String) {
        val currentState = _uiState.value
        val previousComments = currentState.comments

        val itemToDelete = previousComments.find { it.comment.id == commentId } ?: return

        val updatedList = previousComments.toMutableList()

        if (itemToDelete is CommentItem.Parent) {
            updatedList.removeAll { item ->
                item.comment.id == commentId ||
                        (item is CommentItem.Reply && item.comment.parentId == commentId)
            }
        } else if (itemToDelete is CommentItem.Reply) {
            updatedList.removeAll { it.comment.id == commentId }

            val parentId = itemToDelete.comment.parentId
            if (parentId != null) {
                val parentIndex = updatedList.indexOfFirst { it.comment.id == parentId }
                if (parentIndex != -1) {
                    val parentItem = updatedList[parentIndex]
                    val newCount = (parentItem.comment.replyCount - 1).coerceAtLeast(0)
                    val updatedParentComment = parentItem.comment.copy(replyCount = newCount)

                    if (parentItem is CommentItem.Parent) {
                        updatedList[parentIndex] = parentItem.copy(comment = updatedParentComment)
                    }
                }
            }
        }

        _uiState.update { it.copy(comments = updatedList) }

        viewModelScope.launch {
            repository.deleteComment(commentId)
                .onSuccess {

                    println("DEBUG: Xóa thành công comment $commentId")
                }
                .onFailure { e ->
                    println("DEBUG: Xóa thất bại: ${e.message}")
                    _uiState.update {
                        it.copy(
                            comments = previousComments,
                            error = "Không thể xóa bình luận. Vui lòng thử lại."
                        )
                    }
                }
        }
    }

}