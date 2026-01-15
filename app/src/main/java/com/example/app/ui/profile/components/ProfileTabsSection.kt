package com.example.app.ui.profile.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun ProfileTabsSection(
    isOwner: Boolean,
    selectedTab: Int,              // ← Nhận từ parent (ProfileScreen)
    onTabChange: (Int) -> Unit,
    bg: Color = Color(0xFF1F1F1F)
) {
    // Tabs: 0 = Bài viết (Grid), 1 = Đã lưu (Bookmark) - chỉ owner
    val tabs = remember(isOwner) {
        if (isOwner) {
            listOf(
                TabItem(Icons.Default.GridOn, "Bài viết"),
                TabItem(Icons.Default.BookmarkBorder, "Đã lưu")
            )
        } else {
            listOf(
                TabItem(Icons.Default.GridOn, "Bài viết")
            )
        }
    }

    // Tự reset về tab 0 nếu chuyển profile (ví dụ từ owner sang non-owner)
    LaunchedEffect(isOwner) {
        if (selectedTab >= tabs.size) {
            onTabChange(0)
        }
    }

    TabRow(
        selectedTabIndex = selectedTab.coerceIn(0, tabs.lastIndex), // an toàn nếu selectedTab ngoài range
        containerColor = bg,
        contentColor = Color.White,
        indicator = {},     // không thanh dưới
        divider = {}        // không line phân cách
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabChange(index) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (selectedTab == index) Color.White else Color.Gray
                    )
                },
                text = {
                    Text(
                        text = tab.label,
                        color = if (selectedTab == index) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    }
}

private data class TabItem(
    val icon: ImageVector,
    val label: String
)