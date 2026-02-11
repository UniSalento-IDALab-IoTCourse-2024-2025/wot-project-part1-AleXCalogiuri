package com.st.demo.api_interface

import com.st.demo.model.Road
import retrofit2.Response
import retrofit2.http.GET

import retrofit2.http.Path

interface RoadInterface {

    /*
    *
    * @GetMapping("/by-city/{city}")
    public ResponseEntity<List<RoadDTO>> getRoadsByCity(@PathVariable String city) {
        List<RoadDTO> roads = roadService.getRoadsByCityDTO(city);
        if (roads.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(roads);
    }
    * */

    @GET("/api/roads/by-city/{city}")
    suspend fun getRoadByCity(@Path("city") city: String): Response<List<Road>>

    @GET("/api/roads")
    suspend fun getAllRoads(): Response<List<Road>>


}