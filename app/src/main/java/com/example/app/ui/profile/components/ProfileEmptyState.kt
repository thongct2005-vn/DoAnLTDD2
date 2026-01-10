package com.example.app.ui.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Sử dụng bảng màu đã thống nhất
@Composable

fun ProfileEmptyState() {

     val textMain = Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tiêu đề chính sử dụng textMain (Trắng)
        Text(
            text = "Tạo bài viết đầu tiên",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = textMain
        )

        // Mô tả sử dụng textMain với độ trong suốt (alpha) để làm mờ nhẹ
        Text(
            text = "Biến không gian này thành của riêng bạn.",
            textAlign = TextAlign.Center,
            color = textMain.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp, start = 32.dp, end = 32.dp)
        )

        // Nút hành động chính
        // Bạn có thể giữ màu Blue làm điểm nhấn (Primary)
        // Hoặc dùng textMain để làm nút nổi bật trên nền tối
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3897F0), // Giữ màu xanh để làm Call-to-action nổi bật
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Tạo",
                fontWeight = FontWeight.Bold
            )
        }
    }
}