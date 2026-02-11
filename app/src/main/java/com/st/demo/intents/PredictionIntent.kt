package com.st.demo.intents

import com.st.demo.model.Sensor
import com.st.demo.model.SensorData

sealed class PredictionIntent {
    data class predict(val body: SensorData): PredictionIntent()

    data class addSensor(val sensor: Sensor): PredictionIntent()
    data class removeSensor(val id: String): PredictionIntent()
}