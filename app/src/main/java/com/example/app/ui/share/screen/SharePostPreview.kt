package com.example.app.ui.share.screen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.app.ui.share.model.NoticeType
import com.example.app.ui.share.model.SharePostUiState
import com.example.app.ui.share.model.SharePrivacy
import com.example.app.ui.share.screen.SharePostScreen

@Preview(
    name = "SharePost - Empty",
    showBackground = true,
    backgroundColor = 0xFF1F1F1F
)
@Composable
fun PreviewSharePost_Empty() {
    val state = SharePostUiState(
        userName = "Bạn",
        avatarUrl = null,
        privacy = SharePrivacy.PUBLIC,
        content = "",
        originalAuthor = "Trần Văn B",
        originalTime = "2 giờ trước",
        originalContent = "Hôm nay mình vừa hoàn thành một feature khá hay. Mọi người xem thử nhé!",
        noticeMessage = null,
        canShare = true,
        isUploading = false
    )

    MaterialTheme {
        Surface {
            SharePostScreen(state = state, onAction = {})
        }
    }
}

@Preview(
    name = "SharePost - With caption + notice",
    showBackground = true,
    backgroundColor = 0xFF1F1F1F
)
@Composable
fun PreviewSharePost_WithCaption() {
    val state = SharePostUiState(
        userName = "Nguyễn Văn A",
        avatarUrl = null,
        privacy = SharePrivacy.FRIENDS,
        content = "Bài này hay nè, share cho mọi người 👌",
        originalAuthor = "Lê Thị C",
        originalTime = "Hôm qua",
        originalContent = "Một vài tips Compose giúp UI mượt hơn: dùng remember, derivedStateOf...",
        noticeMessage = "Demo preview: đây là notice",
        noticeType = NoticeType.INFO,
        canShare = true,
        isUploading = false
    )

    MaterialTheme {
        Surface {
            SharePostScreen(state = state, onAction = {})
        }
    }
}

@Preview(
    name = "SharePost - Uploading",
    showBackground = true,
    backgroundColor = 0xFF1F1F1F
)
@Composable
fun PreviewSharePost_Uploading() {
    val state = SharePostUiState(
        userName = "Nguyễn Văn A",
        avatarUrl = null,
        privacy = SharePrivacy.PUBLIC,
        content = "Đang share...",
        originalAuthor = "Ngô D",
        originalTime = "5 phút trước",
        originalContent = "Chia sẻ bài viết test uploading",
        noticeMessage = null,
        canShare = true,
        isUploading = true
    )

    MaterialTheme {
        Surface {
            SharePostScreen(state = state, onAction = {})
        }
    }
}
