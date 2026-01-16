package com.st.demo.use_case


import com.st.demo.model.LoginResponse
import com.st.demo.model.LoginUser
import com.st.demo.model.RegistrationResponse
import com.st.demo.model.User
import com.st.demo.repository_impl.AuthRepositoryImpl
import com.st.demo.wrappers.Resource

import javax.inject.Inject

class AuthUsesCases @Inject constructor(private val authRepository: AuthRepositoryImpl) {
    suspend operator fun invoke(loginUser: LoginUser): Resource<LoginResponse> = authRepository.login(loginUser)
}

class SignUpUseCase @Inject constructor(private val authRepository: AuthRepositoryImpl){
    suspend operator fun invoke(user: User): Resource<RegistrationResponse> = authRepository.signup(user)
}

class GetUserByEmail @Inject constructor(private val authRepository: AuthRepositoryImpl){
    suspend operator fun invoke(token: String,email: String): Resource<User> = authRepository.getUserByEmail(token,email)
}
