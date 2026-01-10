package com.example.app.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.SearchRepository
import com.example.app.network.dto.search.SearchUserResponse
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<SearchUserResponse>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {
    private val repository = SearchRepository()
    private val queryFlow = MutableStateFlow("")

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

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
        queryFlow.value = query
    }

//    private suspend fun searchApi(query: String) {
//        _uiState.value = SearchUiState.Loading
//        try {
//            val response = repository.search(query)
//            if (response.isSuccessful) {
//                _uiState.value = SearchUiState.Success(response.body()?.data ?: emptyList())
//            } else {
//                _uiState.value = SearchUiState.Error("Không tìm thấy kết quả")
//            }
//        } catch (e: Exception) {
//            _uiState.value = SearchUiState.Error("Lỗi mạng: ${e.message}")
//        }
//    }

    private suspend fun searchApi(query: String) {
        _uiState.value = SearchUiState.Loading
        try {
            val response = repository.search(query)

            Log.d("SEARCH_API", "code=${response.code()} body=${response.body()}")

            if (response.isSuccessful) {
                _uiState.value = SearchUiState.Success(
                    response.body()?.data ?: emptyList()
                )
            } else {
                _uiState.value = SearchUiState.Error("HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            _uiState.value = SearchUiState.Error("Lỗi mạng: ${e.message}")
        }
    }

}