package com.example.app.ui.share.model
import android.content.Context
import android.net.Uri

enum class SharePrivacy(val label: String) {
    PUBLIC("Công khai"),
    FRIENDS("Bạn bè"),
    ONLY_ME("Chỉ mình tôi")
}

enum class NoticeType { INFO, SUCCESS, ERROR }

data class SharePostUiState(
    val userName: String = "Bạn",
    val avatarUrl: Any? = null,

    val privacy: SharePrivacy = SharePrivacy.PUBLIC,
    val content: String = "",

    // Post gốc để preview
    val originalAuthor: String = "Tác giả",
    val originalContent: String = "Nội dung bài viết gốc...",
    val originalTime: String = "1 giờ trước",

    val noticeMessage: String? = null,
    val noticeType: NoticeType = NoticeType.INFO,

    val canShare: Boolean = true,
    val isUploading: Boolean = false,

    // share thường không cho chọn media mới, nên để trống (nếu bạn muốn bật sau)
    val selectedImages: List<ShareMediaItem> = emptyList(),
)

data class ShareMediaItem(
    val uri: Uri,
    val type: SharePostAction.MediaType = SharePostAction.MediaType.IMAGE
)

sealed class SharePostAction {
    data object CloseClicked : SharePostAction()
    data class ShareClicked(val context: Context) : SharePostAction()

    data class PrivacyChanged(val option: SharePrivacy) : SharePostAction()
    data class ContentChanged(val value: String) : SharePostAction()

    data object DismissNotice : SharePostAction()

    enum class MediaType { IMAGE, VIDEO }
}

