package com.st.demo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SensorDataResponse(
    @SerialName("classificazione")
    val classificazione: String,
    @SerialName("gps_latitude")
    val gps_latitude: Double,
    @SerialName("gps_longitude")
    val gps_longitude: Double,
    @SerialName("strada_rilevamento")
    val strada_rilevamento: String

)