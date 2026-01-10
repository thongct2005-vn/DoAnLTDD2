package com.example.app.data.repository
import com.example.app.domain.model.Post
import com.example.app.domain.model.Profile
import com.example.app.domain.model.ProfilePost
import com.example.app.network.RetrofitClient
import com.example.app.network.api.ProfileApiService
import com.example.app.network.dto.follow.response.FollowResponse
import com.example.app.network.dto.profile.response.ProfilePostResponse
import com.example.app.network.dto.profile.response.UpdateProfileRequest
import com.example.app.utils.mapper.toDomain
import retrofit2.Response


class ProfileRepository {
    private val api = RetrofitClient.create(ProfileApiService::class.java)

    suspend fun getProfile(userId: String): Result<Pair<Profile, List<Post>>> {
        return try {
            val response = api.getProfile(userId)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val profileDomain = body.profile.toDomain()
                    val postsDomain = body.posts.map { it.toDomain() }  // ← Map ở đây
                    Result.success(profileDomain to postsDomain)
                } ?: Result.failure(Exception("Dữ liệu trả về rỗng"))
            } else {
                Result.failure(Exception("Lỗi ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMoreProfilePosts(
        userId: String = "me",
        limit: Int = 5,
        cursor: String? = null
    ): Result<ProfilePost> {
        return try {
            val response: Response<ProfilePostResponse> = api.getProfilePosts(
                userId = userId,
                limit = limit,
                cursor = cursor
            )

            if (response.isSuccessful) {
                response.body()?.let { wrapper ->
                    if (wrapper.success) {
                        val postsDomain = wrapper.posts.map { it.toDomain() }
                        Result.success(
                            ProfilePost(
                                posts = postsDomain,
                                nextCursor = wrapper.nextCursor  // ← Lấy next_cursor từ response
                            )
                        )
                    } else {
                        Result.failure(Exception("API trả về success = false"))
                    }
                } ?: Result.failure(Exception("Dữ liệu rỗng từ server"))
            } else {
                Result.failure(Exception("Lỗi mạng: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // com.example.app.data.repository.ProfileRepository.kt


        // ... các phương thức cũ (getProfile, getMoreProfilePosts) ...

        /**
         * Thực hiện theo dõi người dùng
         */
        suspend fun followUser(userId: String): Result<Unit> {
            return try {
                val response = api.followUser(userId)
                // Nếu thành công (2xx) HOẶC đã follow rồi (409), ta đều coi là thành công
                if (response.isSuccessful || response.code() == 409) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Lỗi: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun unfollowUser(userId: String): Result<Unit> {
        return try {
            val response = api.unFollowUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Lỗi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateProfile(
        username: String?,
        fullName: String?,
        gender: String?,
        avatar: String?,
        address: String?,
        phone: String?
    ): Result<Unit> {
        return try {
            val response = api.updateProfile(
                UpdateProfileRequest(
                    username = username,
                    fullName = fullName, // sẽ gửi thành full_name nhờ @SerializedName
                    gender = gender,
                    avatar = avatar,
                    address = address,
                    phone = phone
                )
            )

            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Lỗi: ${response.code()} - ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
