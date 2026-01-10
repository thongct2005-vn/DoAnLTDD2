package com.example.app.ui.follow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.TabDefaults
import coil.compose.AsyncImage
import com.example.app.domain.model.FollowUser
import com.example.app.ui.auth.components.authTextFieldColors

private val BackgroundDark = Color(0xFF1F1F1F)
private val SurfaceDark = Color(0xFF2A2A2A)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFBDBDBD)
private val TextHint = Color(0xFF757575)
private val BorderColor = Color(0xFF444444)

@Composable
fun FollowersFollowingScreen(
    userId: String,
    initialTab: Int,
    onBack: () -> Unit,
    onProfileClick:(String)->Unit,
    viewModel: FollowerFollowingViewModel

) {
    var tab by remember { mutableIntStateOf(initialTab) }
    var keyword by remember { mutableStateOf("") }

    val items by viewModel.items.collectAsState()

    val filtered = remember(keyword, items) {
        if (keyword.isBlank()) items
        else items.filter {
            it.username.contains(keyword, ignoreCase = true) ||
                    it.fullName.contains(keyword, ignoreCase = true)
        }
    }

    LaunchedEffect(tab) {
        viewModel.loadData(userId, isFollowers = (tab == 0), isRefresh = true)
    }

    Column(Modifier.fillMaxSize().background(BackgroundDark)) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Tài khoản",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextPrimary
            )
        }

        // TabRow - Dark style
        TabRow(
            selectedTabIndex = tab,
            containerColor = BackgroundDark,
            contentColor = TextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tab]),
                    color = Color(0xFFFFFFFF),
                    height = 3.dp
                )
            },
        ) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text("Người theo dõi", color = if (tab == 0) TextPrimary else TextSecondary) }
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text("Đang theo dõi", color = if (tab == 1) TextPrimary else TextSecondary) }
            )
        }

        // Search field - Dark mode
        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Tìm kiếm", color = TextHint) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = followTextFieldColors()
        )

        // Danh sách
        if (filtered.isEmpty() && !viewModel.isLoading) {
            EmptyFollowState(isFollowers = (tab == 0))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(filtered, key = { _, u -> u.id }) { index, user ->
                    // Load more khi cuộn gần cuối
                    if (index >= filtered.size - 1 && viewModel.nextCursor != null && !viewModel.isLoading) {
                        viewModel.loadData(userId, isFollowers = (tab == 0), isRefresh = false)
                    }

                    FollowRow(
                        user = user,
                        isOwner = viewModel.isOwner,
                        isFollowingTab = (tab == 1),
                        onProfileClick = { clickedId ->
                            onProfileClick(clickedId)
                        }
                    )
                }

                // Loading footer
                if (viewModel.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = TextPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FollowRow(
    user: FollowUser,
    isOwner: Boolean,
    isFollowingTab: Boolean,
    onProfileClick:(String)->Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.avatarUrl ?: "https://i.pravatar.cc/150",
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFF333333)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)
            .clickable(onClick = {onProfileClick(user.id)})
        ) {
            Text(
                text = user.username,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 15.sp
            )
            Text(
                text = user.fullName,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

    }
}

@Composable
fun EmptyFollowState(isFollowers: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .border(2.dp, BorderColor, CircleShape)
                .background(Color(0xFF2A2A2A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFollowers) Icons.Default.Person else Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isFollowers) "Chưa có người theo dõi" else "Chưa theo dõi ai",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isFollowers)
                "Khi có người theo dõi, họ sẽ xuất hiện tại đây."
            else
                "Khi bạn theo dõi mọi người, họ sẽ xuất hiện tại đây.",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
@Composable
fun followTextFieldColors() = OutlinedTextFieldDefaults.colors(
    // 1. NỀN (Container)
    focusedContainerColor = Color(0xFF2A2A2A),
    unfocusedContainerColor = Color(0xFF1F1F1F),

    // 2. VIỀN (Đổi từ Indicator thành Border)
    focusedBorderColor = Color(0xFF3897F0),      // Màu xanh bạn muốn tái sử dụng
    unfocusedBorderColor = Color(0xFF444444),
    errorBorderColor = Color(0xFFFF6B6B),

    // 3. CHỮ (Text)
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,

    // 4. CON TRỎ (Cursor)
    cursorColor = Color(0xFF3897F0),

    // 5. NHÃN & GỢI Ý (Label & Placeholder)
    focusedPlaceholderColor = Color(0xFF757575),
    unfocusedPlaceholderColor = Color(0xFF757575),
    focusedLabelColor = Color(0xFF3897F0),
    unfocusedLabelColor = Color(0xFFBDBDBD),

    // 6. BIỂU TƯỢNG (Icons)
    focusedLeadingIconColor = Color.White,
    unfocusedLeadingIconColor = Color(0xFFBDBDBD),
    focusedTrailingIconColor = Color.White,
    unfocusedTrailingIconColor = Color(0xFFBDBDBD)
)