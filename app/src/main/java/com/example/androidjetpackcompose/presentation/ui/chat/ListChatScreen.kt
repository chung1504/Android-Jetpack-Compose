package com.example.androidjetpackcompose.presentation.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListChatScreen(
    onBackLoginClick: () -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text(text = "List Chat") },
                navigationIcon = {
                    IconButton(onBackLoginClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {

                },
                colors = TopAppBarDefaults.topAppBarColors (
                    containerColor = Color(0xFF4A89FF),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )

        },
        // modifier = Modifier.padding(24.dp),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            Button(
                onClick = onChatClick
            ) {
                Text(text = "Chat")
            }
        }
    }
}