package com.example.app.ui.feed.create

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.uploadToCloudinary
import com.example.app.domain.model.Media
import com.example.app.ui.auth.components.NoticeType
import com.example.app.utils.size.getFileSizeMB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class CreatePostViewModel(
    avatarUrl: String?,
    userName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreatePostUiState(
            avatarUrl = avatarUrl,
            userName = userName
        )
    )
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    private val _events = Channel<CreatePostUiEvent>(Channel.BUFFERED)
    val events: Flow<CreatePostUiEvent> = _events.receiveAsFlow()

    private val uploadPreset = "my_unsigned_preset"
    fun onAction(action: CreatePostAction) {
        when (action) {
            is CreatePostAction.ImagesSelected -> {
                _uiState.update { currentState ->
                    val currentList = currentState.selectedImages

                    // Chỉ thêm những Uri chưa tồn tại trong danh sách cũ
                    val newUniqueMedia = action.mediaList.filter { newMedia ->
                        currentList.none { it.uri == newMedia.uri }
                    }

                    val combinedList = currentList + newUniqueMedia

                    // Vẫn dùng take(9) để đảm bảo tuyệt đối không lỗi logic UI
                    currentState.copy(selectedImages = combinedList.take(9))
                }
            }
            is CreatePostAction.RemoveImageClicked -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        // Lọc bỏ đối tượng Media có URI khớp với cái cần xóa
                        selectedImages = currentState.selectedImages.filter { media ->
                            media.uri != action.media.uri
                        }
                    )
                }
            }
            is CreatePostAction.ContentChanged -> {
                _uiState.update { it.copy(content = action.value, errorMessage = null) }
            }

            is CreatePostAction.PrivacyChanged -> {
                _uiState.update { it.copy(privacy = action.value) }
            }
            is CreatePostAction.VideoClicked -> {
                _uiState.update { it.copy(previewVideo = action.media) }
            }

            CreatePostAction.DismissVideoPreview -> {
                _uiState.update { it.copy(previewVideo = null) }
            }

            CreatePostAction.CloseClicked -> emitEvent(CreatePostUiEvent.Close)

            is CreatePostAction.PostClicked -> {
                val state = _uiState.value
                val trimmed = state.content.trim()
                if (trimmed.isEmpty() && state.selectedImages.isEmpty()) {
                    emitEvent(CreatePostUiEvent.ShowMessage("Bạn chưa nhập nội dung hoặc chọn hình ảnh!"))
                    return
                }

                // GỌI UPLOAD (đợi xong) rồi mới Navigate
                uploadSelectedMediaAndNavigate(action.context, trimmed, state.privacy)
            }
            is CreatePostAction.ShowNotice -> {
                _uiState.update { it.copy(noticeMessage = action.message, noticeType = action.type) }

                // Tự ẩn sau 3 giây
                viewModelScope.launch {
                    kotlinx.coroutines.delay(3000)
                    _uiState.update { it.copy(noticeMessage = null) }
                }
            }
            is CreatePostAction.DismissNotice -> {
                _uiState.update {
                    it.copy(noticeMessage = null)
                }
            }
            CreatePostAction.TagClicked -> emitEvent(CreatePostUiEvent.ShowMessage("Gắn thẻ (TODO)"))
            CreatePostAction.FeelingClicked -> emitEvent(CreatePostUiEvent.ShowMessage("Cảm xúc (TODO)"))
            CreatePostAction.LocationClicked -> emitEvent(CreatePostUiEvent.ShowMessage("Vị trí (TODO)"))
            CreatePostAction.MoreClicked -> emitEvent(CreatePostUiEvent.ShowMessage("Thêm (TODO)"))
        }
    }

    private fun uploadSelectedMediaAndNavigate(
        context: Context,
        content: String,
        privacy: PostPrivacy
    ) {
        val mediaList = _uiState.value.selectedImages

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isUploading = true) }

                val uploadedUrls = if (mediaList.isEmpty()) {
                    emptyList()
                } else {
                    coroutineScope {
                        mediaList.map { media ->
                            async(Dispatchers.IO) {
                                uploadToCloudinary(context, media.uri, uploadPreset)
                            }
                        }.awaitAll()
                    }
                }

                // ← CHUYỂN ĐỔI THÀNH LIST<MediaItem>
                val mediaItems = uploadedUrls.map { url ->
                    val type = if (url.contains("video", ignoreCase = true) ||
                        url.endsWith(".mp4", ignoreCase = true) ||
                        url.endsWith(".mov", ignoreCase = true)) {
                        "video"
                    } else {
                        "image"
                    }
                    Media(type = type, url = url)
                }
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        mediaItems = mediaItems,
                        errorMessage = null
                    )
                }

                emitEvent(
                    CreatePostUiEvent.NavigateNext(
                        content = content,
                        privacy = privacy,
                        mediaItems = mediaItems
                    )
                )
            } catch (e: Exception) {
                Log.d("Erorr: ","$e")
            } finally {
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    private fun emitEvent(event: CreatePostUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }
    // Trong CreatePostViewModel
    fun handleMediaSelection(uris: List<Uri>, context: Context) {
        viewModelScope.launch {
            // 1. Lấy danh sách hiện tại để so sánh
            val currentSelected = _uiState.value.selectedImages

            // 2. Lọc bỏ các URI đã tồn tại trong danh sách cũ ngay từ đầu
            val uniqueNewUris = uris.filter { uri ->
                currentSelected.none { it.uri == uri }
            }

            // Nếu sau khi lọc không còn URI nào mới thì thoát luôn
            if (uniqueNewUris.isEmpty()) return@launch

            // Lấy số lượng hiện tại để tính toán giới hạn
            val currentImages = currentSelected.count { it.type == CreatePostAction.MediaType.IMAGE }
            val currentVideos = currentSelected.count { it.type == CreatePostAction.MediaType.VIDEO }

            val validMedia = mutableListOf<CreatePostAction.SelectedMedia>()

            uniqueNewUris.forEach { uri ->
                val sizeMB = getFileSizeMB(uri, context)
                val isVideo = context.contentResolver.getType(uri)?.startsWith("video") == true

                // Tính toán số lượng dựa trên những gì đã có + những gì vừa được thêm vào validMedia trong vòng lặp này
                val totalVideosPending = currentVideos + validMedia.count { it.type == CreatePostAction.MediaType.VIDEO }
                val totalImagesPending = currentImages + validMedia.count { it.type == CreatePostAction.MediaType.IMAGE }

                when {
                    sizeMB > 50.0 -> {
                        onAction(CreatePostAction.ShowNotice("File > 50MB bị loại bỏ", NoticeType.WARNING))
                    }

                    isVideo -> {
                        if (totalImagesPending > 0) {
                            onAction(CreatePostAction.ShowNotice("Đã chọn ảnh, không thể thêm video", NoticeType.ERROR))
                        } else if (totalVideosPending >= 2) {
                            onAction(CreatePostAction.ShowNotice("Tối đa chỉ được chọn 2 video", NoticeType.ERROR))
                        } else {
                            validMedia.add(CreatePostAction.SelectedMedia(uri, CreatePostAction.MediaType.VIDEO))
                        }
                    }

                    else -> { // Trường hợp là Ảnh
                        if (totalVideosPending > 0) {
                            onAction(CreatePostAction.ShowNotice("Đã chọn video, không thể thêm ảnh", NoticeType.ERROR))
                        } else if (totalImagesPending >= 9) {
                            onAction(CreatePostAction.ShowNotice("Đã đạt giới hạn 9 ảnh", NoticeType.WARNING))
                        } else {
                            validMedia.add(CreatePostAction.SelectedMedia(uri, CreatePostAction.MediaType.IMAGE))
                        }
                    }
                }
            }

            if (validMedia.isNotEmpty()) {
                _uiState.update { it.copy(selectedImages = it.selectedImages + validMedia) }
            }
        }
    }
}
