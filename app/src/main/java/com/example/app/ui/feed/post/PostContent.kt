package com.example.app.ui.feed.post

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun PostContent(
    content: String,
) {
    val contentColor = Color.White.copy(alpha = 0.9f)
    if (content.isNotEmpty()) {
        Text(
            text = content,
            color = contentColor,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(start = 15.dp, end = 15.dp)
        )
    }
}