package com.example.app.ui.feed.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.domain.model.Post
import com.example.app.domain.model.Profile
import com.example.app.utils.time.formatTimeAgoFromIso


// Thêm các import cần thiết
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold


// Avatar, username + createAt
@Composable
fun PostHeader(
    profile: Profile,
    post: Post,
    onMenuClick: () -> Unit = {},
    onAvatarClick: (String) -> Unit = {}
) {
    val fullNameColor = Color.White
    val iconColor = Color(0xFFBDBDBD)

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        // 1. Căn lề trên cho tất cả thành phần trong Row
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        AsyncImage(
            model = profile.avatarUrl ?: "https://i.pravatar.cc/300",
            contentDescription = "Avatar",
            modifier = Modifier
                .padding(top = 4.dp) // Thêm chút padding để cân với dòng chữ đầu tiên
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onAvatarClick(profile.id) }
        )

        Spacer(Modifier.width(8.dp))

        // Thông tin Profile
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp), // Căn chỉnh chữ khớp với đỉnh của Avatar
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = profile.fullName,
                fontWeight = SemiBold,
                color = fullNameColor,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onAvatarClick(profile.id) }
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = formatTimeAgoFromIso(post.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = iconColor
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Circle,
                    contentDescription = null,
                    modifier = Modifier.size(3.dp),
                    tint = iconColor
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = iconColor
                )
            }
        }

        // 2. Nút ba chấm
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.offset(y = (-9).dp, x = 9.dp)
            // offset y = -8.dp để khử khoảng trống mặc định của IconButton (48dp), giúp dấu 3 chấm lên cao hẳn
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "More options",
                tint = iconColor
            )
        }
    }
}