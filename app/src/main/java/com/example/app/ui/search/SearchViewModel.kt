package com.example.app.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.SearchRepository
import com.example.app.domain.model.Post
import com.example.app.network.dto.search.SearchUserResponse
import com.example.app.utils.mapper.toDomain
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(
        val users: List<SearchUserResponse>,
        val posts: List<Post>
    ) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}


@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {
    private val repository = SearchRepository()
    private val queryFlow = MutableStateFlow("")

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var latestQuery: String = ""

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.value = SearchUiState.Idle
                    } else {
                        searchApi(query)
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        latestQuery = query
        queryFlow.value = query
    }

    fun onSubmitSearch() {
        if (queryFlow.value.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        viewModelScope.launch {
            searchApi(queryFlow.value)
        }
    }

    private suspend fun searchApi(query: String) {
        _uiState.value = SearchUiState.Loading
        try {
            val userResponse = repository.search(query)
            val postResponse = repository.searchPosts(query)

            if (userResponse.isSuccessful && postResponse.isSuccessful) {

                val users =
                    userResponse.body()?.data ?: emptyList()

                val posts =
                    postResponse.body()?.posts
                        ?.map { it.toDomain() }   // ✅ DTO → DOMAIN
                        ?: emptyList()

                _uiState.value = SearchUiState.Success(
                    users = users,
                    posts = posts
                )

            } else {
                _uiState.value = SearchUiState.Error(
                    "HTTP ${userResponse.code()} / ${postResponse.code()}"
                )
            }
        } catch (e: Exception) {
            _uiState.value = SearchUiState.Error("Lỗi mạng: ${e.message}")
        }
    }


}