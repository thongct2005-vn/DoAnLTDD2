package com.example.app.ui.share.screen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.ui.share.model.NoticeType
import com.example.app.ui.share.model.SharePostAction
import com.example.app.ui.share.model.SharePostUiState
import com.example.app.ui.share.model.SharePrivacy

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SharePostScreen(
    state: SharePostUiState,
    onAction: (SharePostAction) -> Unit
) {
    val bg = Color(0xFF1F1F1F)
    val textMain = Color.White
    val textSub = Color(0xFFBDBDBD)
    val context = LocalContext.current

    var showPrivacySheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Chia sẻ bài viết",
                        color = textMain,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(SharePostAction.CloseClicked) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = textMain)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onAction(SharePostAction.ShareClicked(context)) },
                        enabled = state.canShare && !state.isUploading
                    ) {
                        if (state.isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Chia sẻ",
                                color = if (state.canShare) textMain else Color(0xFF777777),
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bg)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            ShareNotice(
                text = state.noticeMessage,
                type = state.noticeType,
                onClose = { onAction(SharePostAction.DismissNotice) }
            )

            if (state.noticeMessage != null) Spacer(Modifier.height(8.dp))

            // Header: Avatar + Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = state.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    state.userName.ifBlank { "Bạn" },
                    color = textMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            // Privacy chip row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionChip(
                    text = state.privacy.label,
                    onClick = { showPrivacySheet = true }
                )
            }

            // Privacy sheet
            if (showPrivacySheet) {
                ModalBottomSheet(
                    onDismissRequest = { showPrivacySheet = false },
                    containerColor = Color(0xFF1C2D35),
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(0.2f)) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            "Ai có thể xem bài viết của bạn?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                        SharePrivacy.entries.forEach { option ->
                            val isSelected = option == state.privacy
                            ListItem(
                                headlineContent = { Text(option.label, color = Color.White) },
                                modifier = Modifier.clickable {
                                    onAction(SharePostAction.PrivacyChanged(option))
                                    showPrivacySheet = false
                                },
                                trailingContent = {
                                    RadioButton(selected = isSelected, onClick = null)
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Input caption share
            OutlinedTextField(
                value = state.content,
                onValueChange = { onAction(SharePostAction.ContentChanged(it)) },
                placeholder = { Text("Viết lời bình...", color = textSub, fontSize = 20.sp) },
                textStyle = LocalTextStyle.current.copy(color = textMain, fontSize = 20.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color.White
                )
            )

            Spacer(Modifier.height(12.dp))

            // Preview bài viết gốc
            OriginalPostPreviewCard(
                author = state.originalAuthor,
                time = state.originalTime,
                content = state.originalContent
            )
        }
    }
}

@Composable
private fun ActionChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF243B55),
        contentColor = Color(0xFFB3D4FF),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun OriginalPostPreviewCard(
    author: String,
    time: String,
    content: String
) {
    val cardBg = Color(0xFF2A2A2A)
    val textSub = Color(0xFFBDBDBD)

    Surface(
        color = cardBg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(author, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(time, color = textSub, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                content,
                color = Color.White,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ShareNotice(
    text: String?,
    type: NoticeType,
    onClose: () -> Unit
) {
    if (text.isNullOrBlank()) return

    val bg = when (type) {
        NoticeType.INFO -> Color(0xFF1C2D35)
        NoticeType.SUCCESS -> Color(0xFF1E3A2F)
        NoticeType.ERROR -> Color(0xFF3A1E1E)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
        }
    }
}
