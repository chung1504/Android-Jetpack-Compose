package com.example.androidjetpackcompose.presentation.ui.loginscreen

import android.net.ipsec.ike.SaProposal
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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidjetpackcompose.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {},
    onFacebookLoginClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passWordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    val primaryBlue = Color(0xFF4A89FF) // Màu xanh nút bấm
    val textFieldBg = Color(0xFFF3F4F6) // Màu xám nhạt của background text field

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
            Text(
                text = "Login",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(40.dp))

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
                onValueChange = {email = it},
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
                onValueChange = {password = it},
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
                    IconButton(onClick = {passWordVisible = !passWordVisible}) {
                        Icon(
                            imageVector = if (passWordVisible)
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
                    if (passWordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation()
            )

            // Forget password
            TextButton(onClick = {}, modifier = Modifier.align(Alignment.End)) {
                Text(
                    text = "Forget password?",
                    color = primaryBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            // Keep me logged in
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = {rememberMe = it},
                    colors = CheckboxDefaults.colors(
                        checkedColor = primaryBlue,
                        uncheckedColor = Color.Gray
                    )
                )
                Text(text = "Keep me logged in", color = Color.Gray, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Button login
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(contentColor = Color.White)
            ) {
                Text(text = "Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Divider
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

            // Button login with Google and Facebook
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Button login with Google
                OutlinedButton(
                    onClick = {onGoogleLoginClick},
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
                    onClick = {onFacebookLoginClick},
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

            // Register now
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Don't have an account?", color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = {}) {
                    Text(text = "Register now", color = primaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}