package com.example.app.ui.comment.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ReplyControls(
    visibleCount: Int,
    totalCount: Int,
    onLoadMore: () -> Unit,
    onCollapse: () -> Unit
) {
    val remaining = totalCount - visibleCount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 100.dp, top = 4.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (remaining > 0) {
            Text(
                text = "Xem thêm $remaining phản hồi",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.clickable(onClick = onLoadMore)
            )
            Spacer(Modifier.width(20.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onCollapse)
        ) {
            Text(
                "Ẩn",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Thu gọn",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}