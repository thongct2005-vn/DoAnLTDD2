package com.example.app.ui.auth.forgot_password

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.ForgotPasswordRepository
import com.example.app.network.RetrofitClient
import com.example.app.network.api.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authApi = RetrofitClient.create(AuthApiService::class.java)
    private val repo = ForgotPasswordRepository(authApi)

    private val _uiState =
        MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun clearState() {
        _uiState.value = ForgotPasswordUiState.Idle
    }

    /* ===================== SEND OTP ===================== */

    fun sendOtp(email: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading

            repo.sendOtp(email)
                .onSuccess {
                    // 🔥 DÒNG QUYẾT ĐỊNH CHUYỂN MÀN
                    _uiState.value = ForgotPasswordUiState.OtpSent
                }
                .onFailure { throwable ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        mapThrowableToMessage(throwable)
                    )
                }
        }
    }

    /* ===================== VERIFY OTP ===================== */

    fun verifyOtp(inputOtp: String) {
        _uiState.value = ForgotPasswordUiState.Loading

        val isValid = repo.verifyOtp(inputOtp)

        _uiState.value = if (isValid) {
            ForgotPasswordUiState.OtpVerified
        } else {
            ForgotPasswordUiState.Error("Mã OTP không đúng. Vui lòng kiểm tra lại.")
        }
    }

    /* ===================== RESET PASSWORD ===================== */

    fun resetPassword(newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading

            repo.resetPassword(newPassword, confirmPassword)
                .onSuccess {
                    _uiState.value = ForgotPasswordUiState.PasswordResetSuccess
                }
                .onFailure { throwable ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        mapThrowableToMessage(throwable)
                    )
                }
        }
    }

    /* ===================== ERROR MAPPER ===================== */

    private fun mapThrowableToMessage(throwable: Throwable): String {
        val message = throwable.message ?: ""

        return when {
            message.contains("404") ->
                "Email này chưa được đăng ký trong hệ thống."

            message.contains("401") ->
                "Mã xác thực không chính xác hoặc đã hết hạn."

            message.contains("500") ->
                "Hệ thống đang bảo trì, vui lòng thử lại sau."

            message.contains("Unable to resolve host") ->
                "Không có kết nối internet."

            else ->
                "Đã có lỗi xảy ra. Vui lòng thử lại."
        }
    }
}
