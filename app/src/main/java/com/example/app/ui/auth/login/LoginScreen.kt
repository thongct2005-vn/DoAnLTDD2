package com.example.app.ui.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.AppTextInput
import com.example.app.ui.auth.components.NoticeType
import com.example.app.ui.auth.components.PasswordInput

val bg = Color(0xFF0F1B21)
val sheet = Color(0xFF14232B)
val field = Color(0xFF0E1A20)
val textPrimary = Color(0xFFEAF2F6)
val textSecondary = Color(0xFFB7C7D1)
val blue = Color(0xFF1B74E4)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPassWordActivity: ()->Unit,
    successMessage: String? = null,
) {
    val user by viewModel.user.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showSuccessNotice by remember { mutableStateOf(successMessage != null) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(bg)) {
        // Pattern ở góc phải
        Image(
            painter = painterResource(R.drawable.bgw1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopEnd)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = sheet),
            elevation = CardDefaults.cardElevation(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                }

                Text("Đăng nhập", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Vui lòng nhập thông tin để đăng nhập.",
                    fontSize = 15.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )

                if (showSuccessNotice && successMessage != null) {
                    AppNotice(
                        text = successMessage,
                        type = NoticeType.SUCCESS,
                        onClose = { showSuccessNotice = false },
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                AppNotice(
                    text = uiState.errorMessage,
                    type = NoticeType.ERROR,
                    onClose = { viewModel.clearError() }, // Nhớ thêm hàm clearError trong ViewModel
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                AppTextInput(
                    value = user.email,
                    onValueChange = { viewModel.updateEmail(it)},
                    label = "Email",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                PasswordInput(
                    value = user.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = "Mật khẩu",
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = textPrimary
                        )
                    }
                )


                Spacer(modifier = Modifier.height(12.dp))
                Text("Quên mật khẩu?", color = blue, modifier = Modifier
                    .align(Alignment.End)
                    .clickable{onForgotPassWordActivity()})

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.login(
                        onSuccess = onLoginSuccess,
                    ) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    Text("Đăng nhập", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Chưa có tài khoản? Đăng ký", color = blue,
                    modifier = Modifier.clickable {
                        onRegisterClick()
                    })
            }
        }
    }
}
