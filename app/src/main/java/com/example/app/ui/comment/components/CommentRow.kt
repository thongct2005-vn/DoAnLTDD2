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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.domain.model.Comment
import com.example.app.utils.time.formatTimeAgo
import com.example.myapplication.domain.model.CommentItem


@Composable
fun CommentRow(
    item: CommentItem,
    replyCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onReplyClick: (CommentItem) -> Unit,
    onProfileClick: (String) -> Unit,
    isReply: Boolean = false,
    onLikeClick: (String) -> Unit,
    onDeleteClick:(Comment) -> Unit
) {
    val textSub = Color(0xFFBDBDBD)
    // Màu nền cho menu popup (xám đậm)
    val menuBg = Color(0xFF333333)

    val isReplyActual = item is CommentItem.Reply
    val avatarSize = if (isReplyActual) 28.dp else 36.dp
    val startPadding = if (isReplyActual) 56.dp else 16.dp

    // State để quản lý việc ẩn/hiện menu của dòng này
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding, top = 12.dp, end = 16.dp, bottom = 8.dp)
    ) {
        // Avatar
        AsyncImage(
            model = item.comment.user.avatar?.ifEmpty { "https://example.com/default-avatar.jpg" },
            contentDescription = "Avatar",
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .clickable { onProfileClick(item.comment.user.id) },
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        // Nội dung
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    UserNameHeader(item, onUsernameClick = onProfileClick)
                }

                Box(modifier = Modifier.offset(x = (-32).dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Tùy chọn",
                        tint = textSub,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(start = 8.dp)
                            .clickable { showMenu = true } // Bấm vào để mở menu
                    )

                    // Menu xổ xuống
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = menuBg, // Màu nền menu
                        offset = DpOffset(x = 0.dp, y = 8.dp), // Dịch xuống một chút
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        // Kiểm tra quyền sở hữu để hiện nút Xóa hoặc Báo cáo
                        // Giả sử item.comment.isOwner là boolean
                        if (item.comment.isOwner) {
                            DropdownMenuItem(
                                text = {
                                    Text("Xóa bình luận", color = Color(0xFFFF3B30), fontSize = 14.sp)
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick(item.comment)
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = {
                                    Text("Báo cáo", color = Color.White, fontSize = 14.sp)
                                },
                                onClick = {
                                    showMenu = false

                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = item.comment.content,
                color = Color.White,
                fontSize = 14.sp,
            )


            InteractionRow(
                rawTimestamp = item.comment.createdAt,
                onReplyClick = { onReplyClick(item) },
                likeCount = item.comment.likeCount,
                onLikeClick = { onLikeClick(item.comment.id) },
                isLiked = item.comment.isLiked
            )

            if (item is CommentItem.Parent && replyCount > 0 && !isExpanded) {
                Row(
                    modifier = Modifier.padding(top = 8.dp).clickable { onToggleExpand() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(20.dp).height(1.dp).background(textSub.copy(alpha = 0.4f)))
                    Spacer(Modifier.width(8.dp))
                    Text("Xem thêm $replyCount phản hồi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSub)
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = textSub, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}