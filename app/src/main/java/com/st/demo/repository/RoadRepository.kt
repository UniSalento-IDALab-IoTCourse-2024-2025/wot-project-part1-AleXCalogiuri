package com.st.demo.repository

import com.st.demo.model.Road
import com.st.demo.wrappers.Resource

interface RoadRepository {
    suspend fun getRoadByCity(city: String): Resource<List<Road>>
    suspend fun getAllRoads(): Resource<List<Road>>
}

