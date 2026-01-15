package com.example.app.ui.auth.login

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.AuthRepository
import com.example.app.network.RetrofitClient
import com.example.app.network.api.AuthApiService
import com.example.app.network.dto.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
data class LoginUser(
    val email: String = "thongct2005@gmail.com",
    val password: String = "Thong@123"
)
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // ===================================================================
    // 1. Repository – tầng data, chịu trách nhiệm gọi API
    // ===================================================================
    private val authApi = RetrofitClient.create(AuthApiService::class.java)
    private val repository = AuthRepository(authApi)

    // ===================================================================
    // 2. State lưu thông tin người dùng đang nhập (email, password)
    // ===================================================================
    private val _user = MutableStateFlow(LoginUser())
    val user: StateFlow<LoginUser> = _user.asStateFlow()

    // ===================================================================
    // 3. State quản lý UI chung (loading, lỗi)
    // ===================================================================
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ===================================================================
    // 4. State kiểm soát hiển thị/mất mật khẩu
    // ===================================================================
    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    // ===================================================================
    // 5. SharedPreferences để lưu token và thông tin đăng nhập
    //    (dùng để duy trì phiên đăng nhập sau khi app bị kill hoặc khởi động lại)
    // ===================================================================
    private val prefs = application.getSharedPreferences("auth_pref", Context.MODE_PRIVATE)

    // ===================================================================
    // 6. Các hàm cập nhật thông tin người dùng (được gọi từ UI)
    // ===================================================================
    /**
     * Cập nhật email người dùng nhập
     */
    fun updateEmail(email: String) {
        _user.value = _user.value.copy(email = email)
        clearError()
    }

    /**
     * Cập nhật mật khẩu người dùng nhập
     */
    fun updatePassword(password: String) {
        _user.value = _user.value.copy(password = password)
        clearError()
    }

    fun clearError() {
        if (_uiState.value.errorMessage != null) {
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    // ===================================================================
    // 7. Hàm chính: Đăng nhập
    // ===================================================================
    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Bước 1: Kiểm tra input rỗng
            val email = _user.value.email.trim()
            val password = _user.value.password

            when {
                email.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Vui lòng nhập email"
                    )
                    return@launch
                }
                password.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Vui lòng nhập mật khẩu"
                    )
                    return@launch
                }
            }

            // Bước 2: Bắt đầu loading
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            // Bước 3: Gọi API login
            val result = repository.login(email, password)

            result.fold(
                onSuccess = { response ->
                    // Lưu token và thông tin vào AuthManager
                    AuthManager.saveAccessToken(response.accessToken)
                    AuthManager.saveRefreshToken(response.refreshToken)
                    AuthManager.setFirstLogin(response.isFirstLogin)

                    // Thành công → tắt loading và chuyển màn hình
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { error ->
                    // Thất bại → hiển thị lỗi
                    val errorMessage = error.message ?: "Đăng nhập thất bại. Vui lòng thử lại."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
            )
        }
    }

    // ===================================================================
    // 8. Các hàm tiện ích lấy thông tin đã lưu
    // ===================================================================
    /**
     * Lấy access token đã lưu (dùng cho các request cần auth sau này)
     */
    fun getAccessToken(): String? = prefs.getString("access_token", null)

    /**
     * Lấy refresh token đã lưu (dùng để refresh khi access token hết hạn)
     */
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    /**
     * Kiểm tra xem đây có phải lần đăng nhập đầu tiên không
     */
    fun isFirstLogin(): Boolean = prefs.getBoolean("is_first_login", false)

    /**
     * Xóa toàn bộ dữ liệu đăng nhập (dùng khi logout)
     */
    fun clearAuthData() {
        prefs.edit { clear() }
    }

    suspend fun refreshAccessToken(): Result<String> {
        val refreshToken = getRefreshToken()
            ?: return Result.failure(Exception("Không có refresh token – cần đăng nhập lại"))

        return repository.checkRefreshToken(refreshToken).fold(
            onSuccess = { newAccessToken ->
                // Lưu access_token mới vào SharedPreferences
                prefs.edit {
                    putString("access_token", newAccessToken)
                    apply()
                }
                Result.success(newAccessToken)
            },
            onFailure = { error ->
                // Refresh thất bại → token không hợp lệ → buộc logout
                clearAuthData()
                Result.failure(error)
            }
        )
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val refreshToken = AuthManager.getRefreshToken()
            if (refreshToken != null) {
                repository.logout(refreshToken)
            }
            AuthManager.clear()
            onSuccess()
        }
    }
}