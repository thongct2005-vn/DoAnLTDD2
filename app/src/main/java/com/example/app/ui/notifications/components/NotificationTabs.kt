package com.example.app.ui.notifications.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// =====================
// COLORS (Dark tone)
// =====================
private val bg = Color(0xFF1F1F1F)
private val textMain = Color.White
private val white = Color(0xFFFFFFFF)
private val grayButton = Color(0xFF3A3A3A)
private val borderColor = Color.White.copy(alpha = 0.5f)

@Composable
fun NotificationTabs(
    tabIndex: Int,
    onChange: (Int) -> Unit,
    unreadCount: Int
) {
    val tabs = listOf("Tất cả", "Chưa đọc")

    TabRow(
        selectedTabIndex = tabIndex,
        containerColor = bg,
        contentColor = textMain,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                color = white
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = tabIndex == index,
                onClick = { onChange(index) },
                selectedContentColor = textMain,
                unselectedContentColor = textMain.copy(alpha = 0.6f),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title)
                        if (index == 1 && unreadCount > 0) {
                            Spacer(Modifier.width(6.dp))
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = unreadCount.toString(),
                                        color = textMain
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = white
                                )
                            )
                        }
                    }
                }
            )
        }
    }
}
