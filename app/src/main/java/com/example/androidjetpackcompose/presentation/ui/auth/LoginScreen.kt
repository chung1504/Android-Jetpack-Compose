package com.example.androidjetpackcompose.presentation.ui.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidjetpackcompose.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = {_, _ ->},
    onGoogleLoginClick: (String) -> Unit = {},
    onFacebookLoginClick: () -> Unit = {}
) {
    // remember → giữ state khi recomposition
    // mutableStateOf → observable
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    val primaryBlue = Color(0xFF4A89FF)
    val textFieldBg = Color(0xFFF3F4F6)
    val context = LocalContext.current

    // Tạo client để login Google
    val googleSignInClient = GoogleSignIn.getClient(
        context,
        // Config login
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Yêu cầu Google trả về ID TOKEN
            .requestIdToken("539718296920-kaou447642ae4v116f2559ggrggprjkb.apps.googleusercontent.com")
            // Xin email từ Google account
            .requestEmail()
            .build(),
    )

    // Tránh callback bị "stale" khi mà recomposition , luon giữ version mới nhất
    val currentOnGoogleLoginClick by rememberUpdatedState(onGoogleLoginClick)
    // Đây là API mới thay cho startActivityForResult
    val launcher = rememberLauncherForActivityResult(
        // Nói rằng: “tôi sẽ mở 1 Activity và nhận result”
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("STEP", "RESULT CODE = ${result.resultCode}") // ← thêm dòng này
        Log.d("STEP", "RESULT_OK = ${Activity.RESULT_OK}")
        // Nếu user login thành công
        if (result.resultCode == Activity.RESULT_OK) {
            // Lấy data Google trả về
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                // Parse kết quả
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken // lấy TOKEN

                Log.d("STEP", "0. idToken = '$idToken'")          // in ra giá trị thật
                Log.d("STEP", "0. isNull = ${idToken == null}")    // kiểm tra null
                Log.d("STEP", "0. isEmpty = ${idToken?.isEmpty()}") // có thể là empty string?

                // Nếu có token → gửi ra ngoài (AppNavGraph)
                if (idToken != null) {
                    currentOnGoogleLoginClick(idToken)
                } else {
                    Log.d("GOOGLE", "ID TOKEN NULL")
                }
            } catch (e: Exception) {
                Log.e("STEP", "EXCEPTION: ${e.message}")
            }
        }
    }

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // title
            LoginTitle()

            // Email
            EmailField(email) {email = it}

            // Password
            PasswordField(
                password = password,
                visible = passwordVisible,
                onValueChange = {password = it},
                onToggle = {passwordVisible = !passwordVisible}
            )

            // Forget password
            ForgetPassword(
                color = primaryBlue,
                modifier = Modifier.align(Alignment.End)
            )

            // Keep me logged in
            RememberMe(
                checked = rememberMe,
                onChange = {rememberMe = it},
                color = primaryBlue
            )

            // Button login
            LoginButton {
                onLoginClick(email, password)
            }

            // Divider
            DividerOr()

            // Login Google and Facebook
            SocialLogin(
                onGoogleClick = {
                    Log.d("GOOGLE", "CALLED")

                    // 🔥 Bắt Google logout trước → tránh cache account cũ
                    googleSignInClient.signOut().addOnCompleteListener {
                        Log.d("GOOGLE", "SIGNED OUT")
                        // 👉 Sau khi logout xong mới mở login
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                },
                onFacebookClick = onFacebookLoginClick
            )

            // Register now
            RegisterNow(primaryBlue)
        }
    }
}

// Title
@Composable
fun LoginTitle() {
    Text(
        text = "Login",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(40.dp))
}

// Email
@Composable
fun EmailField(email: String, onValueChange: (String) -> Unit) {
    // Label Email
    Text (
        text = "Email",
        modifier = Modifier.fillMaxWidth(),
        color = Color.Gray,
        fontWeight = FontWeight.Medium
    )

    // Input email
    OutlinedTextField (
        value = email,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top =8.dp),
        placeholder = { Text(
            text = "Enter your email",
            color = Color.Gray
        )},
        leadingIcon = {
            Icon(
                Icons.Outlined.Email,
                contentDescription = null,
                tint = Color.Gray
            )
        },
        colors = TextFieldDefaults.colors(
            // Màu nền khi đang focus (đang gõ)
            focusedContainerColor = Color.LightGray,
            // Màu nền khi chưa focus
            unfocusedContainerColor = Color.LightGray,
            disabledContainerColor = Color.Gray,
            // Màu đường gạch dưới / viền khi focus
            focusedIndicatorColor = Color.Transparent,
            // Màu viền khi không focus
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(20.dp))
}

// Password
@Composable
fun PasswordField(
    password: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    // Label password
    Text(
        text = "Password",
        modifier = Modifier.fillMaxWidth(),
        color = Color.Gray,
        fontWeight = FontWeight.Medium
    )

    // Input password
    OutlinedTextField (
        value = password,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        placeholder = { Text(
            text = "Enter your password",
            color = Color.Gray
        )},
        leadingIcon = {
            Icon(
                Icons.Outlined.Password,
                contentDescription = null,
                tint = Color.Gray
            )
        },
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (visible)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            // Màu nền khi đang focus (đang gõ)
            focusedContainerColor = Color.LightGray,
            // Màu nền khi chưa focus
            unfocusedContainerColor = Color.LightGray,
            disabledContainerColor = Color.Gray,
            // Màu đường gạch dưới / viền khi focus
            focusedIndicatorColor = Color.Transparent,
            // Màu viền khi không focus
            unfocusedIndicatorColor = Color.Transparent,
        ),
        visualTransformation =
            if (visible)
                VisualTransformation.None
            else
                PasswordVisualTransformation()
    )
}

// Forget password
@Composable
fun ForgetPassword(
    modifier: Modifier = Modifier,
    color: Color
) {
    TextButton(onClick = {}, modifier = modifier) {
        Text(
            text = "Forget password?",
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// Remember me
@Composable
fun RememberMe(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(
                checkedColor = color,
                uncheckedColor = Color.Gray
            )
        )
        Text(text = "Keep me logged in", color = Color.Gray, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(24.dp))
}

// Button Login
@Composable
fun LoginButton(onClick:() -> Unit) {
    Button(
        onClick = {onClick()},
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(contentColor = Color.White)
    ) {
        Text(text = "Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(32.dp))
}

// Divider
@Composable
fun DividerOr () {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        Text(
            text = "OR",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Gray
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
    }
}

// Login Google and Facebook
@Composable
fun SocialLogin (
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit
) {
    // Button login with Google and Facebook
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Button login with Google
        OutlinedButton(
            onClick = {onGoogleClick()},
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 0.5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Google")
        }

        // Button login with Facebook
        OutlinedButton(
            onClick = onFacebookClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 0.5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_facebook),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Facebook")
        }
    }
}

// RegisterNow
@Composable
fun RegisterNow(
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Don't have an account?", color = Color.Gray, fontSize = 14.sp)
        TextButton(onClick = {}) {
            Text(text = "Register now", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
