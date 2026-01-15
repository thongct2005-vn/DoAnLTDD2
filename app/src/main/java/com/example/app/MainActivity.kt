package com.example.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.app.network.SocketManager
import com.example.app.network.dto.auth.AuthEvent
import com.example.app.network.dto.auth.AuthEventBus
import com.example.app.network.dto.auth.AuthManager
import com.example.app.ui.auth.login.LoginViewModel
import com.example.app.ui.navigation.AppNavHost
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SocketManager.connect()
        var startDestination by mutableStateOf<String?>(null)

        lifecycleScope.launch {
            val result = loginViewModel.refreshAccessToken()
            startDestination = if (result.isSuccess) "main" else "welcome"
        }
        setContent {


            MaterialTheme {
                RequestNotificationPermission()
                val destination = startDestination
                if (destination != null) {

                    val navController = rememberNavController()
                    ObserveAuthEvents(navController)
                    AppNavHost(
                        navController = navController,
                        startDestination = destination
                    )
                } else {
                    LoadingSplashScreen()
                }
            }
        }
    }

    @Composable
    private fun ObserveAuthEvents(navController: NavHostController) {
        LaunchedEffect(Unit) {
            AuthEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.SessionExpired -> {
                        SocketManager.disconnect()
                        AuthManager.clear()
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingSplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

        }
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}