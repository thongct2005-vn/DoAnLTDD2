package com.example.app.ui.feed.post.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.example.app.domain.model.Media
import com.example.app.domain.model.Post
import com.example.app.domain.model.Profile

@Composable
fun MediaGrid(
    mediaList: List<Media>?,
    post: Post,
    profile: Profile
) {
    val items = mediaList ?: emptyList()
    if (items.isEmpty()) return

    var isFullScreenVisible by remember { mutableStateOf(false) }
    val onImageClick = { isFullScreenVisible = true }
    val commonHeight = 300.dp
    val modifier = Modifier.fillMaxWidth()

    when (items.size) {
        1 -> {
            MediaImage(
                media = items[0], // Đã sửa: Truyền cả object media
                modifier = modifier
                    .heightIn(min = 200.dp, max = 400.dp)
                    .clickable { onImageClick() }
            )
        }

        2 -> {
            Row(modifier = modifier.height(commonHeight)) {
                MediaImage(media = items[0], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
                Spacer(modifier = Modifier.width(4.dp))
                MediaImage(media = items[1], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
            }
        }

        3 -> {
            Column(modifier = modifier) {
                MediaImage(media = items[0], modifier = Modifier.fillMaxWidth().height(200.dp).clickable { onImageClick() })
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    MediaImage(media = items[1], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
                    Spacer(modifier = Modifier.width(4.dp))
                    MediaImage(media = items[2], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
                }
            }
        }

        4 -> {
            Column(modifier = modifier.heightIn(min = 200.dp, max = 400.dp)) {
                Row(modifier = Modifier.weight(1f)) {
                    MediaImage(media = items[0], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
                    Spacer(modifier = Modifier.width(4.dp))
                    MediaImage(media = items[1], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.weight(1f)) {
                    MediaImage(media = items[2], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
                    Spacer(modifier = Modifier.width(4.dp))
                    MediaImage(media = items[3], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
                }
            }
        }

        else -> {
            val remainingCount = items.size - 4
            Column(modifier = modifier.heightIn(min = 200.dp, max = 400.dp)) {
                Row(modifier = Modifier.weight(1f)) {
                    MediaImage(media = items[0], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
                    Spacer(modifier = Modifier.width(4.dp))
                    MediaImage(media = items[1], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.weight(1f)) {
                    MediaImage(media = items[2], modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() })
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { onImageClick() }) {
                        MediaImage(media = items[3], modifier = Modifier.fillMaxSize()) // Sửa images thành items nếu chưa đổi tên
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Black.copy(alpha = 0.3f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "+$remainingCount", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (isFullScreenVisible) {
        Dialog(
            onDismissRequest = { isFullScreenVisible = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            FullScreenMediaViewer(
                mediaList = items,
                onClose = { isFullScreenVisible = false }
            )
        }
    }
}

@Composable
fun MediaImage(
    media: Media,
    modifier: Modifier
) {
    val context = LocalContext.current

    // Tạo imageLoader hỗ trợ lấy frame từ video nếu chưa có trong Application class
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = media.url,
            imageLoader = imageLoader,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        if (media.type == "video") {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}