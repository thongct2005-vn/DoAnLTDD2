package com.example.app.ui.profile

import android.content.Context
import android.net.Uri
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
import kotlin.compareTo

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val profile: Profile,
        val posts: List<Post>,
        val nextCursor: String? = null,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true
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

    // Optional: thêm refresh
    fun refresh() = loadProfile(currentUserId)



    fun initEditFormFromCurrentProfile() {
        val state = _uiState.value as? ProfileUiState.Success ?: return
        val p = state.profile

        _editState.value = EditProfileUiState(
            username = p.username,
            fullName = p.fullName,
            gender = _editState.value.gender,      // giữ lại nếu user đã chọn trước đó
            avatarUrl = p.avatarUrl,               // ✅ đúng field trong Profile.kt
            address = _editState.value.address,    // giữ lại
            phone = _editState.value.phone         // giữ lại
        )
    }

    fun onEditUsernameChange(v: String) = _editState.update { it.copy(username = v) }
    fun onEditFullNameChange(v: String) = _editState.update { it.copy(fullName = v) }
    fun onEditGenderChange(v: String) = _editState.update { it.copy(gender = v) }
    fun onEditAddressChange(v: String) = _editState.update { it.copy(address = v) }
    fun onEditPhoneChange(v: String) = _editState.update { it.copy(phone = v) }
    fun onEditAvatarPicked(uri: String) = _editState.update { it.copy(avatarLocalUri = uri) }

    fun submitEditProfile(context: Context) {
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
                    // ✅ user chọn ảnh mới -> upload -> lấy URL
                    val url = uploadToCloudinary(
                        context = context,
                        uri = Uri.parse(s.avatarLocalUri),
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

                // reload lại profile để đồng bộ (vì API không trả profile mới)
                loadProfile(currentUserId)
            } else {
                _editState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Cập nhật thất bại"
                    )
                }
            }
        }
    }

    fun resetEditSuccess() {
        _editState.update { it.copy(success = false, error = null) }
    }
    fun initEditFormFromIntent(username: String, fullName: String, avatarUrl: String?) {
        _editState.value = EditProfileUiState(
            username = username,
            fullName = fullName,
            avatarUrl = avatarUrl,
            gender = "",
            address = "",
            phone = ""
        )
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
}