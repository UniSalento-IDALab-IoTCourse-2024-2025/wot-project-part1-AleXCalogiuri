package com.st.demo.feature_detail

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.st.demo.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("MissingPermission")
@Composable
fun SensorTileAndSmartphone(
    navController: NavHostController,
    featureViewModel: FeatureDetailViewModel,
    recognitionViewModel: RecognitionViewModel,
    deviceId: String,
    featureName: String
){
    val potHoleRecognitionIndex = 0
    val accelerometerIndex = 1
    val gyroscopeIndex = 2
    val backHandlingEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        featureViewModel.startCalibration(deviceId, featureName)
        featureViewModel.startCalibration(deviceId, "Accelerometer")
    }

    BackHandler(enabled = backHandlingEnabled) {
        featureViewModel.disconnectFeature(
            deviceId = deviceId,
            featureName = featureName,

        )
        featureViewModel.disconnectFeature(
            deviceId = deviceId,
            featureName = "Accelerometer",

        )

        navController.popBackStack()
    }

    val anomalyDetect = featureViewModel.featureUpdates.value?.featureName?.get(potHoleRecognitionIndex)
    val accelerometerFeature = featureViewModel.featureUpdates.value?.featureName?.get(accelerometerIndex)
    val gyroscopeFeature = featureViewModel.featureUpdates.value?.featureName?.get(gyroscopeIndex)

    val currentActivity = remember { mutableStateOf("") }

    LaunchedEffect(anomalyDetect) {
        val data = anomalyDetect.toString()
        currentActivity.value = data

    }


    val context = LocalContext.current

    LaunchedEffect(accelerometerFeature) {
        accelerometerFeature?.let { feature ->
            val dataString = feature.toString()
            recognitionViewModel.executeAccelerometerEffect(
                context = context,
                dataString = dataString,
                deviceId = deviceId
            )
        }
    }

    Column(
        modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .padding(16.dp))
    {
        Text(
            text = "HAR su SensorTile.box PRO e Smartphone",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = Color(0xFF374151),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = "SensorTile.box PRO:",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = {
                // Salvo l'ultimo riconoscimento
                if (recognitionViewModel.startTime.longValue != 0L && RecognitionData.activity.value != "") {

                    val endTime = System.currentTimeMillis()
                    val duration = (endTime - recognitionViewModel.startTime.longValue) / 1000
                    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
//                    val recognition = InsertPrediction().apply {
//                        this.prediction = RecognitionData.activity.value
//                        this.deviceId = deviceId
//                        this.date = date
//                        this.duration = duration.toInt()
//                    }
                    //recognitionViewModel.saveRecognition(jwtToken!!, recognition)

                    RecognitionData.xValues.clear()
                    RecognitionData.yValues.clear()
                    RecognitionData.zValues.clear()
                    RecognitionData.updateActivity("")
                }
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F3F4)),
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Icona Indietro",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Indietro",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}