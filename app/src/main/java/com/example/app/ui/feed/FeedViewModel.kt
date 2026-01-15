package com.example.app.ui.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.PostRepository
import com.example.app.data.repository.UserRepository
import com.example.app.domain.model.Post
import com.example.app.domain.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class FeedUiState {
    object InitialLoading : FeedUiState()
    data class Success(
        val posts: List<Post>,
        val hasMore: Boolean,
        val isRefreshing: Boolean = false
    ) : FeedUiState()

    data class LoadingMore(val currentPosts: List<Post>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

sealed class SinglePostUiState {
    object Loading : SinglePostUiState()
    data class Success(val post: Post) : SinglePostUiState()
    data class Error(val message: String) : SinglePostUiState()
}

class FeedViewModel(
    private val repository: PostRepository = PostRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.InitialLoading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _currentPostUiState = MutableStateFlow<SinglePostUiState>(SinglePostUiState.Loading)
    val currentPostUiState: StateFlow<SinglePostUiState> = _currentPostUiState.asStateFlow()

    private var nextCursor: String? = null
    private val posts = mutableListOf<Post>()

    private var isLoading = false

    private val _savedChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val savedChanged = _savedChanged.asSharedFlow()

    init {
        loadFeed()
    }

    fun loadFeed(refresh: Boolean = false) {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true

            _uiState.value = when {
                refresh -> FeedUiState.InitialLoading
                posts.isNotEmpty() -> FeedUiState.LoadingMore(posts.toList())
                else -> FeedUiState.InitialLoading
            }

            if (refresh) {
                posts.clear()
                nextCursor = null
            }

            val result = repository.getPostFeed(
                limit = 10,
                cursor = nextCursor
            )

            result.onSuccess { response ->
                posts.addAll(response.posts)
                nextCursor = response.nextCursor

                _uiState.value = FeedUiState.Success(
                    posts = posts.toList(),
                    hasMore = nextCursor != null,
                    isRefreshing = false
                )
            }.onFailure { e ->
                _uiState.value = FeedUiState.Error(
                    message = e.message ?: "Không thể tải bài viết. Vui lòng thử lại."
                )
            }

            isLoading = false
        }
    }

    fun refresh() {
        loadFeed(refresh = true)
    }

    private val _scrollToTopAndRefreshEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTopAndRefreshEvent = _scrollToTopAndRefreshEvent.asSharedFlow()

    fun onHomeReselected() {
        _scrollToTopAndRefreshEvent.tryEmit(Unit)
    }

    fun loadMore() {
        if (nextCursor == null || isLoading) return
        loadFeed(refresh = false)
    }

    fun toggleLike(postId: String) {
        val currentState = _uiState.value
        val postsList = when (currentState) {
            is FeedUiState.Success -> currentState.posts
            is FeedUiState.LoadingMore -> currentState.currentPosts
            else -> return
        }

        val post = postsList.find { it.id == postId } ?: return

        val willLike = !post.isLiked
        val newLikeCountDelta = if (willLike) 1 else -1

        _uiState.update { state ->
            when (state) {
                is FeedUiState.Success -> state.copy(
                    posts = state.posts.map {
                        if (it.id == postId) it.copy(
                            isLiked = willLike,
                            likeCount = it.likeCount + newLikeCountDelta
                        ) else it
                    }
                )
                is FeedUiState.LoadingMore -> state.copy(
                    currentPosts = state.currentPosts.map {
                        if (it.id == postId) it.copy(
                            isLiked = willLike,
                            likeCount = it.likeCount + newLikeCountDelta
                        ) else it
                    }
                )
                else -> state
            }
        }

        viewModelScope.launch {
            val result = repository.toggleLike(postId, !willLike)

            if (result.isFailure) {
                _uiState.update { state ->
                    when (state) {
                        is FeedUiState.Success -> state.copy(
                            posts = state.posts.map {
                                if (it.id == postId) it.copy(
                                    isLiked = !willLike,
                                    likeCount = it.likeCount - newLikeCountDelta
                                ) else it
                            }
                        )
                        is FeedUiState.LoadingMore -> state.copy(
                            currentPosts = state.currentPosts.map {
                                if (it.id == postId) it.copy(
                                    isLiked = !willLike,
                                    likeCount = it.likeCount - newLikeCountDelta
                                ) else it
                            }
                        )
                        else -> state
                    }
                }
            }
        }
    }


    fun toggleSave(postId: String) {
        val currentState = _uiState.value
        val postsList = when (currentState) {
            is FeedUiState.Success -> currentState.posts
            is FeedUiState.LoadingMore -> currentState.currentPosts
            else -> return
        }

        val post = postsList.find { it.id == postId } ?: return

        val willSave = !post.isSaved
        _uiState.update { state ->
            when (state) {
                is FeedUiState.Success -> state.copy(
                    posts = state.posts.map {
                        if (it.id == postId) it.copy(isSaved = willSave) else it
                    }
                )
                is FeedUiState.LoadingMore -> state.copy(
                    currentPosts = state.currentPosts.map {
                        if (it.id == postId) it.copy(isSaved = willSave) else it
                    }
                )
                else -> state
            }
        }

        viewModelScope.launch {
            val result = repository.toggleSave(postId, !willSave)  // !willSave vì ta gửi trạng thái hiện tại (trước khi toggle)

            if (result.isFailure) {
                _uiState.update { state ->
                    when (state) {
                        is FeedUiState.Success -> state.copy(
                            posts = state.posts.map {
                                if (it.id == postId) it.copy(isSaved = !willSave) else it
                            }
                        )
                        is FeedUiState.LoadingMore -> state.copy(
                            currentPosts = state.currentPosts.map {
                                if (it.id == postId) it.copy(isSaved = !willSave) else it
                            }
                        )
                        else -> state
                    }
                }

            }
            _savedChanged.tryEmit(Unit)
        }
    }

    fun deletePost(postId: String) {
        _uiState.update { state ->
            when (state) {
                is FeedUiState.Success -> state.copy(
                    posts = state.posts.filterNot { it.id == postId }
                )
                is FeedUiState.LoadingMore -> state.copy(
                    currentPosts = state.currentPosts.filterNot { it.id == postId }
                )
                else -> state
            }
        }

        viewModelScope.launch {
            repository.deletePost(postId).onFailure {
                // Có thể show toast lỗi nếu cần
            }
        }
    }

    // THÊM: updatePostPrivacy (tương tự toggleLike, có optimistic update + rollback)
    fun updatePostPrivacy(postId: String, newPrivacy: String) {
        val currentState = _uiState.value
        val postsList = when (currentState) {
            is FeedUiState.Success -> currentState.posts
            is FeedUiState.LoadingMore -> currentState.currentPosts
            else -> return
        }

        val post = postsList.find { it.id == postId } ?: return
        val oldPrivacy = post.privacy

        // Optimistic update
        _uiState.update { state ->
            when (state) {
                is FeedUiState.Success -> state.copy(
                    posts = state.posts.map {
                        if (it.id == postId) it.copy(privacy = newPrivacy) else it
                    }
                )
                is FeedUiState.LoadingMore -> state.copy(
                    currentPosts = state.currentPosts.map {
                        if (it.id == postId) it.copy(privacy = newPrivacy) else it
                    }
                )
                else -> state
            }
        }

        viewModelScope.launch {
            repository.updatePostPrivacy(postId, newPrivacy)
                .onFailure {
                    // Rollback nếu API fail
                    _uiState.update { state ->
                        when (state) {
                            is FeedUiState.Success -> state.copy(
                                posts = state.posts.map {
                                    if (it.id == postId) it.copy(privacy = oldPrivacy) else it
                                }
                            )
                            is FeedUiState.LoadingMore -> state.copy(
                                currentPosts = state.currentPosts.map {
                                    if (it.id == postId) it.copy(privacy = oldPrivacy) else it
                                }
                            )
                            else -> state
                        }
                    }
                    // Optional: show error toast
                }
        }
    }


    private val userRepository = UserRepository()

    private val _onlineUsers = MutableStateFlow<List<User>>(emptyList())
    val onlineUsers: StateFlow<List<User>> = _onlineUsers.asStateFlow()

    private val _isLoadingOnline = MutableStateFlow(false)
    val isLoadingOnline: StateFlow<Boolean> = _isLoadingOnline.asStateFlow()

    fun loadOnlineUsers() {
        viewModelScope.launch {
            _isLoadingOnline.value = true

            val result = userRepository.getOnlineUsers()

            result.onSuccess { users ->
                _onlineUsers.value = users
            }.onFailure { e ->
                Log.e("FeedVM", "Load online users failed", e)
            }

            _isLoadingOnline.value = false
        }
    }

    private fun updatePostInFeedList(updatedPost: Post) {
        val index = posts.indexOfFirst { it.id == updatedPost.id }
        if (index != -1) {
            posts[index] = updatedPost
            // Trigger update UI Feed
            val currentState = _uiState.value
            if (currentState is FeedUiState.Success) {
                _uiState.value = currentState.copy(posts = posts.toList())
            } else if (currentState is FeedUiState.LoadingMore) {
                _uiState.value = currentState.copy(currentPosts = posts.toList())
            }
        }
    }


    fun getPostById(postId: String) {
        viewModelScope.launch {
            _currentPostUiState.value = SinglePostUiState.Loading

            // 1. Kiểm tra xem bài viết đã có trong list feed chưa để hiện ngay (trải nghiệm mượt)
            val existingPost = posts.find { it.id == postId }
            if (existingPost != null) {
                _currentPostUiState.value = SinglePostUiState.Success(existingPost)
            }

            // 2. Gọi API để lấy dữ liệu mới nhất
            repository.getPostById(postId).onSuccess { updatedPost ->
                // Cập nhật State cho màn Detail
                _currentPostUiState.value = SinglePostUiState.Success(updatedPost)

                // Cập nhật luôn vào List Feed (để khi back về Feed dữ liệu cũng mới)
                updatePostInFeedList(updatedPost)
            }.onFailure { e ->
                // Nếu chưa hiện gì thì báo lỗi, nếu đang hiện bài cũ (existingPost) thì thôi hoặc Toast
                if (_currentPostUiState.value is SinglePostUiState.Loading) {
                    _currentPostUiState.value = SinglePostUiState.Error(e.message ?: "Lỗi tải bài viết")
                }
            }
        }
    }

}