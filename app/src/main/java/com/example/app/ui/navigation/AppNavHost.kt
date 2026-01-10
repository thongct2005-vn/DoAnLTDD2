package com.example.app.ui.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.ui.auth.forgot_password.ForgotPasswordViewModel
import com.example.app.ui.auth.forgot_password.components.ForgotEmailScreen
import com.example.app.ui.auth.forgot_password.components.ForgotNewPassWordScreen
import com.example.app.ui.auth.forgot_password.components.ForgotOtpScreen
import com.example.app.ui.auth.login.LoginScreen
import com.example.app.ui.auth.login.LoginViewModel
import com.example.app.ui.auth.register.RegisterViewModel
import com.example.app.ui.auth.register.components.RegisterDateScreen
import com.example.app.ui.auth.register.components.RegisterEmailScreen
import com.example.app.ui.auth.register.components.RegisterFullNameScreen
import com.example.app.ui.auth.register.components.RegisterOtpScreen
import com.example.app.ui.auth.register.components.RegisterPasswordScreen
import com.example.app.ui.feed.create.CreatePostRoute
import com.example.app.ui.feed.create.CreatePostViewModel
import com.example.app.ui.feed.create.CreatePostViewModelFactory
import com.example.app.ui.follow.FollowerFollowingViewModel
import com.example.app.ui.follow.FollowersFollowingScreen
import com.example.app.ui.profile.ProfileScreen
import com.example.app.ui.profile.ProfileUiState
import com.example.app.ui.profile.ProfileViewModel
import com.example.app.ui.profile.edit.EditProfileScreen
import com.example.app.ui.search.SearchScreen
import com.example.app.ui.search.SearchViewModel
import com.example.app.ui.welcome.WelcomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "welcome"
) {
    val forgotVm: ForgotPasswordViewModel = viewModel()
    val registerVm: RegisterViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {


        composable("welcome") {
            WelcomeScreen(
                onLoginClick = {
                    navController.navigate("login")
                },
                onRegisterClick = {
                    // Điều hướng sang màn đăng ký (bạn cần định nghĩa route "register")
                    navController.navigate("register_fullName")
                }
            )
        }


        composable("register_fullName") {
            RegisterFullNameScreen(
                viewModel = registerVm,
                onClose = {
                    navController.popBackStack()
                },
                onNext = {
                    registerVm.nextStep()
                    navController.navigate("register_date")
                }
            )
        }


        composable("register_date") {
            RegisterDateScreen(
                viewModel = registerVm,
                onBack = {
                    navController.popBackStack()
                },
                onNext = {
                    registerVm.nextStep()
                    navController.navigate("register_email")
                }
            )
        }


        composable("register_email") {
            RegisterEmailScreen(
                viewModel = registerVm,
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigate("register_password")
                }
            )
        }


        composable("register_password") {
            RegisterPasswordScreen(
                viewModel = registerVm,
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigate("register_otp")
                }
            )
        }


        composable("register_otp") {
            RegisterOtpScreen(
                viewModel = registerVm,
                onVerifySuccess = {
                    navController.navigate("login") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onNoCode = {
//                    registerVm.onNoCodeClick()
//                    registerVm.resendOtp()
                }
            )
        }


        composable("login") {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onBackClick = { navController.popBackStack() },
                onRegisterClick = { navController.navigate("register_fullName") }, // Cần thêm route register nếu có
                onLoginSuccess = {
                    navController.navigate("feed") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPassWordActivity = {
                    navController.navigate("forgot_email")
                }
            )
        }


        composable("forgot_email") {
            ForgotEmailScreen(
                onBack = { navController.popBackStack() },
                onNext = { enteredEmail ->
                    forgotVm.updateEmail(enteredEmail)
                    navController.navigate("forgot_otp")
                }
            )
        }


        composable("forgot_otp") {
            ForgotOtpScreen(
                onBack = { navController.popBackStack() },
                onNext = { enteredOtp ->
                    forgotVm.updateOtp(enteredOtp)
                    navController.navigate("forgot_password_reset")
                },
                onNoCode = {
                    registerVm.onNoCodeClick()
                    registerVm.resendOtp()
                }
            )
        }


        composable("forgot_password_reset") {
            ForgotNewPassWordScreen(
                onBack = { navController.popBackStack() },
                onDone = { newPass ->
                    navController.navigate("login") {
                        popUpTo("forgot_email") { inclusive = true }
                    }
                }
            )
        }


        composable("feed") {
            val feedViewModel: FeedViewModel = viewModel()
            val loginViewModel: LoginViewModel = viewModel()
            FeedScreen(
                navController = navController,
                feedViewModel = feedViewModel,
                onClickLogout = {
                    loginViewModel.logout(onSuccess = {
                        navController.navigate("login") {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    })
                },
                onCreatePostClick = { navController.navigate("create_post") },
                onProfileClick = { userId ->
                    navController.navigate("profile/$userId")
                },
                onSearchClick = {
                    navController.navigate("search")
                }
            )
        }

        composable("search") {
            val searchVm: SearchViewModel = viewModel()
            SearchScreen(
                onBack = {
                    navController.popBackStack()
                },
                onProfileClick = { userId ->
                    // Khi chọn user, đi tới profile của họ
                    navController.navigate("profile/$userId")
                },
                viewModel = searchVm
            )
        }

        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "me"

            val profileViewModel: ProfileViewModel = viewModel(backStackEntry)

            ProfileScreen(
                navController = navController,
                viewModel = profileViewModel,
                userId = userId
            )
        }


        composable("edit_profile") { backStackEntry ->
            val context = LocalContext.current
            val profileBackStackEntry = remember(backStackEntry) {
                navController.previousBackStackEntry
            }
            val profileViewModel: ProfileViewModel = if (profileBackStackEntry != null) {
                viewModel(profileBackStackEntry)
            } else {
                viewModel()
            }
            val editState by profileViewModel.editState.collectAsState()
            LaunchedEffect(Unit) {
                profileViewModel.initEditFormFromCurrentProfile()
            }
            LaunchedEffect(editState.success) {
                if (editState.success) {
                    navController.popBackStack()
                    profileViewModel.resetEditSuccess()
                }
            }
            EditProfileScreen(
                uiState = editState,
                onBack = { navController.popBackStack() },
                onUsernameChange = profileViewModel::onEditUsernameChange,
                onFullNameChange = profileViewModel::onEditFullNameChange,
                onGenderChange = profileViewModel::onEditGenderChange,
                onAddressChange = profileViewModel::onEditAddressChange,
                onPhoneChange = profileViewModel::onEditPhoneChange,
                onAvatarPicked = profileViewModel::onEditAvatarPicked,
                onSubmit = { profileViewModel.submitEditProfile(context) }
            )
        }


        composable(
            route = "followers/{userId}?tab={tab}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("tab") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "me"
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            val followViewModel: FollowerFollowingViewModel = viewModel()

            FollowersFollowingScreen(
                userId = userId,
                initialTab = initialTab,
                viewModel = followViewModel,
                onBack = { navController.popBackStack() },
                onProfileClick = { clickedUserId ->
                    navController.navigate("profile/$clickedUserId")
                }
            )
        }

        // Thêm vào trong NavHost { ... } của AppNavHost

        composable("create_post") {
            val profileViewModel: ProfileViewModel = viewModel()
            val profileState by profileViewModel.uiState.collectAsState()
            val successState = profileState as? ProfileUiState.Success
            val currentProfile = successState?.profile

            val createPostVm: CreatePostViewModel = viewModel(
                factory = CreatePostViewModelFactory(
                    avatarUrl = currentProfile?.avatarUrl,
                    userName = currentProfile?.fullName ?: "No name"
                )
            )

            CreatePostRoute(
                viewModel = createPostVm,
                onClose = {
                    navController.popBackStack()
                },
                onSubmit = { content, privacy, mediaItems ->
                    navController.popBackStack()
                }
            )
        }
    }
}