package com.example.app.ui.feed.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.domain.model.Media
import com.example.app.domain.model.Post
import com.example.app.domain.model.Profile
import com.example.app.ui.feed.post.media.MediaGrid
import com.example.app.utils.count.formatCount

// Tất cả thành phần của một bài post
@Composable
fun PostItem(
    mediaList: List<Media>?,
    post: Post,
    profile: Profile,
    onProfileClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick:(String)->Unit
) {
    val bg = Color(0xFF1F1F1F)
    val iconDefaultColor = Color(0xFFBDBDBD)
    val iconFilledHeartColor = Color(0xFFD81B60)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding( bottom = 15.dp)
    ) {
        // Header: Avatar + Username + Time + Public
        PostHeader(
            profile = profile,
            post = post,
            onAvatarClick = { if(profile.id != post.user.id)onProfileClick(profile.id) },
            onMenuClick = {}

        )
        // --- TÁI SỬ DỤNG CONTENT ---
        PostContent(content = post.content)



        if (post.sharedPost != null) {
            Spacer(Modifier.height(5.dp))
            SharedPostCard(sharedPost = post.sharedPost,
                onProfileClick = { onProfileClick (post.sharedPost.user.id)})
        }
        else {
            Spacer(Modifier.height(5.dp))
            MediaGrid(
                mediaList = mediaList,
                post = post,
                profile = profile
            )
        }



        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bên trái: Like, Comment, Share
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(75.dp)) {
                    ActionButton(
                        icon = if (post.isLiked) ImageVector.vectorResource(R.drawable.ic_filled_heart)
                        else ImageVector.vectorResource(R.drawable.ic_rounded_heart),
                        text = if (post.likeCount > 0) formatCount(post.likeCount) else "",
                        tint = if (post.isLiked) iconFilledHeartColor else iconDefaultColor,
                        onClick = { onLikeClick(post.id) }
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(modifier = Modifier.width(75.dp)) {
                    ActionButton(
                        icon = ImageVector.vectorResource(R.drawable.ic_chat),
                        text = if (post.commentCount > 0) formatCount(post.commentCount) else "",
                        tint = iconDefaultColor,
                        iconSize = 20,
                        onClick = { onCommentClick(post.id) }
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Nhóm Share
                if (!profile.isOwner || post.sharedPost == null) {
                    Box(modifier = Modifier.width(50.dp)) { // Share thường không có text nên cần ít hơn
                        ActionButton(
                            icon = ImageVector.vectorResource(id = R.drawable.ic_share),
                            text = "",
                            tint = iconDefaultColor,
                            onClick = {onShareClick(post.id)}
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.width(75.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                ActionButton(
                    icon = ImageVector.vectorResource(R.drawable.ic_save),
                    text = "",
                    tint = iconDefaultColor
                )
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = Color.Black)
    }

}