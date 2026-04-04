package com.example.androidjetpackcompose.app.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidjetpackcompose.presentation.ui.chat.ChatScreen
import com.example.androidjetpackcompose.presentation.ui.chat.ListChatScreen
import com.example.androidjetpackcompose.presentation.ui.auth.LoginScreen
import com.example.androidjetpackcompose.presentation.ui.auth.AuthViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.LOGIN_SCREEN
    ) {
        composable(Route.LOGIN_SCREEN) {
            val viewModel: AuthViewModel = hiltViewModel()
            // collectAsState() = "Nghe Flow → convert thành State để UI tự recompose"
            val uiState by viewModel.uiState.collectAsState()

            // ✅ Dùng Unit làm key phụ, observe uiState bên trong
            // LaunchedEffect: dùng chạy side effect (tác vụ phụ) khi UI thay đổi
            // Nó chạy trong coroutine
            LaunchedEffect(uiState.isSuccess) { // Mỗi khi uiState.isSuccess thay đổi thì nó chạy lại
                // Nếu login thành công thì
                if (uiState.isSuccess) {
                    // Đièu hướng vào màn hình list chat
                    navController.navigate(Route.LIST_CHAT_SCREEN) {
                        // Xóa stack navigation từ login trở về trước khỏi backstack
                        popUpTo(Route.LOGIN_SCREEN) {
                            inclusive = true  // Xóa luôn cả login screen khỏi backstack
                        }
                    }
                    viewModel.resetState() // ✅ Reset sau khi navigate
                }
            }

            LoginScreen(
                onLoginClick = { email, password ->
                    // TODO: thêm email/password login sau
                    navController.navigate(Route.LIST_CHAT_SCREEN)
                },
                onGoogleLoginClick = { idToken ->
                    Log.d("STEP", "1. GOT TOKEN: $idToken")
                    viewModel.loginWithGoogle(idToken)
                    Log.d("STEP", "2. CALLED loginWithGoogle")
                },
                onFacebookLoginClick = {
                    navController.navigate(Route.LIST_CHAT_SCREEN)
                }
            )
        }

        composable(Route.LIST_CHAT_SCREEN) {
            ListChatScreen(
                onBackLoginClick = {
                    navController.navigate(Route.CHAT_SCREEN)
                },
                onChatClick = {}
            )
        }

        composable(Route.CHAT_SCREEN) {
            ChatScreen()
        }
    }
}