package com.st.demo.repository_impl

import android.util.MalformedJsonException
import com.st.demo.api_interface.SensorDataService
import com.st.demo.model.Sensor
import com.st.demo.model.SensorData
import com.st.demo.model.SensorDataResponse
import com.st.demo.repository.SensorRepository
import com.st.demo.wrappers.Resource
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class SensorRepositoryImpl @Inject constructor(
    private val sensorDataService: SensorDataService
) : SensorRepository {
    override suspend fun create_sensor(sensor: Sensor): Resource<Sensor> = safeApiCall{
        sensorDataService.create_sensor(sensor)
    }


    override suspend fun remove_sensor(id: String): Resource<String> = safeApiCall{
        sensorDataService.remove_sensor(id)
    }

    override suspend fun classifica(sensorData: SensorData): Resource<SensorDataResponse> = safeApiCall{
        sensorDataService.classifica(sensorData)
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

    // Exception handling with detailed Resource.Error
    private fun <T> handleApiError(exception: Throwable): Resource<T> {
        val message = when (exception) {
            is TimeoutException -> "Request timed out. Please try again."
            is IOException -> "Network error. Please check your connection."
            is HttpException -> {
                when (val statusCode = exception.code()) {
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