package com.st.demo.wrappers

import com.st.demo.model.Road

data class RoadViewState (
    val isLoading: Boolean = false,
    val road: Road? = null,
    val roads: List<Road>? = null,
    val message: String? = null
)