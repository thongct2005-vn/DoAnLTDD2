package com.example.app.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app.ui.notifications.model.CommentSnippet

// =====================
// COLORS
// =====================
private val bg = Color(0xFF1F1F1F)
private val textMain = Color.White
private val primaryBlue = Color(0xFF3897F0)
private val grayButton = Color(0xFF3A3A3A)
private val borderColor = Color.White.copy(alpha = 0.5f)

@Composable
fun CommentThreadPreview(
    comment: CommentSnippet,
    reply: CommentSnippet?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)

    ) {
        // Bubble comment gốc
        Bubble(
            author = comment.authorName,
            text = comment.text
        )

        // Bubble reply (giữ nguyên layout)
        if (reply != null) {
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(18.dp))
                Bubble(
                    author = reply.authorName,
                    text = reply.text
                )
            }
        }
    }
}

@Composable
private fun Bubble(author: String, text: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(grayButton) // đổi màu bubble
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = author,
            color = primaryBlue, // đổi màu tên
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = text,
            color = textMain, // đổi màu nội dung
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
