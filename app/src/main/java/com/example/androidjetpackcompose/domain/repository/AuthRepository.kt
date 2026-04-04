package com.example.androidjetpackcompose.domain.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository{
    // FirebaseUse: là class của Firebase Authentication
    // địa diện người dùng đã đăng nhập thành công với Firebase
    // Nó chứa các thong tin như: uid, displayName, email, photoUrl, isEmailVerified
    suspend fun signInWithGoogle(idToken: String): FirebaseUser?
}

