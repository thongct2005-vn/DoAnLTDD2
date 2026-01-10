package com.example.app.ui.feed.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.NoticeType


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    state: CreatePostUiState,
    onAction: (CreatePostAction) -> Unit,
    onAddMediaClick: () -> Unit,              // Callback từ Route (launcher + debounce)
    navController: NavHostController? = null  // Để đóng màn hình hoặc navigate thêm
) {
    val bg = Color(0xFF1F1F1F)
    val sheetBg = Color(0xFF2A2A2A)
    val textMain = Color.White
    val textSub = Color(0xFFBDBDBD)

    var showSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    BottomSheetScaffold(
        sheetPeekHeight = 180.dp,
        sheetContainerColor = sheetBg,
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(100))
                        .background(Color(0xFF777777))
                )
            }
        },
        sheetContent = {
            SheetItem(
                icon = { Icon(Icons.Filled.Image, null, tint = Color(0xFF4CAF50)) },
                title = "Ảnh/video",
                onClick = {
                    val imageCount = state.selectedImages.count { it.type == CreatePostAction.MediaType.IMAGE }
                    val videoCount = state.selectedImages.count { it.type == CreatePostAction.MediaType.VIDEO }

                    when {
                        state.isUploading -> onAction(CreatePostAction.ShowNotice("Đang tải lên, vui lòng đợi...", NoticeType.INFO))
                        videoCount >= 2 -> onAction(CreatePostAction.ShowNotice("Đã đủ 2 video", NoticeType.INFO))
                        imageCount >= 9 -> onAction(CreatePostAction.ShowNotice("Đã đủ 9 ảnh", NoticeType.INFO))
                        else -> onAddMediaClick()  // Gọi launcher từ Route
                    }
                },
                // Disable nút nếu không hợp lệ
                enabled = !state.isUploading &&
                        state.selectedImages.count { it.type == CreatePostAction.MediaType.IMAGE } < 9 &&
                        state.selectedImages.count { it.type == CreatePostAction.MediaType.VIDEO } < 2
            )

            SheetItem(
                icon = { Icon(Icons.Filled.PersonAddAlt, null, tint = Color(0xFF2196F3)) },
                title = "Gắn thẻ người khác",
                onClick = { onAction(CreatePostAction.TagClicked) }  // TODO: nếu gắn thẻ cần màn hình riêng → navController?.navigate("tag_screen")
            )
            Spacer(Modifier.height(8.dp))
        },
        containerColor = bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tạo bài viết", color = textMain, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() ?: onAction(CreatePostAction.CloseClicked) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = textMain)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onAction(CreatePostAction.PostClicked(context)) },
                        enabled = state.canNext && !state.isUploading
                    ) {
                        if (state.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                "Đăng",
                                color = if (state.canNext) textMain else Color(0xFF777777),
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
            AppNotice(
                text = state.noticeMessage,
                type = state.noticeType,
                onClose = { onAction(CreatePostAction.DismissNotice) }
            )

            if (state.noticeMessage != null) {
                Spacer(Modifier.height(8.dp))
            }

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

            // Privacy chip
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionChip(
                    text = state.privacy.label,
                    onClick = { showSheet = true }
                )
            }

            // Privacy bottom sheet
            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
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
                        PostPrivacy.entries.forEach { option ->
                            val isSelected = option == state.privacy
                            ListItem(
                                headlineContent = { Text(option.label, color = Color.White) },
                                modifier = Modifier.clickable {
                                    onAction(CreatePostAction.PrivacyChanged(option))
                                    showSheet = false
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

            // Input text
            OutlinedTextField(
                value = state.content,
                onValueChange = { onAction(CreatePostAction.ContentChanged(it)) },
                placeholder = {
                    Text("Bạn đang nghĩ gì?", color = textSub, fontSize = 20.sp)
                },
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

            // Image/Video Grid
            if (state.selectedImages.isNotEmpty()) {
                val displayImages = state.selectedImages.take(9)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    items(displayImages) { media ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (media.type == CreatePostAction.MediaType.VIDEO) {
                                        onAction(CreatePostAction.VideoClicked(media))
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = media.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (media.type == CreatePostAction.MediaType.VIDEO) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(26.dp)
                                    .clickable { onAction(CreatePostAction.RemoveImageClicked(media)) },
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(5.dp))
                            }
                        }
                    }
                }
            }
        }

        // Full screen video preview
        if (state.previewVideo != null) {
            FullScreenVideoPlayer(
                uri = state.previewVideo.uri,
                onDismiss = { onAction(CreatePostAction.DismissVideoPreview) }
            )
        }
    }
}

// Giữ nguyên các Composable phụ (ActionChip, SheetItem, FullScreenVideoPlayer)

// Giữ nguyên các Composable phụ của bạn
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
private fun SheetItem(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true  // ← Thêm param này
) {
    Surface(
        onClick = { if (enabled) onClick() else null },  // Không click nếu disable
        color = if (enabled) Color.Transparent else Color.Transparent.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)  // Làm mờ khi disable
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                icon()
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = title,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                fontSize = 18.sp
            )
        }
    }
}
@Composable
fun FullScreenVideoPlayer(
    uri: android.net.Uri,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Khởi tạo Player
    val exoPlayer = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    // Giải phóng bộ nhớ khi đóng
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // Dùng Dialog để hiển thị đè lên toàn màn hình
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // View phát video
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    androidx.media3.ui.PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true // Hiện nút play/pause/thanh tua
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Nút đóng ở góc trên
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, "Đóng", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}