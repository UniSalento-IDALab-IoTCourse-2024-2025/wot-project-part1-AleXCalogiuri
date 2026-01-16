package com.st.demo.api_interface

import com.st.demo.model.Notifica
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationInterface {
    // ENDPOINT DI NOTIFICATION SERVICE
    @GET("api/getNotification/admin") //per notifiche globali
    suspend fun getNotify(@Header("Authorization") token: String): Response<Notifica>

    @POST("api/getNotification/delete")
    suspend fun deleteNotify(@Header("Authorization") token: String,@Body body: Notifica): Response<Notifica>

    @GET("api/getNotification/{id}")
    suspend fun setAdminIsLetta(@Header("Authorization") token: String,@Path("id") id: String): Response<Notifica>

    @GET("api/getNotification/personal/{id}")
    suspend fun setUserIsLetta(@Header("Authorization") token: String,@Path("id") id: String): Response<Notifica>

}