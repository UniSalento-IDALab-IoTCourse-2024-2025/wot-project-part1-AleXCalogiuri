package com.st.demo.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.demo.intents.UserIntent
import com.st.demo.model.LoginResponse
import com.st.demo.model.RegistrationResponse
import com.st.demo.model.SecureStorageManager
import com.st.demo.model.User
import com.st.demo.use_case.AuthUsesCases
import com.st.demo.use_case.GetUserByEmail
import com.st.demo.use_case.SignUpUseCase
import com.st.demo.wrappers.Resource
import com.st.demo.wrappers.UserViewState


import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


/*
* The ViewModel handles Intents by calling
*  Use Cases and updating the ViewState based on their results.
* */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authenticate: AuthUsesCases,
    private val signup : SignUpUseCase,
    private val getUserProfile: GetUserByEmail,
    private val secureStorageManager: SecureStorageManager,
): ViewModel()
{

    private val _state = MutableStateFlow(UserViewState())
    val state: StateFlow<UserViewState> = _state

    private val _intentChannel = Channel<UserIntent>(Channel.UNLIMITED)

    init {
        Log.d("LOGIN_VIEWMODEL", "ViewModel creato con successo")
        Log.d("LOGIN_VIEWMODEL", "authenticate: $authenticate")
        Log.d("LOGIN_VIEWMODEL", "signup: $signup")
        processIntents()
    }

    // Canale per eventi di navigazione
    private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    sealed class NavigationEvent {
        object NavigateToHome : NavigationEvent()
        object NavigateToRegistrationSuccess : NavigationEvent()
    }

    fun sendIntent(intent: UserIntent) {
        viewModelScope.launch {
            _intentChannel.send(intent)
        }
    }
    private fun processIntents() {
        viewModelScope.launch {
            _intentChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    is UserIntent.authenticate -> executeSuspend { authenticate(intent.loginUser) }
                    is UserIntent.signUp -> executeSuspend { signup(intent.user) }
                    is UserIntent.getUser -> executeSuspend { getUserProfile(intent.token,intent.email) }
                }
            }
        }
    }

    private fun <T> executeSuspend(block: suspend () -> Resource<T>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = block()
            when (result) {
                is Resource.Success -> handleSuccess(result.data)
                is Resource.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                is Resource.Loading -> Unit // Shouldn't happen in a one-time operation
            }
        }
    }

    private fun <T> handleSuccess(data: T?) {
        _state.update { currentState ->
            when (data) {
                is LoginResponse -> {

                    if (data.jwt != null){
                        secureStorageManager.saveJwt(data.jwt)
                        secureStorageManager.saveRole(data.role)
                        viewModelScope.launch {
                            _navigationEvent.send(NavigationEvent.NavigateToHome)
                        }
                    }else if(data.message == "Bad credentials"){
                        currentState.copy(
                            isLoading = false,
                            loginResponse = null,
                            errorMessage = "Email o Password errata !"
                        )
                    }
                    currentState.copy(
                        isLoading = false,
                        loginResponse = data,
                        errorMessage = data.message
                    )
                }
                is RegistrationResponse -> {
                    if (data.message == "Registrazione effettuata!"){
                        viewModelScope.launch {
                            _navigationEvent.send(NavigationEvent.NavigateToRegistrationSuccess)
                        }
                    }

                    currentState.copy(isLoading = false, registrationResponse = data, errorMessage = null)
                }
                is User -> currentState.copy(isLoading = false, user = data, errorMessage = null)
                is List<*> -> currentState.copy(isLoading = false, users = data.filterIsInstance<User>(), errorMessage = null)

                else -> currentState.copy(isLoading = false, errorMessage = "Unknown data type")
            }
        }
    }

}