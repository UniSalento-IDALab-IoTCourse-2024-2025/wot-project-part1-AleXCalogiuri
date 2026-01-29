package com.st.demo.model

import kotlinx.serialization.SerialName

data class SensorDataResponse(
    @SerialName("process")
    val process: String,
    @SerialName("message")
    val message: String
)