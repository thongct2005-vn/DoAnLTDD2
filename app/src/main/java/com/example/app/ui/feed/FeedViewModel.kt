package com.example.app.ui.feed

import androidx.lifecycle.ViewModel
import com.example.app.data.repository.PostRepository
import com.example.app.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val posts: List<Post>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

class FeedViewModel : ViewModel() {
    private val repository = PostRepository()

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

//    init {
//        loadFeed()
//    }
//
//    fun loadFeed() {
//        viewModelScope.launch {
//            _uiState.value = FeedUiState.Loading
//            repository.getFeedPosts()
//                .onSuccess { posts ->
//                    _uiState.value = FeedUiState.Success(posts)
//                }
//                .onFailure { exception ->
//                    _uiState.value = FeedUiState.Error(exception.message ?: "Không thể tải bài viết")
//                }
//        }
//    }
//
//    fun refresh() = loadFeed()
}