package com.example.app.ui.share.screen
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.app.ui.share.model.SharePostAction
import com.example.app.ui.share.model.SharePostUiState
import com.example.app.ui.share.model.SharePrivacy

class SharePostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val originalAuthor = intent.getStringExtra("original_author") ?: "Nguyễn Văn A"
        val originalContent = intent.getStringExtra("original_content")
            ?: "Đây là nội dung bài viết gốc để preview."
        val originalTime = intent.getStringExtra("original_time") ?: "1 giờ trước"

        setContent {
            var state by remember {
                mutableStateOf(
                    SharePostUiState(
                        userName = "Bạn",
                        avatarUrl = null,
                        privacy = SharePrivacy.PUBLIC,
                        content = "",
                        originalAuthor = originalAuthor,
                        originalContent = originalContent,
                        originalTime = originalTime,
                        canShare = true,
                        isUploading = false,
                        noticeMessage = null
                    )
                )
            }

            val ctx = LocalContext.current

            SharePostScreen(
                state = state,
                onAction = { action ->
                    when (action) {
                        SharePostAction.CloseClicked -> finish()

                        is SharePostAction.ContentChanged -> {
                            state = state.copy(
                                content = action.value,
                                canShare = true // bạn có thể set điều kiện: content.isNotBlank()
                            )
                        }

                        is SharePostAction.PrivacyChanged -> {
                            state = state.copy(privacy = action.option)
                        }

                        is SharePostAction.ShareClicked -> {
                            // demo: show toast
                            Toast.makeText(
                                ctx,
                                "Đã bấm Chia sẻ (demo)\nPrivacy: ${state.privacy.label}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        SharePostAction.DismissNotice -> {
                            state = state.copy(noticeMessage = null)
                        }
                    }
                }
            )
        }
    }
}
