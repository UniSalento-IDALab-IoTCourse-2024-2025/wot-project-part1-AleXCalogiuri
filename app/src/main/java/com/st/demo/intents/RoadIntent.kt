package com.st.demo.intents

sealed class RoadIntent {
    data object GetAllRoad : RoadIntent() //se non servono parametri in input fai object
    data class GetRoadByCity(val city: String) : RoadIntent()
}


