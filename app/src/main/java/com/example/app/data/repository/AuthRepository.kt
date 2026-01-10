package com.example.app.data.repository

import com.example.app.network.api.AuthApiService
import com.example.app.network.dto.auth.request.*
import com.example.app.network.dto.auth.response.AuthApiResponse
import com.example.app.network.dto.auth.response.LoginResponse
import com.example.app.network.dto.auth.response.RegisterResponse
import com.google.gson.Gson
import retrofit2.Response
    /**Tầng Data (Repository): Chỉ quan tâm đến việc lấy dữ liệu ở đâu và lưu dữ liệu
     * thế nào. Nó trả lời câu hỏi: "Làm sao để gửi nội dung này lên Server?"
     * hay "Làm sao để lưu nó vào Database?".*/
class AuthRepository(
    private val api: AuthApiService
) {
    private val gson = Gson()
    private fun getErrorMessage(response: Response<*>): String {
        return try {
            val errorJson = response.errorBody()?.string()
            val errorResponse = gson.fromJson(errorJson, AuthApiResponse::class.java)
            errorResponse?.message ?: "Lỗi xác thực từ hệ thống"
        } catch (e: Exception) {
            "Đã có lỗi xảy ra (${response.code()})"
        }
    }


    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val res = api.login(LoginRequest(email, password))

            // TRƯỜNG HỢP 1: Status 2xx
            if (res.isSuccessful) {
                val body = res.body()
                if (body?.success == true) {
                    Result.success(LoginResponse(
                        access_token = body.access_token ?: "",
                        refresh_token = body.refresh_token ?: "",
                        isFirstLogin = body.isFirstLogin ?: false
                    ))
                } else {
                    // Backend đôi khi trả 200 nhưng success: false (như hàm logout/refresh của bạn)
                    Result.failure(Exception(body?.message ?: "Thông tin không chính xác"))
                }
            } else {
                // TRƯỜNG HỢP 2: Status 400, 401, 404... (Email không tồn tại nằm ở đây)
                val errorMsg = getErrorMessage(res)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ"))
        }
    }



    // --- 2. REGISTER ---
    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            val res = api.register(request)
            if (res.isSuccessful && res.body()?.success == true) {
                val body = res.body()!!
                Result.success(RegisterResponse(
                    user_id = body.user_id ?: "",
                    email = body.email ?: ""
                ))
            } else {
                Result.failure(Exception(getErrorMessage(res)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- 3. CHECK REFRESH TOKEN ---
    suspend fun checkRefreshToken(refreshToken: String): Result<String> {
        return try {
            val res = api.checkRefreshToken(RefreshTokenRequest(refreshToken))
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(res.body()?.access_token ?: "")
            } else {
                Result.failure(Exception(getErrorMessage(res)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- 4. SEND OTP ---
    suspend fun sendOtp(userId: String, email: String): Result<Unit> {
        return try {
            val res = api.sendOtp(SendOtpRequest(userId, email))
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessage(res)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- 5. VERIFY OTP ---
    suspend fun verifyOtp(userId: String, email: String, otp: String): Result<Unit> {
        return try {
            val res = api.verifyOtp(VerifyOtpRequest(userId, email, otp))
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessage(res)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- 6. LOGOUT & REVOKE (Giữ nguyên vì bạn muốn xử lý âm thầm) ---
    suspend fun logout(refreshToken: String): Result<Unit> {
        return try {
            val res = api.logout(LogoutRequest(refreshToken))

            Result.success(Unit)
        } catch (e: Exception) { Result.success(Unit) }
    }

    suspend fun checkEmail(email: String): Result<Boolean> {
        return try {
            val res = api.checkEmail(CheckEmailRequest(email = email.trim()))

            if (res.isSuccessful && res.body()?.success == true) {
                // Backend trả "Email hợp lệ" → chưa tồn tại hoặc chưa verify → false
                // Backend trả "Email đã tồn tại" → đã verify → true
                val message = res.body()?.message ?: ""
                val isExists = message.contains("tồn tại", ignoreCase = true)
                Result.success(isExists)
            } else {
                val errorMsg = getErrorMessage(res)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        suspend fun updateUsername(token: String, username: String): Result<Unit> {
            return try {
                // 1. Thực hiện gọi API
                val res = api.updateProfile(
                    token = "Bearer $token",
                    request = UpdateProfileRequest(username = username)
                )

                // 2. Xử lý kết quả trả về
                if (res.isSuccessful) {
                    val body = res.body()
                    if (body?.success == true) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(body?.message ?: "Cập nhật thất bại"))
                    }
                } else {
                    // Sử dụng hàm getErrorMessage có sẵn trong Repository của bạn
                    val errorMsg = getErrorMessage(res)
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Lỗi kết nối máy chủ: ${e.message}"))
            }
        }

}