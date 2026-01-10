package com.example.app.ui.auth.forgot_password

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.app.ui.auth.forgot_password.components.ForgotEmailScreen
import com.example.app.ui.auth.forgot_password.components.ForgotNewPassWordScreen
import com.example.app.ui.auth.forgot_password.components.ForgotOtpScreen

class ForgotPassWordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var step by remember { mutableIntStateOf(0) }
            var email by remember { mutableStateOf("") }
            var otp by remember { mutableStateOf("") }

            when (step) {
                0 -> ForgotEmailScreen(
                    onBack = { finish() },
                    onNext = { enteredEmail ->
                        email = enteredEmail       // ✅ lưu email đúng biến
                        step = 1
                    }
                )

                1 -> ForgotOtpScreen( // ✅ truyền email qua OTP
                    onBack = { step = 0 },
                    onNext = { enteredOtp ->
                        otp = enteredOtp          // ✅ lưu otp (demo)
                        step = 2
                    },
                    onNoCode = { /* demo: chưa xử lý */ }
                )

                else -> ForgotNewPassWordScreen(
                    onBack = { step = 1 },
                    onDone = { newPass ->
                        // demo: chưa cần xử lý lưu mật khẩu
                        finish()
                    }
                )
            }
        }
    }
}
