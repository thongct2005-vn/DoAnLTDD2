package com.example.app.ui.profile.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Portrait
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset // IMPORT QUAN TRỌNG NHẤT
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfileTabsSection() {
    // Sử dụng mutableIntStateOf để tối ưu hiệu năng cho biến kiểu Int
    var selectedTabIndex by remember { mutableIntStateOf(0) }
     val bg = Color(0xFF1F1F1F)

    // Danh sách các Icon hiển thị trên Tab
    val tabs = listOf(
        Icons.Default.GridOn,      // icon bài viết

        Icons.Default.FavoriteBorder     // icon bài viết đã lưu
    )

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = bg,
        contentColor = Color.White,
        // vạch đen dưới tab
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    // Đây là nơi sử dụng tabIndicatorOffset đã import ở trên
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color.White
                )
            }
        },
        divider = {
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
        }
    ) {
        tabs.forEachIndexed { index, icon ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        // Đổi màu icon dựa trên trạng thái chọn
                        tint = if (selectedTabIndex == index) Color.White else Color.Gray
                    )
                }
            )
        }
    }
}