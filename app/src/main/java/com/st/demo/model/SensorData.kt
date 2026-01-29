package com.st.demo.model

import kotlinx.serialization.SerialName
import java.time.LocalDateTime

data class SensorData (
    @SerialName("sensor_data_id")
    val sensorDataId: Int,
    @SerialName("accelerometer_x")
    val accelerometerX: Float,
    @SerialName("accelerometer_y")
    val accelerometerY: Float,
    @SerialName("accelerometer_z")
    val accelerometerZ: Float,
    @SerialName("gyroscope_x")
    val gyroscopeX: Float,
    @SerialName("gyroscope_y")
    val gyroscopeY: Float,
    @SerialName("gyroscope_z")
    val gyroscopeZ: Float,
    @SerialName("timestamp")
    val timestamp: LocalDateTime,
    @SerialName("gps_lat")
    val gpsLat: Float,
    @SerialName("gps_lon")
    val gpsLon: Float,
)