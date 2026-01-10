package com.example.app.ui.profile.edit

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.app.ui.profile.ProfileViewModel

@Composable
fun EditProfileRoute(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.editState.collectAsState()
    val context = LocalContext.current  // ✅ thêm dòng này

    // lưu xong thì back
    LaunchedEffect(uiState.success) {
        if (uiState.success) onBack()
    }

    EditProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onUsernameChange = viewModel::onEditUsernameChange,
        onFullNameChange = viewModel::onEditFullNameChange,
        onGenderChange = viewModel::onEditGenderChange,
        onAddressChange = viewModel::onEditAddressChange,
        onPhoneChange = viewModel::onEditPhoneChange,
        onAvatarPicked = viewModel::onEditAvatarPicked,
        onSubmit = { viewModel.submitEditProfile(context) }
    )
}
