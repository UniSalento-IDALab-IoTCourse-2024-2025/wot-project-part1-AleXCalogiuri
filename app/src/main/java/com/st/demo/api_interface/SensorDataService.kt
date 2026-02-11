package com.st.demo.api_interface


import com.st.demo.model.Sensor
import com.st.demo.model.SensorData
import com.st.demo.model.SensorDataResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface SensorDataService {
    @POST("/api/v1/sensors")
    suspend fun create_sensor(@Body sensor: Sensor): Response<Sensor>

    @POST("/api/v1/sensors/{id}")
    suspend fun remove_sensor(@Path("id") id: String): Response<String>


    @POST("/api/v1/model")
    suspend fun classifica(@Body sensorData: SensorData): Response<SensorDataResponse>

}