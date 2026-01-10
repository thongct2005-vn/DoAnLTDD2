package com.example.app.ui.comment.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.domain.model.CommentItem


@Composable
fun CommentRow(
    item: CommentItem,
    replyCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onReplyClick: (CommentItem) -> Unit
) {
    val indent = (minOf(item.level, 1) * 45).dp
    val avatarSize = if (item is CommentItem.Reply) 25.dp else 36.dp

    var liked by remember { mutableStateOf(false) }
    var disliked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(item.comment.likeCount) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReplyClick(item) }
            .padding(start = indent, top = 20.dp, end = 10.dp)
    ) {

        // avatar
        Box(
            modifier = Modifier
                .padding(start = 20.dp)
                .size(avatarSize)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
        )

        Spacer(Modifier.width(10.dp))

        Column {
            UserNameHeader(item)

            Text(
                text = item.comment.content,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 3.dp)  // Đúng vị trí và cách dùng
            )

            item.comment.imageUri?.let { uri ->
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = uri,
                    contentDescription = "Comment imgage",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }

            InteractionRow(
                createdAt = item.comment.createdAt,
                onReplyClick = { onReplyClick(item) },
                liked = liked,
                disliked = disliked,
                likeCount = likeCount,
                onLikeToggle = {
                    if (liked) { liked = false; likeCount-- }
                    else { disliked = false; liked = true; likeCount++ }
                },
                onDislikeToggle = {
                    if (disliked) disliked = false
                    else { liked = false; likeCount--; disliked = true }
                }
            )

            if (item is CommentItem.Parent && replyCount > 0 && !isExpanded) {
                Row(
                    modifier = Modifier.padding(top = 8.dp).clickable { onToggleExpand() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Xem thêm $replyCount phản hồi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}