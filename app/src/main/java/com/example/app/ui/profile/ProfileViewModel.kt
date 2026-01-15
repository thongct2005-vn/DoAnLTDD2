package com.example.app.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.PostRepository
import com.example.app.data.repository.ProfileRepository
import com.example.app.data.repository.uploadToCloudinary
import com.example.app.domain.model.Post
import com.example.app.domain.model.Profile
import com.example.app.ui.profile.edit.EditProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.net.toUri


sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val profile: Profile,
        val posts: List<Post>,
        val nextCursor: String? = null,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,

        // ✅ Saved riêng
        val savedPosts: List<Post> = emptyList(),
        val savedNextCursor: String? = null,
        val isLoadingMoreSaved: Boolean = false,
        val hasMoreSaved: Boolean = true
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {

    private val profileRepository = ProfileRepository()

    private val postRepository = PostRepository()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: String = "me"
    private var isLoadingMore = false

    private val _editState = MutableStateFlow(EditProfileUiState())
    val editState: StateFlow<EditProfileUiState> = _editState.asStateFlow()
    fun loadProfile(userId: String = "me") {
        currentUserId = userId

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            profileRepository.getProfile(userId)
                .onSuccess { (profile, initialPosts) ->
                    postRepository.addToCache(initialPosts)
                    val hasMore = initialPosts.size >= 5
                    _uiState.value = ProfileUiState.Success(
                        profile = profile,
                        posts = initialPosts,
                        nextCursor = null,
                        isLoadingMore = false,
                        hasMore = hasMore
                    )
                    isLoadingMore = false
                }
                .onFailure { exception ->
                    _uiState.value = ProfileUiState.Error(
                        exception.message ?: "Không thể tải thông tin cá nhân"
                    )
                }
        }
    }

    fun loadMorePosts() {
        val currentState = _uiState.value
        if (currentState !is ProfileUiState.Success) return
        if (isLoadingMore || !currentState.hasMore) return

        isLoadingMore = true
        _uiState.value = currentState.copy(isLoadingMore = true)

        viewModelScope.launch {
            profileRepository.getMoreProfilePosts(
                userId = currentUserId,
                limit = 5,
                cursor = currentState.nextCursor
            )
                .onSuccess { result ->
                    postRepository.addToCache(result.posts)
                    val hasMore = result.posts.isNotEmpty() && result.nextCursor != null

                    val updatedPosts = (currentState.posts + result.posts).distinctBy { it.id }

                    _uiState.value = currentState.copy(
                        posts = updatedPosts, // Danh sách đã được làm sạch
                        nextCursor = result.nextCursor,
                        isLoadingMore = false,
                        hasMore = hasMore
                    )
                    isLoadingMore = false
                }
                .onFailure {
                    _uiState.value = currentState.copy(isLoadingMore = false)
                    isLoadingMore = false
                }
        }
    }

    fun loadPostSave() {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return
        if (!currentState.profile.isOwner) return

        // ✅ dùng state riêng cho saved
        if (currentState.isLoadingMoreSaved || !currentState.hasMoreSaved) return

        _uiState.update { (it as ProfileUiState.Success).copy(isLoadingMoreSaved = true) }

        viewModelScope.launch {
            try {
                val result = postRepository.getPostSave(
                    limit = 5,
                    cursor = currentState.savedNextCursor   // ✅ cursor riêng của saved
                ).getOrThrow()

                postRepository.addToCache(result.posts)

                val hasMoreSaved = result.posts.isNotEmpty() && result.nextCursor != null
                val updatedSavedPosts = (currentState.savedPosts + result.posts).distinctBy { it.id }

                _uiState.update { state ->
                    (state as ProfileUiState.Success).copy(
                        savedPosts = updatedSavedPosts,
                        savedNextCursor = result.nextCursor,   // ✅ lưu cursor riêng
                        isLoadingMoreSaved = false,
                        hasMoreSaved = hasMoreSaved
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    (state as ProfileUiState.Success).copy(isLoadingMoreSaved = false)
                }
                Log.e("ProfileVM", "Load saved posts failed", e)
            }
        }
    }



    fun onEditUsernameChange(v: String) = _editState.update { it.copy(username = v) }
    fun onEditFullNameChange(v: String) = _editState.update { it.copy(fullName = v) }
    fun onEditGenderChange(v: String) = _editState.update { it.copy(gender = v) }
    fun onEditAddressChange(v: String) = _editState.update { it.copy(address = v) }
    fun onEditPhoneChange(v: String) = _editState.update { it.copy(phone = v) }
    fun onEditAvatarPicked(uri: String) = _editState.update { it.copy(avatarLocalUri = uri) }

    fun submitEditProfile(context: Context, onSuccess:()->Unit) {
        val s = _editState.value

        if (s.username.isBlank()) {
            _editState.update { it.copy(error = "Username không được trống") }
            return
        }
        if (s.username.length < 6) {
            _editState.update { it.copy(error = "Tên phải có độ dài lớn 6") }
            return
        }
        if (s.fullName.isBlank()) {
            _editState.update { it.copy(error = "Họ tên không được trống") }
            return
        }

        viewModelScope.launch {
            _editState.update { it.copy(isLoading = true, error = null, success = false) }

            // tạm thời: chưa upload ảnh -> chưa có avatar string đúng backend
            val avatarString: String? = when {
                !s.avatarLocalUri.isNullOrBlank() -> {

                    val url = uploadToCloudinary(
                        context = context,
                        uri = s.avatarLocalUri.toUri(),
                        uploadPreset = "my_unsigned_preset"
                    )
                    url
                }
                !s.avatarUrl.isNullOrBlank() -> s.avatarUrl // ✅ không đổi ảnh -> giữ url cũ
                else -> null
            }
            // nếu bạn đang có avatarUrl sẵn từ profile: avatarString = _editState.value.avatarUrl

            val result = profileRepository.updateProfile(
                username = s.username.trim(),
                fullName = s.fullName.trim(),
                gender = s.gender.takeIf { it.isNotBlank() },
                avatar = avatarString, // ✅ gửi URL cloudinary
                address = s.address.trim().takeIf { it.isNotBlank() },
                phone = s.phone.trim().takeIf { it.isNotBlank() }
            )

            if (result.isSuccess) {
                _editState.update { it.copy(isLoading = false, success = true) }
                onSuccess()
                // reload lại profile để đồng bộ (vì API không trả profile mới)
                loadProfile(currentUserId)
            } else {
                _editState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Cập nhật thất bại",
                    )
                }
            }
        }
    }




    fun toggleFollow(targetUserId: String) {
        val currentState = _uiState.value
        if (currentState !is ProfileUiState.Success) return

        val currentProfile = currentState.profile
        val isCurrentlyFollowing = currentProfile.isFollowing

        viewModelScope.launch {
            // Gọi Repository
            val result = if (isCurrentlyFollowing) {
                profileRepository.unfollowUser(targetUserId)
            } else {
                profileRepository.followUser(targetUserId)
            }

            // SỬA TẠI ĐÂY: Sử dụng .isSuccess của Kotlin Result
            if (result.isSuccess) {
                // Cập nhật UI ngay lập tức vì Repository đã lo phần check 409 rồi
                _uiState.value = currentState.copy(
                    profile = currentProfile.copy(
                        isFollowing = !isCurrentlyFollowing,
                        followerCount = if (isCurrentlyFollowing)
                            currentProfile.followerCount - 1
                        else
                            currentProfile.followerCount + 1
                    )
                )
            } else {
                // Xử lý khi thực sự có lỗi (ví dụ: mất mạng)
                val error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                Log.e("ProfileVM", "Toggle follow failed: $error")
            }
        }
    }

    // Thêm repository post vào (nếu bạn gộp chung hoặc dùng repository riêng)
// private val postRepository = PostRepository()
    fun toggleLike(postId: String) {
        val state = _uiState.value as? ProfileUiState.Success ?: return
        val post = state.posts.find { it.id == postId } ?: return

        val willLike = !post.isLiked
        val newLikeCountDelta = if (willLike) 1 else -1

        // Optimistic update
        _uiState.update { current ->
            if (current !is ProfileUiState.Success) return@update current

            current.copy(
                posts = current.posts.map {
                    if (it.id == postId) it.copy(
                        isLiked = willLike,
                        likeCount = it.likeCount + newLikeCountDelta
                    ) else it
                }
            )
        }

        viewModelScope.launch {
            val result = postRepository.toggleLike(postId, !willLike) // !willLike = trạng thái server đang biết

            if (result.isFailure) {
                // Rollback + thông báo lỗi
                _uiState.update { current ->
                    if (current !is ProfileUiState.Success) return@update current
                    current.copy(
                        posts = current.posts.map {
                            if (it.id == postId) it.copy(
                                isLiked = !willLike,
                                likeCount = it.likeCount - newLikeCountDelta
                            ) else it
                        }
                    )
                }
            }
        }
    }


    fun toggleSave(postId: String) {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return

        // post có thể nằm trong posts hoặc savedPosts
        val postInPosts = currentState.posts.find { it.id == postId }
        val postInSaved = currentState.savedPosts.find { it.id == postId }
        val postToToggle = postInPosts ?: postInSaved ?: return

        val willSave = !postToToggle.isSaved

        // ✅ Optimistic update:
        // - Nếu willSave = false (unSave) và đang ở savedPosts -> remove khỏi danh sách savedPosts ngay
        _uiState.update { state ->
            if (state !is ProfileUiState.Success) return@update state

            val newPosts = state.posts.map {
                if (it.id == postId) it.copy(isSaved = willSave) else it
            }

            val newSavedPosts =
                if (!willSave) {
                    // unSave: remove khỏi list "Đã lưu"
                    state.savedPosts.filterNot { it.id == postId }
                } else {
                    // save: chỉ set cờ (hoặc có thể refreshSavedPosts để add đúng dữ liệu)
                    state.savedPosts.map { if (it.id == postId) it.copy(isSaved = true) else it }
                }

            state.copy(
                posts = newPosts,
                savedPosts = newSavedPosts
            )
        }

        viewModelScope.launch {
            try {
                val result = postRepository.toggleSave(postId, !willSave)

                if (result.isFailure) {
                    // ❌ Rollback nếu fail
                    _uiState.update { state ->
                        if (state !is ProfileUiState.Success) return@update state

                        val rbPosts = state.posts.map {
                            if (it.id == postId) it.copy(isSaved = !willSave) else it
                        }

                        val rbSavedPosts =
                            if (!willSave) {
                                // rollback unSave: add lại post vào savedPosts
                                // (dùng postToToggle bản cũ)
                                listOf(postToToggle.copy(isSaved = true)) + state.savedPosts
                            } else {
                                // rollback save: set false lại
                                state.savedPosts.map {
                                    if (it.id == postId) it.copy(isSaved = false) else it
                                }
                            }

                        state.copy(
                            posts = rbPosts,
                            savedPosts = rbSavedPosts
                        )
                    }
                } else {
                    if (willSave) {
                        refreshSavedPosts()
                    }
                }
            } catch (e: Exception) {
                Log.e("Debug: ","$e")
                _uiState.update { state ->
                    if (state !is ProfileUiState.Success) return@update state

                    val rbPosts = state.posts.map {
                        if (it.id == postId) it.copy(isSaved = !willSave) else it
                    }

                    val rbSavedPosts =
                        if (!willSave) {
                            listOf(postToToggle.copy(isSaved = true)) + state.savedPosts
                        } else {
                            state.savedPosts.map {
                                if (it.id == postId) it.copy(isSaved = false) else it
                            }
                        }

                    state.copy(
                        posts = rbPosts,
                        savedPosts = rbSavedPosts
                    )
                }
            }
        }
    }


    fun deletePost(postId: String) {
        val current = _uiState.value
        if (current is ProfileUiState.Success) {
            _uiState.value = current.copy(
                posts = current.posts.filterNot { it.id == postId }
            )
        }
    }
    fun updatePostPrivacy(postId: String, newPrivacy: String) {
        val current = _uiState.value
        if (current is ProfileUiState.Success) {
            _uiState.value = current.copy(
                posts = current.posts.map { p ->
                    if (p.id == postId) p.copy(privacy = newPrivacy) else p
                }
            )
        }
    }

    suspend fun loadUserDetailForEdit(userId: String) {
        _editState.update { it.copy(isLoading = true) }
        val result = runCatching { profileRepository.getUserDetails(userId) } // gọi API getUserDetails(id)
        if (result.isSuccess) {
            val user = result.getOrNull()!!
            _editState.update {
                it.copy(
                    username = user.username,
                    fullName = user.fullName,
                    gender = user.gender ?: "other",
                    address = user.address ?: "",
                    phone = user.phone ?: "",
                    avatarUrl = user.avatar,
                    isLoading = false
                )
            }
            Log.d("DEBUG_EDIT", "Current Username: ${user.username}")
        } else {
            _editState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Lỗi tải dữ liệu", isLoading = false) }
        }
    }
    fun refreshSavedPosts() {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return
        if (!currentState.profile.isOwner) return

        viewModelScope.launch {
            // ✅ chỉ bật loading + reset cursor, KHÔNG xóa list hiện tại
            _uiState.update { state ->
                val s = state as ProfileUiState.Success
                s.copy(
                    isLoadingMoreSaved = true,
                    savedNextCursor = null,
                    hasMoreSaved = true
                    // ❌ KHÔNG set savedPosts = emptyList()
                )
            }

            try {
                val result = postRepository.getPostSave(
                    limit = 5,
                    cursor = null
                ).getOrThrow()

                postRepository.addToCache(result.posts)

                val hasMoreSaved = result.posts.isNotEmpty() && result.nextCursor != null

                _uiState.update { state ->
                    val s = state as ProfileUiState.Success
                    s.copy(
                        savedPosts = result.posts.distinctBy { it.id },
                        savedNextCursor = result.nextCursor,
                        isLoadingMoreSaved = false,
                        hasMoreSaved = hasMoreSaved
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    val s = state as ProfileUiState.Success
                    s.copy(isLoadingMoreSaved = false)
                }
                Log.e("ProfileVM", "Refresh saved posts failed", e)
            }
        }
    }

}