package com.st.demo.model

import kotlinx.serialization.SerialName

data class Sensor(
    @SerialName("sensor_id")
    val sensorId: String,
    @SerialName("serial_number")
    val serialNumber: String,
    @SerialName("model")
    val model: String,
    @SerialName("status")
    val status: String
)