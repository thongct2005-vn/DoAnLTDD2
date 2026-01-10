package com.example.app.ui.auth.forgot_password


import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.AuthRepository
import com.example.app.network.RetrofitClient
import com.example.app.network.api.AuthApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch

// State để quản lý UI cho luồng Quên mật khẩu
data class ForgotUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOtpResent: Boolean = false
)

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    // Khởi tạo Repository (giống bên Register)
    private val apiAuth = RetrofitClient.create(AuthApiService::class.java)
    private val repository: AuthRepository = AuthRepository(apiAuth)

    // State quản lý email và OTP
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp

    // State quản lý UI (loading, error)
    private val _uiState = MutableStateFlow(ForgotUiState())
    val uiState: StateFlow<ForgotUiState> = _uiState

    // State đếm ngược Cooldown
    private val _noCodeCooldown = MutableStateFlow(0)
    val noCodeCooldown: StateFlow<Int> = _noCodeCooldown

    private val prefs = application.getSharedPreferences("forgot_otp_pref", Context.MODE_PRIVATE)
    private val cooldownSeconds = 30

    init {
        // Khôi phục cooldown nếu app bị tắt/mở lại
        val lastTime = prefs.getLong("last_forgot_no_code_time", 0L)
        if (lastTime != 0L) {
            val diff = (System.currentTimeMillis() - lastTime) / 1000
            val remain = cooldownSeconds - diff.toInt()
            if (remain > 0) {
                startCooldown(remain)
            }
        }
    }

    fun updateEmail(newEmail: String) { _email.value = newEmail }
    fun updateOtp(newOtp: String) { _otp.value = newOtp }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Logic xử lý khi bấm "Tôi không nhận được mã"
    fun onNoCodeClick() {
        if (_noCodeCooldown.value > 0) return // Chống bấm nhiều lần

        _noCodeCooldown.value = cooldownSeconds
        prefs.edit { putLong("last_forgot_no_code_time", System.currentTimeMillis()) }
        startCooldown(cooldownSeconds)

        // Gọi API gửi lại mã cho trường hợp Quên mật khẩu
        resendForgotOtp()
    }

    private fun startCooldown(seconds: Int) {
        viewModelScope.launch {
            (seconds downTo 0).asFlow().collect {
                _noCodeCooldown.value = it
                delay(1000)
            }
        }
    }

    private fun resendForgotOtp() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val currentEmail = _email.value
            if (currentEmail.isBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Email không hợp lệ")
                return@launch
            }

            // Lưu ý: Tùy vào API của bạn, thường quên mật khẩu chỉ cần Email
            // Nếu API cần UserId, bạn phải lưu UserId từ bước check email trước đó
//            val result = repository.forgotPassword(currentEmail) // Giả định hàm trong Repo
//
//            _uiState.value = if (result.isSuccess) {
//                _uiState.value.copy(isLoading = false, isOtpResent = true)
//            } else {
//                _uiState.value.copy(
//                    isLoading = false,
//                    errorMessage = result.exceptionOrNull()?.message ?: "Gửi lại mã thất bại"
//                )
//            }
        }
    }
}