package com.example.androidjetpackcompose.presentation.ui.auth

data class AuthUIState (
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)