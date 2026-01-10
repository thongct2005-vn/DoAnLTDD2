package com.example.app.domain.usecase
/**Tầng Domain (UseCase): Quan tâm đến quy tắc nghiệp vụ.
 *  Nó trả lời câu hỏi:"Người dùng đang muốn làm gì?".
 * Ví dụ: Trước khi tạo bài viết, bạn cần kiểm tra nội dung có từ cấm không,
 * định dạng lại chuỗi privacy, hoặc kết hợp dữ liệu từ 2-3 Repository khác nhau.
 * Những logic này thuộc về "Nghiệp vụ", không phải "Dữ liệu".*/

import com.example.app.data.repository.PostRepository
import com.example.app.domain.model.Media
import com.example.app.ui.feed.create.PostPrivacy

class CreatePostUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        content: String,
        privacy: PostPrivacy,
        mediaItems: List<Media>
    ): Result<Unit> {
        return try {
            val response = postRepository.createPost(
                content = content,
                privacy = privacy.apiValue.lowercase(),
                mediaItems = mediaItems
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}