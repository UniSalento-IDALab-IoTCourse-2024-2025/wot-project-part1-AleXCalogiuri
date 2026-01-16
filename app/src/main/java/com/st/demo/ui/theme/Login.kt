package com.st.demo.ui.theme


import android.util.Log
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.st.demo.intents.UserIntent
import com.st.demo.model.LoginUser
import com.st.demo.view_model.LoginViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(
    viewModel: LoginViewModel,
    navController: NavController
) {
    var email = remember { mutableStateOf("") }
    var password = remember { mutableStateOf("") }
    var passwordVisible = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isClicked = remember { mutableStateOf(false) }
    Log.e("DEBUG_LOGIN", "========== INIZIO FUNZIONE LOGIN ==========")
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { //Questa gestisce la navigazione in caso di login
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is LoginViewModel.NavigationEvent.NavigateToHome -> {
                    val loginMail = email.value
                    navController.navigate("home/$loginMail") {
                        popUpTo("login") { inclusive = true }
                    }
                }

                else -> {}
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    )
    {
        val state by viewModel.state.collectAsState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        )
        {
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                placeholder = { Text("inserisci@email.com") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email"
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    cursorColor = Color(0xFF3A7BD5),
                    unfocusedContainerColor = Color(0xFFE3F2FD),
                    focusedContainerColor = Color.White,
                    unfocusedLabelColor = Color.Black,
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") }, // Corretto: aggiunta lambda
                placeholder = { Text("Inserisci Password") }, // Corretto: aggiunta lambda
                singleLine = true,
                visualTransformation = if (passwordVisible.value)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Nascondi tastiera quando premi Invio
                    }
                ),
                trailingIcon = {
                    val image = if (passwordVisible.value)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff
                    val description = if (passwordVisible.value)
                        "Nascondi password"
                    else
                        "Mostra password"
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) { // Corretto
                        Icon(
                            imageVector = image,
                            contentDescription = description
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    cursorColor = Color(0xFF3A7BD5),
                    unfocusedContainerColor = Color(0xFFE3F2FD),
                    focusedContainerColor = Color.White,
                    unfocusedLabelColor = Color.Black,
                ),
                shape = RoundedCornerShape(8.dp)
            )


            Button(
                onClick = {
                    if (email.value.isNotEmpty() && password.value.isNotEmpty()) {
                        val auth = LoginUser(email.value, password.value)
                        viewModel.sendIntent(UserIntent.authenticate(auth))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A7BD5)),
                enabled = !state.isLoading // Disabilita il bottone durante il caricamento
            ) {
                Text(if (state.isLoading) "Caricamento..." else "Accedi")
            }



            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }


        }
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), Color(0xFF3A7BD5))
            }
        }
    }
}

