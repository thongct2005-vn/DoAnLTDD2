package com.example.app.ui.comment.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.utils.time.formatTimeAgo  // Điều chỉnh package nếu cần

@Composable
fun InteractionRow(
    createdAt: Long,
    onReplyClick: () -> Unit,
    liked: Boolean,
    disliked: Boolean,
    likeCount: Int,
    onLikeToggle: () -> Unit,
    onDislikeToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatTimeAgo(createdAt),
            fontSize = 12.sp,
            color = Color.Gray
        )

        Text(
            "Trả lời",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier
                .padding(start = 15.dp)
                .clickable { onReplyClick() }
        )

        Spacer(Modifier.weight(1f))

        // Nút Like
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onLikeToggle() }
        ) {
            Icon(
                imageVector = if (liked) Icons.Filled.ThumbUp else Icons.Default.ThumbUp,
                contentDescription = null,
                tint = if (liked) Color(0xFF1877F2) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = likeCount.toString(),
                fontSize = 12.sp,
                color = if (liked) Color(0xFF1877F2) else Color.Gray
            )
        }

        Spacer(Modifier.width(16.dp))

        // Nút Dislike
        Icon(
            imageVector = if (disliked) Icons.Filled.ThumbDown else Icons.Default.ThumbDown,
            contentDescription = null,
            tint = if (disliked) Color(0xFF1877F2) else Color.Gray,
            modifier = Modifier
                .size(16.dp)
                .clickable { onDislikeToggle() }
        )

        Spacer(Modifier.width(20.dp)) // Khoảng cách phải
    }
}