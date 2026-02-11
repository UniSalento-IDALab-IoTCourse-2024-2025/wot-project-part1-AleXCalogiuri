package com.st.demo.repository_impl

import android.util.MalformedJsonException
import com.st.demo.api_interface.RoadInterface
import com.st.demo.model.Road
import com.st.demo.repository.RoadRepository
import com.st.demo.wrappers.Resource
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class RoadRepositoryImpl @Inject constructor(
    private val roadService : RoadInterface
): RoadRepository{
    override suspend fun getRoadByCity(city: String): Resource<List<Road>> = safeApiCall{
        roadService.getRoadByCity(city)
    }

    override suspend fun getAllRoads(): Resource<List<Road>> = safeApiCall {
        roadService.getAllRoads()
    }



    // Safe API call handler with suspend
    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = apiCall()
            when {
                response.isSuccessful -> {
                    response.body()?.let { Resource.Success(it) }
                        ?: Resource.Error("HTTP 200: Empty response body")
                }
                else -> Resource.Error("HTTP Error: ${response.code()} - ${response.message()}")
            }
        } catch (exception: Throwable) {
            handleApiError(exception)
        }
    }

    private fun <T> handleApiError(exception: Throwable): Resource<T> {
        val message = when (exception) {
            is TimeoutException -> "Request timed out. Please try again."
            is IOException -> "Network error. Please check your connection."
            is HttpException -> {
                when (val statusCode = exception.code()) {
                    204 -> "No Content. The server has successfully processed the request."
                    400 -> "Bad Request"
                    401 -> "Unauthorized. Please check your credentials."
                    403 -> "Forbidden. Access is denied."
                    404 -> "Resource not found."
                    500 -> "Internal Server Error. Please try again later."
                    503 -> "Service Unavailable. Please try again later."

                    else -> "Unexpected HTTP Error: $statusCode"
                }
            }
            is SerializationException, is MalformedJsonException -> "Malformed JSON received. Parsing failed."
            is IllegalArgumentException -> "Invalid argument provided. ${exception.message}"
            is IllegalStateException -> "Illegal application state. ${exception.message}"
            else -> "Unexpected error occurred: ${exception.message}"
        }
        return Resource.Error(message, exception)
    }
}