package com.example.androidjetpackcompose.domain.usecase.auth

import com.example.androidjetpackcompose.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor (
    private val authRepository: AuthRepository
) {
    // operator fun invoke: là 1 tính năng cho phép gọi object như 1 hàm
    /* Ví dụ:
        class MyClass {
            operator fun invoke(x: Int) = x * 2
        }

        val obj = MyClass()

        Dùng để gọi như 1 hàm:
        println(obj(5)) // 10 → tự hiểu là gọi invoke(5)

        Thay vì phải
        println(obj.invoke(5)) // hoặc viết 1 hàm khác tên gì đó
    */
    suspend operator fun invoke(idToken: String) =
        authRepository.signInWithGoogle(idToken)
}