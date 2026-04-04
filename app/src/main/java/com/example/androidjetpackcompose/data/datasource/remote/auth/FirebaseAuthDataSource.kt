package com.example.androidjetpackcompose.data.datasource.remote.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor() {
    // getInstance() = lấy instance duy nhất (singleton)
    // Singleton = chỉ có 1 object duy nhất trong toàn app
    // auth là đối tượng chính để tháo tác đăng nhoâoậ ,, logout, check user hieên tại
    private val auth = FirebaseAuth.getInstance()

    // xác thực tài khoản google, đăng nhập vào firebase và trả về thông tin user
    suspend fun signInWithGoogle(idToken: String): FirebaseUser? {
        // huyển Google idToken sang credential Firebase.
        // Credential này là “chứng minh thư” để Firebase xác thực Google account.
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        // Sử dụng credential để đăng nhập vào Firebase, chưa thông tin use và token
        val result = auth.signInWithCredential(credential).await()
        Log.d("AUTH", "USER = ${result.user}")
        // Nếu login thành công → trả về FirebaseUser
        return result.user
    }
}