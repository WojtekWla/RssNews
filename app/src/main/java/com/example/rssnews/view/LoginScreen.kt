package com.example.rssnews.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.rssnews.viewModel.LoginViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.example.rssnews.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    goToMainScreen: () -> Unit,
    goToRegister: () -> Unit
) {
    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row (verticalAlignment = Alignment.CenterVertically){
                        Text(
                            text = stringResource(R.string.news),
                            color = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF0D665),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ) { innerPaddings ->
        val systemPaddings = WindowInsets.systemBars.asPaddingValues()
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()
        var displaySuccessDialog by remember { mutableStateOf(false) }
        var displayFailDialog by remember { mutableStateOf(false) }

        if (displaySuccessDialog) {
            SuccessDialog {
                displaySuccessDialog = false
                goToMainScreen()
            }
        }

        if (displayFailDialog) {
            FailDialog {
                displayFailDialog = false
            }
        }


        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPaddings)
                .padding(bottom = systemPaddings.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            TextField(
                email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                coroutineScope.launch {
                    val result = loginViewModel.authenticate(email, password)
                    if (result) {
                        displaySuccessDialog = true
                    } else {
                        displayFailDialog = true
                    }
                }
            }) {
                Text(stringResource(R.string.login))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = goToRegister) {
                Text(stringResource(R.string.register))
            }

        }
    }
}

@Composable
fun SuccessDialog(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(R.string.successfully_logged_in),
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun FailDialog(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(R.string.failed_to_log_in),
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }
    }
}
