package com.st.demo.wrappers

import com.st.demo.model.Sensor
import com.st.demo.model.SensorDataResponse

data class PredictionViewState(
    val isLoading: Boolean = false,
    val predictionResponse: SensorDataResponse? =null,
    val sensor: Sensor? = null,
    val sensorList: List<Sensor>? = null,
    val message: String? = null
)
