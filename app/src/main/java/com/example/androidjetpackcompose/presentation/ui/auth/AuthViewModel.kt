package com.example.androidjetpackcompose.presentation.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidjetpackcompose.domain.usecase.auth.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor (
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
): ViewModel() {
    // Quan sát UI
    private val _uiState = MutableStateFlow(AuthUIState())
    // Lấy để sử dụng
    val uiState = _uiState.asStateFlow()

    // Hàm được gọi từ UI khi có token
    fun loginWithGoogle(idToken: String) {
        Log.d("STEP", "3. INSIDE ViewModel")
        // Coroutine scope của ViewModel
        viewModelScope.launch {
            // update { ... } = lấy state cũ, copy ra state mới
            // it.copy(...) → Nó tạo bản sao mới của object, thay đổi 1 số thuộc tính mà không ảnh hưởng tới object cũ
            // Ý nghĩa:
            // it = state hiện tại (AuthUIState(isLoading=false, isSuccess=false, error="xxx"))
            // copy → tạo state mới với: isLoading = true,error = null
            // update → gán giá trị mới cho _uiState.value thread-safe
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Gọi login login với token
                val user = signInWithGoogleUseCase(idToken)
                Log.d("STEP", "4. USER = $user")
                // Nếu login thành công
                if (user != null) {
                    // Update state
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Login failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                Log.d("VM", "ERROR = ${e.message}")
            }
        }
    }

    // ✅ Reset để tránh navigate lại khi quay back
    fun resetState() {
        _uiState.update { it.copy(isSuccess = false, error = null) }
    }
}