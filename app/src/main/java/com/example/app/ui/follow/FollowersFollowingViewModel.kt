package com.example.app.ui.follow

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app.domain.model.FollowUser
import com.example.app.network.RetrofitClient
import com.example.app.network.api.UserApiService
import com.example.app.network.dto.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FollowerFollowingViewModel : ViewModel() {

    private val api = RetrofitClient.create(UserApiService::class.java)

    private val _items = MutableStateFlow<List<FollowUser>>(emptyList())
    val items = _items.asStateFlow()

    var nextCursor by mutableStateOf<String?>(null)
    var isOwner by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    fun loadData(userId: String, isFollowers: Boolean, isRefresh: Boolean = false) {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            val cursor = if (isRefresh) null else nextCursor

            try {
                val token = "Bearer ${AuthManager.getAccessToken()}"

                val res = if (isFollowers) api.getFollowers(userId, cursor = cursor)
                else api.getFollowing(userId, cursor = cursor)

                Log.d("DEBUG_API", "Raw Response: ${res.raw()}")
                Log.d("DEBUG_API", "Body: ${res.body()}")

                if (res.isSuccessful && res.body() != null) {
                    val body = res.body()!!
                    isOwner = body.is_owner
                    nextCursor = body.next_cursor

                    val rawList = if (isFollowers) body.follower else body.following
                    val mappedList = (rawList ?: emptyList()).map { dto ->
                        FollowUser(
                            id = dto.id,
                            username = dto.username ?: "User",
                            fullName = "Ngày: ${dto.created_at.take(10)}",
                            avatarUrl = dto.avatar,
                            isFollowing = true
                        )
                    }

                    _items.value = if (isRefresh) mappedList else _items.value + mappedList
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Lỗi rồi: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}