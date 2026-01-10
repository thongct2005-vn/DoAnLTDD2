package com.example.app.ui.comment.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.CommentItem

@Composable
fun UserNameHeader(item: CommentItem) {
    if (item is CommentItem.Parent) {
        Text(
            text = item.comment.userName,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .clickable { /* Click vào tên để xem profile nếu cần */ }
        )
    } else if (item is CommentItem.Reply) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.comment.userName,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            if (item.level >= 2) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = item.replyToUserName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}