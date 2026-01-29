package com.st.demo.feature_detail

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.st.demo.feature_detail.RecognitionData.xValues
import com.st.demo.feature_detail.RecognitionData.yValues
import com.st.demo.feature_detail.RecognitionData.zValues
import com.st.demo.intents.PredictionIntent
import com.st.demo.model.Sensor
import com.st.demo.model.SensorDataResponse
import com.st.demo.use_case.AddSensor
import com.st.demo.use_case.Classifica
import com.st.demo.use_case.RemoveSensor
import com.st.demo.wrappers.PredictionViewState
import com.st.demo.wrappers.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RecognitionViewModel @Inject constructor(
    private val prediction: Classifica,
    private val addSensor: AddSensor,
    private val removeSensor: RemoveSensor
) : ViewModel() {


    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun extractCoordinatesFromLoggable(dataString: String): Array<Float> {
        // Regex per estrarre i valori delle coordinate dell'accelerometro
        val xRegex = Regex("X\\s*=\\s*([-+]?\\d*\\.?\\d+)\\s*mg")
        val yRegex = Regex("Y\\s*=\\s*([-+]?\\d*\\.?\\d+)\\s*mg")
        val zRegex = Regex("Z\\s*=\\s*([-+]?\\d*\\.?\\d+)\\s*mg")

        val xResult = xRegex.find(dataString)
        val yResult = yRegex.find(dataString)
        val zResult = zRegex.find(dataString)

        val xValue = xResult?.groupValues?.get(1)?.toFloat() ?: 0.0f
        val yValue = yResult?.groupValues?.get(1)?.toFloat() ?: 0.0f
        val zValue = zResult?.groupValues?.get(1)?.toFloat() ?: 0.0f

        return arrayOf(xValue, yValue, zValue)
    }

    fun calculateStatistics(data: FloatArray): FloatArray {
        val mean = data.average().toFloat()
        val variance = data.map { (it - mean) * (it - mean) }.average().toFloat()
        val peakToPeak = data.maxOrNull()!! - data.minOrNull()!!

        var zeroCrossings = 0
        for (i in 0 until data.size - 1) {
            if (data[i] * data[i + 1] < 0) {
                zeroCrossings++
            }
        }

        return floatArrayOf(mean, variance, peakToPeak, zeroCrossings.toFloat())
    }

    //TODO aggiungi il modello qui.
   /*
    fun getRecognitionFromRandomForest(context: Context, stats: FloatArray): String {
        // Inizializza l'ambiente ONNX Runtime
        val env = OrtEnvironment.getEnvironment()

        // Carica il modello ONNX
        val inputStream = context.resources.openRawResource(R.raw.random_forest)
        val modelFile = File(context.cacheDir, "random_forest.onnx")

        // Scrivi il file nella cache
        FileOutputStream(modelFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        // Ora puoi passare il percorso del file come stringa a createSession
        val session = env.createSession(modelFile.absolutePath, OrtSession.SessionOptions())

        // Prepara i dati di input per il modello
        val inputTensor = OnnxTensor.createTensor(env, arrayOf(stats))

        // Esegui l'inferenza
        val results = session.run(mapOf("float_input" to inputTensor))

        // Ottieni il risultato come stringa
        val outputTensor = results[0].value as Array<*>
        val output = outputTensor[0]

        // Rilascia le risorse
        inputTensor.close()
        results.close()
        session.close()
        env.close()

        return output.toString()
    }
*/
    val randomForestActivity = mutableStateOf("")
    val lastActivity = mutableStateOf("")
    val startTime = mutableLongStateOf(0L)



    fun executeAccelerometerEffect(
        context: Context,
        dataString: String,

        deviceId: String
    ) {
        viewModelScope.launch {
            val (x, y, z) = extractCoordinatesFromLoggable(dataString)

            xValues.add(x)
            yValues.add(y)
            zValues.add(z)

            val nRows = 60

            if (xValues.size == nRows && yValues.size == nRows && zValues.size == nRows) {

                val xStats = calculateStatistics(xValues.toFloatArray())
                val yStats = calculateStatistics(yValues.toFloatArray())
                val zStats = calculateStatistics(zValues.toFloatArray())

                val stats = FloatArray(12)

                for (i in 0 until 4) {
                    val j = i * 3
                    stats[j] = xStats[i]
                    stats[j + 1] = yStats[i]
                    stats[j + 2] = zStats[i]
                }

               // randomForestActivity.value = getRecognitionFromRandomForest(context, stats)
                if (lastActivity.value == "") {
                    Log.d("Smartphone Recognition", "Timer started")
                    startTime.longValue = System.currentTimeMillis()
                }

                Log.d(
                    "Smartphone Recognition",
                    "Activity: ${randomForestActivity.value}"
                )
                if (lastActivity.value != randomForestActivity.value && startTime.longValue != 0L) {

                    val endTime = System.currentTimeMillis()
                    val duration =
                        (endTime - (startTime.longValue)) / 1000

                    if (duration > 0 && lastActivity.value != "") {
                        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
//                        val recognition = SensorData().apply {
//                            this.sensorDataId = deviceId
//                            this.deviceId = deviceId
//                            this.date = date
//                            this.duration = duration.toInt()
//                        }

                       // saveRecognition(jwtToken, recognition)
                        Log.d(
                            "Smartphone Recognition",
                            "Activity: ${lastActivity.value}, Duration: $duration seconds"
                        )
                    }
                    lastActivity.value = randomForestActivity.value
                    RecognitionData.updateActivity(randomForestActivity.value)

                    startTime.longValue = System.currentTimeMillis()
                    Log.d("Smartphone Recognition", "Timer started")
                }

                xValues.clear()
                yValues.clear()
                zValues.clear()
            }
        }
    }


    //REST API
    private val _state = MutableStateFlow(PredictionViewState())
    val state: StateFlow<PredictionViewState> = _state

    private val _intentChannel = Channel<PredictionIntent>(Channel.UNLIMITED)

    fun sendIntent(intent: PredictionIntent) {
        viewModelScope.launch {
            _intentChannel.send(intent)
        }
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
                is Sensor -> currentState.copy(isLoading = false, sensor = data as com.st.demo.model.Sensor?, message = null)
                is List<*> -> currentState.copy(isLoading = false, sensorList = data.filterIsInstance<Sensor>(), message = null)
                else -> currentState.copy(isLoading = false, message = "Unknown data type")
            }
        }
    }

}

