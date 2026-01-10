package com.example.app.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app.ui.notifications.model.AppNotification
import com.example.app.ui.notifications.model.NotificationType
import com.example.app.ui.notifications.model.buildTitle

// =====================
// COLORS (Dark tone)
// =====================
private val bg = Color(0xFF1F1F1F)
private val textMain = Color.White
private val primaryBlue = Color(0xFF3897F0)
private val grayButton = Color(0xFF3A3A3A)
private val borderColor = Color.White.copy(alpha = 0.5f)

@Composable
fun NotificationItem(
    item: AppNotification,
    onClick: () -> Unit
) {
    val cardBg =
        if (item.isRead) grayButton
        else primaryBlue.copy(alpha = 0.12f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {

            // Avatar / Icon
            Box(modifier = Modifier.size(44.dp)) {
                if (!item.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(grayButton),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (item.type) {
                                NotificationType.LIKE -> "♥"
                                NotificationType.COMMENT -> "💬"
                                NotificationType.FOLLOW -> "➕"
                                NotificationType.SHARE -> "↗"
                            },
                            color = textMain,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Unread dot
                if (!item.isRead) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(primaryBlue)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = item.buildTitle(),
                    color = textMain,
                    fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                if (item.type == NotificationType.COMMENT && item.commentPreview != null) {
                    CommentThreadPreview(
                        comment = item.commentPreview,
                        reply = item.replyPreview
                    )
                } else {
                    Text(
                        text = item.message,
                        color = textMain.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = item.timeText,
                    color = textMain.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Thumb
            if (!item.thumbUrl.isNullOrBlank()) {
                Spacer(Modifier.width(12.dp))
                AsyncImage(
                    model = item.thumbUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    }
}
