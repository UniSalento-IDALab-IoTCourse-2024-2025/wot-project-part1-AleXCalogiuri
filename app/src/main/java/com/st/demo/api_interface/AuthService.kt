package com.st.demo.api_interface

import com.st.demo.model.LoginResponse
import com.st.demo.model.LoginUser
import com.st.demo.model.RegistrationResponse
import com.st.demo.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


interface AuthService {


    // ENDPOINT DI AUTHENTICATION SERVICE

    @POST("api/users/authenticate")
    suspend fun login(@Body body: LoginUser): Response<LoginResponse>

    @POST("api/signup/")
    suspend fun registration(@Body body: User): Response<RegistrationResponse>

    @GET("api/users/{id}")
    suspend fun getById(@Path("id") id: String): Response<User>

    @GET("api/users/getInfo/{email}")
    suspend fun getByEmail(@Header("Authorization") token: String, @Path("email") email: String): Response<User>

}