package com.example.app.ui.feed.post.media

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.app.domain.model.Media
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.app.ui.auth.components.BackIconButton

@Composable
fun FullScreenMediaViewer(
    mediaList: List<Media>?,
    onClose: () -> Unit
) {
    val items = mediaList ?: emptyList()

    // Khởi tạo trạng thái Pager
    val pagerState = rememberPagerState(pageCount = { items.size })

    BackHandler { onClose() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black // TikTok thường dùng nền đen tuyền
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Sử dụng VerticalPager thay vì LazyColumn
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { items[it].url } // Giúp tối ưu hóa việc render
            ) { page ->
                val media = items[page]

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (media.type.equals("video", ignoreCase = true)) {
                        VideoPlayer(
                            videoUrl = media.url,
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentHeight()

                            // Chiếm trọn màn hình
                        )
                    } else {
                        AsyncImage(
                            model = media.url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit // Giữ nguyên tỉ lệ ảnh để không bị méo
                        )
                    }
                }
            }

            BackIconButton(
                onClose,
                Color.White

            )
        }
    }
}