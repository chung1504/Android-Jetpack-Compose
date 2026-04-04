package com.example.androidjetpackcompose.data.repository

import com.example.androidjetpackcompose.data.datasource.remote.auth.FirebaseAuthDataSource
import com.example.androidjetpackcompose.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor (
    private val dataSource: FirebaseAuthDataSource
): AuthRepository {
    override suspend fun signInWithGoogle(idToken: String): FirebaseUser? {
        return dataSource.signInWithGoogle(idToken)
    }
}