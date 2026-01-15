

package com.example.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.data.repository.CommentRepository
import com.example.app.data.repository.PostRepository
import com.example.app.data.repository.ProfileRepository
import com.example.app.domain.usecase.CreatePostUseCase
import com.example.app.network.dto.auth.AuthManager
import com.example.app.ui.auth.forgot_password.ForgotPasswordUiState
import com.example.app.ui.auth.forgot_password.ForgotPasswordViewModel
import com.example.app.ui.auth.forgot_password.components.ForgotEmailScreen
import com.example.app.ui.auth.forgot_password.components.ForgotNewPassWordScreen
import com.example.app.ui.auth.forgot_password.components.ForgotOtpScreen
import com.example.app.ui.auth.login.LoginScreen
import com.example.app.ui.auth.login.LoginViewModel
import com.example.app.ui.auth.login.UpdateUsernameAccountScreen
import com.example.app.ui.auth.register.RegisterViewModel
import com.example.app.ui.auth.register.components.RegisterDateScreen
import com.example.app.ui.auth.register.components.RegisterEmailScreen
import com.example.app.ui.auth.register.components.RegisterFullNameScreen
import com.example.app.ui.auth.register.components.RegisterOtpScreen
import com.example.app.ui.auth.register.components.RegisterPasswordScreen
import com.example.app.ui.comment.CommentScreen
import com.example.app.ui.comment.CommentViewModel
import com.example.app.ui.comment.CommentViewModelFactory
import com.example.app.ui.feed.FeedViewModel
import com.example.app.ui.feed.create.CreatePostRoute
import com.example.app.ui.feed.create.CreatePostUiEvent
import com.example.app.ui.feed.create.CreatePostViewModel
import com.example.app.ui.feed.create.CreatePostViewModelFactory
import com.example.app.ui.feed.post.PostDetailScreen
import com.example.app.ui.follow.FollowerFollowingViewModel
import com.example.app.ui.follow.FollowersFollowingScreen
import com.example.app.ui.main.MainScreen
import com.example.app.ui.profile.ProfileScreen
import com.example.app.ui.profile.ProfileUiState
import com.example.app.ui.profile.ProfileViewModel
import com.example.app.ui.profile.edit.EditProfileScreen
import com.example.app.ui.search.SearchScreen
import com.example.app.ui.search.SearchViewModel
import com.example.app.ui.share.SharePostScreen
import com.example.app.ui.share.SharePostViewModelFactory
import com.example.app.ui.welcome.WelcomeScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = if (AuthManager.isLoggedIn()) "main" else "welcome"
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
                onLoginSuccess = {
                    if (AuthManager.isFirstLogin()) {
                        navController.navigate("update_username") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                },
                onRegisterClick = { navController.navigate("register_fullName") },
                onForgotPassWordActivity = { navController.navigate("forgot_email") }
            )
        }


        composable("update_username") {
            UpdateUsernameAccountScreen(
                onUpdateSuccess = {
                    navController.navigate("main") {
                        popUpTo("update_username") { inclusive = true }
                    }
                }
            )
        }


        composable("forgot_email") {
            val uiState by forgotVm.uiState.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is ForgotPasswordUiState.OtpSent) {
                    navController.navigate("forgot_otp")
                }
            }

            ForgotEmailScreen(
                systemError = (uiState as? ForgotPasswordUiState.Error)?.message,
                isLoading = uiState is ForgotPasswordUiState.Loading,
                onBack = { navController.popBackStack() },
                onNext = { email ->
                    forgotVm.sendOtp(email)
                }
            )
        }


        composable("forgot_otp") {
            val uiState by forgotVm.uiState.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is ForgotPasswordUiState.OtpVerified) {
                    navController.navigate("forgot_password_reset")
                }
            }

            ForgotOtpScreen(
                systemError = (uiState as? ForgotPasswordUiState.Error)?.message,
                isLoading = uiState is ForgotPasswordUiState.Loading,
                onBack = { navController.popBackStack() },
                onNext = { otp ->
                    forgotVm.verifyOtp(otp)
                },
                onNoCode = {
                    // nếu muốn resend OTP thì gọi lại sendOtp
                }
            )
        }


        composable("forgot_password_reset") {
            val uiState by forgotVm.uiState.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is ForgotPasswordUiState.PasswordResetSuccess) {
                    navController.navigate("login") {
                        popUpTo("forgot_email") { inclusive = true }
                    }
                }
            }

            ForgotNewPassWordScreen(
                onBack = { navController.popBackStack() },
                onDone = { newPass, confirmPass ->
                    forgotVm.resetPassword(newPass, confirmPass)
                }
            )
        }

        navigation(
            route = "main",
            startDestination = "home"
        ) {
            composable("home") {
                MainScreen(rootNavController = navController)
            }

            composable("search") {
                val searchVm: SearchViewModel = viewModel()
                SearchScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onProfileClick = { userId ->
                        navController.navigate("profile/$userId")
                    },
                    viewModel = searchVm
                )
            }


            composable(
                route = "comments/{postId}",
                arguments = listOf(
                    navArgument("postId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""

                val repository = CommentRepository()

                val commentViewModel: CommentViewModel = viewModel(
                    factory = CommentViewModelFactory(repository)
                )

                CommentScreen(
                    postId = postId,
                    onClose = { navController.popBackStack() },
                    viewModel = commentViewModel,
                    onProfileClick = { targetId ->
                        navController.navigate("profile/$targetId")
                    }


                )
            }


            composable(
                route = "profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "me"

                val profileViewModel: ProfileViewModel = viewModel(backStackEntry)
                val feedVm: FeedViewModel = viewModel()

                ProfileScreen(
                    navController = navController,
                    viewModel = profileViewModel,
                    userId = userId,
                    feedViewModel = feedVm
                )
            }


            composable("edit_profile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "me"
                val context = LocalContext.current

                val profileViewModel: ProfileViewModel = viewModel() // hoặc shared với ProfileScreen

                val editState by profileViewModel.editState.collectAsState()

                // Load dữ liệu user từ API
                LaunchedEffect(userId) {
                    profileViewModel.loadUserDetailForEdit(userId)
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
                    onSubmit = {
                        profileViewModel.submitEditProfile(context) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("edit_msg", "Cập nhật hồ sơ thành công!") // Gửi thông báo đi
                            navController.popBackStack()
                        }
                    }
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
                val scope = rememberCoroutineScope()
                val repo = remember { ProfileRepository() }
                FollowersFollowingScreen(
                    userId = userId,
                    initialTab = initialTab,
                    viewModel = followViewModel,
                    onBack = { navController.popBackStack() },
                    onProfileClick = { clickedUserId ->
                        navController.navigate("profile/$clickedUserId")
                    },
                    onUnfollow = { id ->
                        scope.launch {
                            repo.unfollowUser(id)
                        }
                    }
                )
            }


            composable(
                route = "share_post/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""

                if (postId.isBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không tìm thấy bài viết để chia sẻ")
                    }
                    return@composable
                }

                val profileViewModel: ProfileViewModel = viewModel()
                val profileState by profileViewModel.uiState.collectAsState()
                val currentProfile = (profileState as? ProfileUiState.Success)?.profile

                val createVm: CreatePostViewModel = viewModel(
                    key = "share_post_$postId",  // key ổn định dựa trên postId
                    factory = SharePostViewModelFactory(
                        createPostUseCase = CreatePostUseCase(PostRepository()),
                        avatarUrl = currentProfile?.avatarUrl,
                        userName = currentProfile?.fullName ?: "Bạn"
                    )
                )

                LaunchedEffect(postId) {
                    createVm.initShareMode(postId)  // gọi hàm với postId (String)
                }

                LaunchedEffect(Unit) {
                    createVm.events.collect { event ->
                        if (event is CreatePostUiEvent.Close) {
                            navController.popBackStack()
                        }
                    }
                }

                val state by createVm.uiState.collectAsState()
                SharePostScreen(
                    state = state,
                    onAction = createVm::onAction
                )
            }

            composable(
                route = "post_detail/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""

                // PostDetailScreen tự khởi tạo FeedViewModel bên trong (nhờ default param)
                // Hoặc bạn có thể khởi tạo ở đây nếu muốn custom factory
                PostDetailScreen(
                    postId = postId,
                    navController = navController
                )
            }


            composable("create_post") {
                val profileViewModel: ProfileViewModel = viewModel()
                val profileState by profileViewModel.uiState.collectAsState()
                val successState = profileState as? ProfileUiState.Success
                val currentProfile = successState?.profile

                val createVm: CreatePostViewModel = viewModel(
                    factory = CreatePostViewModelFactory(
                        createPostUseCase = CreatePostUseCase(PostRepository()),  // hoặc inject bằng Hilt/Dagger
                        avatarUrl = currentProfile?.avatarUrl,
                        userName = currentProfile?.fullName ?: "Bạn"
                    )
                )

                CreatePostRoute(
                    viewModel = createVm,
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
}