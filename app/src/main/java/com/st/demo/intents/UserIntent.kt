package com.st.demo.intents

import com.st.demo.model.LoginUser
import com.st.demo.model.User


sealed class UserIntent {
    data class authenticate(val loginUser: LoginUser): UserIntent()
    data class signUp(val user: User): UserIntent()

    data class getUser(val token: String, val email: String): UserIntent()
}
