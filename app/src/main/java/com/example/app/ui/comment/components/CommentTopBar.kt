package com.example.app.ui.comment.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentTopBar(onClose: () -> Unit) {
    // Khai báo bảng màu đồng bộ
    val bg = Color(0xFF1F1F1F)
    val textMain = Color.White

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Bình luận",
                fontWeight = FontWeight.Bold,
                color = textMain // Cập nhật màu chữ tiêu đề
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = textMain // Cập nhật màu icon đóng
                )
            }
        },
        // Thiết lập màu sắc cho TopAppBar
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = bg, // Màu nền của thanh công cụ
            navigationIconContentColor = textMain,
            titleContentColor = textMain
        )
    )
}