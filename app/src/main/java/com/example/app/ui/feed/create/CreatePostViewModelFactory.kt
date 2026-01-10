package com.example.app.ui.feed.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CreatePostViewModelFactory(
    private val avatarUrl: String?,
    private val userName: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
            return CreatePostViewModel(avatarUrl, userName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
