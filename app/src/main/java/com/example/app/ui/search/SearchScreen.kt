package com.example.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.network.dto.search.SearchUserResponse
import com.example.app.network.dto.search.relationLabel
import com.example.app.ui.feed.post.PostItem

private val FB_Gray_Bg = Color(0xFF18191A)
private val FB_Search_Bar = Color(0xFF3A3B3C)

enum class SearchTab(val title: String) {
    ALL("Tất cả"),
    USERS("Người dùng"),
    POSTS("Bài viết"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(SearchTab.ALL) }
    var isSubmitted by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FB_Gray_Bg,
        topBar = {
            Column(modifier = Modifier.background(FB_Gray_Bg).statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = Color.White)
                    }

                    TextField(
                        value = query,
                        onValueChange = {
                            query = it
                            isSubmitted = false
                            viewModel.onQueryChange(it)
                        },
                        placeholder = { Text("Tìm kiếm", color = Color(0xFFB0B3B8), fontSize = 15.sp) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFB0B3B8)) },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                isSubmitted = true
                                viewModel.onSubmitSearch()
                            }
                        ),
                        shape = RoundedCornerShape(25.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = FB_Search_Bar,
                            unfocusedContainerColor = FB_Search_Bar,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .padding(end = 12.dp)
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                if (isSubmitted) {
                    SecondaryTabRow(
                        selectedTabIndex = SearchTab.entries.indexOf(selectedTab),
                        containerColor = FB_Gray_Bg,
                        indicator = {},
                        divider = {}
                    ) {
                        SearchTab.entries.forEach { tab ->
                            val selected = selectedTab == tab

                            Tab(
                                selected = selected,
                                onClick = { selectedTab = tab },
                                modifier = Modifier
                                    .height(50.dp)
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (selected) Color(0xFF2D88FF)
                                        else Color.Transparent
                                    ),
                                text = {
                                    Text(
                                        text = tab.title,
                                        color = if (selected) Color.White else Color.Gray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }

                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Column(Modifier.padding(16.dp)) {
                        Text("Tìm kiếm gần đây", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                is SearchUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2D88FF)
                    )
                }
                is SearchUiState.Error -> {
                    Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                is SearchUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        // ===== CHƯA NHẤN ENTER → GỢI Ý USER =====
                        if (!isSubmitted) {
                            items(state.users) { user ->
                                UserSearchItem(
                                    user = user,
                                    onClick = { onProfileClick(user.id) }
                                )
                            }
                        }

                        // ===== ĐÃ NHẤN ENTER → THEO TAB =====
                        if (isSubmitted) {

                            // TAB: USERS hoặc ALL
                            if (selectedTab == SearchTab.USERS || selectedTab == SearchTab.ALL) {
                                items(state.users) { user ->
                                    UserSearchItem(
                                        user = user,
                                        onClick = { onProfileClick(user.id) }
                                    )
                                }
                            }

                            // TAB: POSTS hoặc ALL
                            if (selectedTab == SearchTab.POSTS || selectedTab == SearchTab.ALL) {
                                items(state.posts) { post ->
                                    PostItem(
                                        post = post,
                                        onProfileClick = onProfileClick,
                                        onLikeClick = { /* TODO */ },
                                        onCommentClick = { /* TODO */ },
                                        onShareClick = { /* TODO */ }
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun     UserSearchItem(user: SearchUserResponse, onClick: () -> Unit) {
    val label = user.relationLabel()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF3E4042)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.username.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() }
        ) {
            Text(
                text = user.username,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            )
            if (label != null) {
                Text(
                    text = label,
                    color = Color(0xFFB0B3B8),
                    fontSize = 13.sp
                )
            }
        }
    }
}