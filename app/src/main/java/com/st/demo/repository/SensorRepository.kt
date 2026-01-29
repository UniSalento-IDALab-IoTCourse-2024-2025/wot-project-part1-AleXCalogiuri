package com.st.demo.repository

import com.st.demo.api_interface.SensorDataService
import com.st.demo.model.Sensor
import com.st.demo.model.SensorData
import com.st.demo.model.SensorDataResponse
import com.st.demo.wrappers.Resource
import retrofit2.Response

interface SensorRepository {

    suspend fun create_sensor(sensor: Sensor): Resource<Sensor>

    suspend fun remove_sensor(id: String): Resource<String>

    suspend fun classifica(sensorData: SensorData): Resource<SensorDataResponse>
}