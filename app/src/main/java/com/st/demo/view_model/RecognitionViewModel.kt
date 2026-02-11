package com.st.demo.view_model

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.demo.intents.PredictionIntent
import com.st.demo.model.Sensor
import com.st.demo.model.SensorDataResponse
import com.st.demo.use_case.AddSensor
import com.st.demo.use_case.Classifica
import com.st.demo.use_case.RemoveSensor
import com.st.demo.wrappers.PredictionViewState
import com.st.demo.wrappers.Resource
import dagger.hilt.android.internal.Contexts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val prediction: Classifica,
    private val addSensor: AddSensor,
    private val removeSensor: RemoveSensor
) : ViewModel() {

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private var started = false

    fun start() {
        if (started) return
        started = true
        processIntents()
    }


    //REST API
    private val _state = MutableStateFlow(PredictionViewState())
    val state: StateFlow<PredictionViewState> = _state

    private val _intentChannel = Channel<PredictionIntent>(Channel.Factory.UNLIMITED)

    fun sendIntent(intent: PredictionIntent) {
        viewModelScope.launch {
            _intentChannel.send(intent)
        }
    }

    fun resetState() {
        _state.update { PredictionViewState() }
    }

    private fun processIntents() {
        viewModelScope.launch {
            _intentChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    is PredictionIntent.addSensor -> executeSuspend { addSensor(intent.sensor) }
                    is PredictionIntent.removeSensor-> executeSuspend { removeSensor(intent.id) }
                    is PredictionIntent.predict -> executeSuspend { prediction(intent.body) }
                }
            }
        }
    }

    private fun <T> executeSuspend(block: suspend () -> Resource<T>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = block()
            when (result) {
                is Resource.Success -> handleSuccess(result.data)
                is Resource.Error -> _state.update { it.copy(isLoading = false, message = result.message) }
                is Resource.Loading -> Unit // Shouldn't happen in a one-time operation
            }
        }
    }

    private fun <T> handleSuccess(data: T?) {
        _state.update { currentState ->
            when (data) {
                is SensorDataResponse -> currentState.copy(isLoading = false, predictionResponse = data, message = null)
                is Sensor -> currentState.copy(isLoading = false, sensor = data as Sensor?, message = null)
                is List<*> -> currentState.copy(isLoading = false, sensorList = data.filterIsInstance<Sensor>(), message = null)
                else -> currentState.copy(isLoading = false, message = "Unknown data type")
            }
        }
    }


    fun extractCoordinatesFromLoggable(dataString: String): Triple<Float, Float, Float> {
        // Regex per estrarre i valori delle coordinate dell'accelerometro
        val xRegex = Regex("X\\s*=\\s*([-+]?\\d*\\.?\\d+)\\s*dps")
        val yRegex = Regex("Y\\s*=\\s*([-+]?\\d*\\.?\\d+)\\s*dps")
        val zRegex = Regex("Z\\s*=\\s*([-+]?\\d*\\.?\\d+)\\s*dps")

        val xResult = xRegex.find(dataString)
        val yResult = yRegex.find(dataString)
        val zResult = zRegex.find(dataString)

        val xValue = xResult?.groupValues?.get(1)?.toFloat() ?: 0.0f
        val yValue = yResult?.groupValues?.get(1)?.toFloat() ?: 0.0f
        val zValue = zResult?.groupValues?.get(1)?.toFloat() ?: 0.0f

        return Triple(xValue, yValue, zValue)
    }


    //Questa ottiene la via dalle coordinate
    suspend fun getStreetName(context: Context, latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(Contexts.getApplication(context), Locale.ITALIAN)
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                addresses?.firstOrNull()?.let { address ->
                    // Costruisci l'indirizzo
                    buildString {
                        // Via e numero civico
                        address.thoroughfare?.let { append(it) }
                        address.subThoroughfare?.let { append(" $it") }

                        // Città/Comune
                        address.locality?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }

                        // Provincia (sigla)
                        address.adminArea?.let { adminArea ->
                            // igla della provincia
                            address.subAdminArea?.let { provincia ->
                                if (isNotEmpty()) append(", ")
                                append(provincia)
                            }
                        }

                        // Regione
                        address.adminArea?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }

                        // CAP
                        address.postalCode?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                    }.ifEmpty {
                        address.getAddressLine(0) ?: "Indirizzo non disponibile"
                    }
                } ?: "Indirizzo non disponibile"

            } catch (e: Exception) {
                Log.e("PotholeDetection", "Errore geocoding", e)
                "EXCEPTION: ${e.message}: Lat: %.4f, Lon: %.4f".format(latitude, longitude)
            }
        }
    }

}