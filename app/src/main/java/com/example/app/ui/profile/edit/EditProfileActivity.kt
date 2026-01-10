package com.example.app.ui.profile.edit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.profile.ProfileViewModel
import com.example.app.ui.theme.AppTheme // đổi theo theme của bạn

class EditProfileActivity : ComponentActivity() {

    private val vm: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("username").orEmpty()
        val fullName = intent.getStringExtra("fullName").orEmpty()
        val avatarUrl = intent.getStringExtra("avatarUrl")

        // ✅ set thẳng vào editState (tạo 1 hàm trong VM)
        vm.initEditFormFromIntent(username, fullName, avatarUrl)

        setContent {
            AppTheme {
                EditProfileRoute(
                    viewModel = vm,
                    onBack = { finish() }
                )
            }
        }
    }
}