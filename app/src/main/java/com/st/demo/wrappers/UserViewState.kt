package com.st.demo.wrappers

import com.st.demo.model.LoginResponse
import com.st.demo.model.RegistrationResponse
import com.st.demo.model.User


data class UserViewState(
    val isLoading: Boolean = false,
    val loginResponse: LoginResponse? = null,
    val registrationResponse: RegistrationResponse? =null,
    val user: User? = null,
    val users: List<User>? = null,
    val errorMessage: String? = null
)