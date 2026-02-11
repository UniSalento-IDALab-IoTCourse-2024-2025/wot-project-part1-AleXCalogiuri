package com.st.demo.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SensorData (
    @SerialName("sensor_id")
    val sensorId: Int,
    @SerialName("acc_x")
    val accelerometerX: Double,
    @SerialName("acc_y")
    val accelerometerY: Double,

    @SerialName("gyro_x")
    val gyroscopeX: Double,
    @SerialName("gyro_y")
    val gyroscopeY: Double,
    @SerialName("gyro_z")
    val gyroscopeZ: Double,

    @SerialName("gps_latitude")
    val gpsLat: Double,
    @SerialName("gps_longitude")
    val gpsLon: Double,
    @SerialName("sensor_data_id")
    val sensorDataId: Int,
    @SerialName("strada_rilevamento")
    val stradaRilevamento: String
)