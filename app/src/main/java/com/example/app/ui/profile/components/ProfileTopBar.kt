package com.example.app.ui.profile.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.app.ui.auth.components.BackIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    onBack: () -> Unit
) {
    TopAppBar(
        title = { /* Để trống tiêu đề */ },
        navigationIcon = {
            BackIconButton(onClick = onBack)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1F1F1F), // Màu nền tối của bạn
            navigationIconContentColor = Color.White
        )
    )
}