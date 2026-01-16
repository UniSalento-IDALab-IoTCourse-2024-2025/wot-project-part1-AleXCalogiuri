package com.st.demo.repository


import com.st.demo.model.LoginResponse
import com.st.demo.model.LoginUser
import com.st.demo.model.RegistrationResponse
import com.st.demo.model.User
import com.st.demo.wrappers.Resource


interface AuthRepository {
    suspend fun login(loginUser: LoginUser): Resource<LoginResponse>
    suspend fun signup(user : User): Resource<RegistrationResponse>

    suspend fun getUserByEmail(token: String,email: String): Resource<User>

}