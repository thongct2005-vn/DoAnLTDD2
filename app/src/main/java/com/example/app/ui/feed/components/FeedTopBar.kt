package com.example.app.ui.feed.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.R


// Logo + button createPost, buttonSearch, buttonLogout

// Tabs Home, Friends, Dating, Notification
@Composable
fun FeedTopBar(
    onCreatePostClick: () -> Unit,
    onSearchClick: () -> Unit,
    onClickLogout: () -> Unit,
    selectedTabIndex: Int,
    onTabClick: (Int) -> Unit,
) {
    val bg = Color(0xFF1F1F1F)
    Column(modifier = Modifier.background(bg).statusBarsPadding()) {
        AnimatedVisibility(
            visible = selectedTabIndex == 0,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            // Phần logo + 3 nút bấm
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val whiteGradient = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFE0E0E0),
                    Color(0xFFBDBDBD)
                )
                Text(
                    text = "undisc.",
                    style = TextStyle(
                        brush = Brush.linearGradient(colors = whiteGradient),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    HeaderIconButton(ImageVector.vectorResource(R.drawable.ic_add), onClick = onCreatePostClick)
                    HeaderIconButton(ImageVector.vectorResource( R.drawable.ic_search), onClick = onSearchClick)
                    HeaderIconButton(Icons.AutoMirrored.Filled.Logout, onClick = onClickLogout)
                }
            }
        }

        /**--------------------------------------------------------------*/
        Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
            TabIcon(
                icon = Icons.Default.Home,
                isSelected = selectedTabIndex == 0,
                modifier = Modifier.weight(1f),
                onClick = { onTabClick(0) }
            )
            TabIcon(
                icon = Icons.Default.People,
                isSelected = selectedTabIndex == 1,
                modifier = Modifier.weight(1f),
                onClick = { onTabClick(1) }
            )
            TabIcon(
                icon = Icons.Default.Favorite,
                isSelected = selectedTabIndex == 2,
                modifier = Modifier.weight(1f),
                onClick = { onTabClick(2) }
            )
            TabIcon(
                icon = Icons.Default.Notifications,
                isSelected = selectedTabIndex == 3,
                modifier = Modifier.weight(1f),
                onClick = { onTabClick(3) }
            )
        }

        // Đường kẻ mảnh dưới khi chọn 1 tag
        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}
@Composable
fun HeaderIconButton(icon: ImageVector,
                     onClick: ()-> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(38.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = Color.White
        )
    }
}
