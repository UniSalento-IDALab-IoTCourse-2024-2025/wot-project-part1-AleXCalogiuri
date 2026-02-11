package com.st.demo.feature_detail

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.st.blue_sdk.features.gyroscope.GyroscopeInfo
import com.st.demo.R
import com.st.demo.device_detail.BleDeviceDetailViewModel
import com.st.demo.intents.PredictionIntent
import com.st.demo.model.SensorData
import com.st.demo.view_model.RecognitionViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.get

@SuppressLint("MissingPermission")
@Composable
fun PotholeDetection(
    navController: NavHostController,
    viewModel: FeatureDetailViewModel,
    recognitionViewModel: RecognitionViewModel,
    deviceId: String,
    featureName: String
)
{
    val context = LocalContext.current
    val appContext = LocalContext.current.applicationContext
    val backHandlingEnabled by remember { mutableStateOf(true) }


    val mlcStatus = remember { mutableStateOf("UNKNOWN") }
    val mlcCode = remember { mutableIntStateOf(-1) }
    var buttonEnabled by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    //  Stati sensori
    val accelerometerData = remember { mutableStateOf<Triple<Float, Float, Float>?>(null) }
    val gyroscopeData = remember { mutableStateOf<Triple<Float, Float, Float>?>(null) }
    val latestGyroData = remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    val address = remember { mutableStateOf(String()) }
    val locationData = remember { mutableStateOf<Location?>(null) }

    //  Log delle anomalie
    val potholeLog = remember { mutableStateListOf<PotholeEvent>() }

    // Sensor Manager
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Location Manager
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    //  Setup accelerometro
    DisposableEffect(Unit) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val accelerometerListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    accelerometerData.value = Triple(it.values[0], it.values[1], it.values[2])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }



        sensorManager.registerListener(
            accelerometerListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )


        onDispose {
            sensorManager.unregisterListener(accelerometerListener)

        }
    }

    //  Setup GPS
    DisposableEffect(Unit) {
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationData.value = location
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L, // Ogni 1 secondo
                1f,    // Ogni 1 metro
                locationListener
            )
        } catch (e: SecurityException) {
            Log.e("PotholeDetection", "GPS permission denied", e)
        }

        onDispose {
            locationManager.removeUpdates(locationListener)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startCalibration(deviceId, featureName)
        //viewModel.startCalibration(deviceId,"Gyroscope")
    }
    LaunchedEffect(Unit) {
        recognitionViewModel.start()
    }

    BackHandler(enabled = backHandlingEnabled) {
        viewModel.disconnectFeature(deviceId = deviceId, featureName = featureName)
        viewModel.disconnectFeature(deviceId=deviceId, featureName = "Gyroscope")
        navController.popBackStack()
    }

    val featureUpdate = viewModel.featureUpdates.value
    val featureUpdatesMap by viewModel.featureUpdatesMapFlow.collectAsState()

    //  RILEVA ANOMALIA E SALVA TUTTI I DATI
    LaunchedEffect(featureUpdatesMap["Machine Learning Core"]) {
        featureUpdatesMap["Machine Learning Core"]?.let { update ->
            val dataString = update.toString()
            try {
                val mlc0Regex = Regex("MLC_0\\s*=\\s*(\\d+)")
                val match = mlc0Regex.find(dataString)
                if (match != null) {
                    val value = match.groupValues[1].toInt()
                    mlcCode.intValue = value
                    val newStatus = when (value) {
                        255 -> "ANORMAL"
                        0 -> "NORMAL"
                        else -> "UNKNOWN"
                    }

                    if (newStatus == "ANORMAL" && mlcStatus.value != "ANORMAL") {
                        buttonEnabled = true

                        address.value = locationData.value?.let {
                            recognitionViewModel.getStreetName(appContext, it.latitude, it.longitude)
                        } ?: "GPS non disponibile"
                        Log.d("PotholeDetection", "BUCA RILEVATA!")
                        Log.d("PotholeDetection", "Giroscopio: ${latestGyroData.value}")
                    }
                    mlcStatus.value = newStatus
                }
            } catch (e: Exception) {
                Log.e("PotholeDetection", "Error parsing MLC data", e)
            }
        }
    }

// Per Giroscopio
    LaunchedEffect(featureUpdatesMap["Gyroscope"]) {
        featureUpdatesMap["Gyroscope"]?.let { update ->
            try {
                Log.v("PotholeDetection", "GYRO RAW: ${update.toString()}")
                // Usa la funzione corretta per il giroscopio
                latestGyroData.value = recognitionViewModel.extractCoordinatesFromLoggable(update.toString())
                Log.v("PotholeDetection", "GIROSCOPIO: ${latestGyroData.value}")

            } catch (e: Exception) {
                Log.e("PotholeDetection", "Error parsing Gyroscope data", e)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD).copy(alpha = 0.75f))
            .padding(16.dp)
    ) {
        Text(
            text = "Pothole Detection",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = Color(0xFF374151),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        //  Card con dati sensori in tempo reale
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        )
        {
            Column(modifier = Modifier.padding(12.dp))
            {
                Text(
                    text = " Sensori in tempo reale",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Accelerometro
                accelerometerData.value?.let { (x, y, z) ->
                    Text(
                        text = " Accelerometro: X:%.2f Y:%.2f Z:%.2f".format(x, y, z),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }

                // Giroscopio
                gyroscopeData.value?.let { (x, y, z) ->
                    Text(
                        text = " Giroscopio: X:%.2f Y:%.2f Z:%.2f".format(x, y, z),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }

                // GPS
                locationData.value?.let { location ->
                    Text(
                        text = " GPS: %.6f, %.6f (±%.1fm)".format(
                            location.latitude,
                            location.longitude,
                            location.accuracy
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                } ?: Text(
                    text = " GPS: In attesa segnale...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Text(
                    text = " Via: ${address.value}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                //GIROSCOPIO
                val (gyroX, gyroY, gyroZ) = latestGyroData.value
                Text(
                    text = " Giroscopio: X:%.2f Y:%.2f Z:%.2f".format(gyroX, gyroY, gyroZ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = " Buche rilevate: ${potholeLog.size}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Red
                )

            }
        }

        Text(
            text = "Stato: ${mlcStatus.value}",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            ),
            color = if (mlcStatus.value == "ANORMAL") Color.Red else Color.Green,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        )
        {
            ActivityCard(
                activityName = "Normal Road",
                imageRes = R.drawable.car_road,
                isSelected = mlcStatus.value == "NORMAL",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            ActivityCard(
                activityName = "Pothole!",
                imageRes = R.drawable.pothole,
                isSelected = mlcStatus.value == "ANORMAL",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        resultMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = if (message.contains("Errore")) Color.Red else Color(0xFF2E7D32),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        //questo per aspettare la response del server
        val coroutineScope = rememberCoroutineScope()
        val predictionState by recognitionViewModel.state.collectAsState()
        //Bottone per inviare segnalazione
        ActionButtonPothole("Invia Segnalazione", buttonEnabled) {
            buttonEnabled = false
            recognitionViewModel.resetState()
            // Prepare data
            val (accX, accY, accZ) = accelerometerData.value ?: Triple(0.2f, 0.2f, 0f)
            val (gyroX, gyroY, gyroZ) = latestGyroData.value
            val lat = locationData.value?.latitude ?: 0.0
            val lon = locationData.value?.longitude ?: 0.0


            val request = SensorData(

                1,
                accX.toDouble(),
                accY.toDouble(),
                gyroX.toDouble(),
                gyroY.toDouble(),
                gyroZ.toDouble(),
                lat,
                lon,
                1,
                address.value
            )


            Log.d("PotholeButton", "Sending prediction request: $request")
            recognitionViewModel.sendIntent(PredictionIntent.predict(request))

            coroutineScope.launch {
                try {
                    // Timeout per evitare attese infinite
                    withTimeout(10000) { // 10 secondi
                        val state = recognitionViewModel.state
                            .first { !it.isLoading && (it.predictionResponse != null || it.message != null) }

                        when {
                            // Caso successo
                            state.predictionResponse != null -> {
                                val response = state.predictionResponse
                                val potholeEvent = PotholeEvent(
                                    timestamp = System.currentTimeMillis(),
                                    classificazione = response.classificazione,
                                    location = locationData.value
                                )
                                potholeLog.add(potholeEvent)
                                savePotholeEvent(context, potholeEvent)
                                resultMessage = "Segnalazione salvata: ${response.classificazione}"
                                Log.d("PotholeButton", "Success: ${response.classificazione}")
                            }

                            // Caso errore
                            state.message != null -> {
                                resultMessage = "Errore: ${state.message},Data: $request"
                                Log.e("PotholeButton", "Error: ${state.message}")
                                buttonEnabled = true // Riabilita il pulsante in caso di errore
                            }

                            // Caso imprevisto
                            else -> {
                                resultMessage = "Errore sconosciuto"
                                Log.e("PotholeButton", "Unknown error state")
                                buttonEnabled = true
                            }
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    resultMessage = "Timeout: la richiesta ha impiegato troppo tempo"
                    Log.e("PotholeButton", "Timeout waiting for prediction", e)
                    buttonEnabled = true
                } catch (e: Exception) {
                    resultMessage = "Errore: ${e.message}"
                    Log.e("PotholeButton", "Exception during prediction", e)
                    buttonEnabled = true
                }
            }
        }

        // Nella UI, sotto il pulsante:
        if (predictionState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }

        predictionState.message?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }
    }

    LaunchedEffect(true) {
        viewModel.observeFeature(deviceId = deviceId, featureName = featureName)
        viewModel.observeFeature(deviceId= deviceId, featureName = "Gyroscope")

        viewModel.sendExtendedCommand(featureName = featureName, deviceId = deviceId)
        viewModel.sendExtendedCommand(featureName = "Gyroscope", deviceId = deviceId)
    }
}

// Data class per salvare eventi
data class PotholeEvent(
    val timestamp: Long,
    val classificazione: String,
    val location: Location?
)

// Funzione per salvare su file
fun savePotholeEvent(context: Context, event: PotholeEvent) {
    try {
        val file = File(context.getExternalFilesDir(null), "pothole_log.csv")
        val exists = file.exists()

        FileWriter(file, true).use { writer ->
            // Header se file nuovo
            if (!exists) {
                writer.append("Timestamp,Date,classificazione,Latitude,Longitude,Accuracy\n")
            }

            // Dati
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(event.timestamp))

            writer.append("${event.timestamp},")
            writer.append("$date,")
            writer.append("${event.classificazione},")
            writer.append("${event.location?.latitude ?: ""},")
            writer.append("${event.location?.longitude ?: ""},")
            writer.append("${event.location?.accuracy ?: ""}\n")
        }

        Log.d("PotholeDetection", " Event saved to: ${file.absolutePath}")
    } catch (e: Exception) {
        Log.e("PotholeDetection", " Error saving event", e)
    }
}
@Composable
fun ActivityCard(
    activityName: String,
    imageRes: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFF42A5F5) else Color.Transparent

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .padding(8.dp)
            .border(4.dp, borderColor, RoundedCornerShape(16.dp))
            .height(200.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color.Unspecified
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = activityName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = activityName,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}


@Composable
fun ActionButtonPothole(text: String,isEnabled: Boolean, onClick: () -> Unit) {
    //var isClicked by remember { mutableStateOf(false) }
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3A7BD5),
            disabledContainerColor = Color.Gray
        )
    ) {
        Text(text)
    }
}