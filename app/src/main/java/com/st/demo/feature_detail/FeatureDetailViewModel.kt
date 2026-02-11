/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.feature_detail

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.CalibrationStatus
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.compass.Compass
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtConfigCommands
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtendedFeatureCommand
import com.st.blue_sdk.features.extended.hs_datalog_config.HSDataLogConfig
import com.st.blue_sdk.features.extended.hs_datalog_config.request.HSDCmd
import com.st.blue_sdk.features.extended.hs_datalog_config.request.HSDataLogCommand
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.blue_sdk.services.calibration.CalibrationService

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import javax.inject.Inject

@HiltViewModel
class FeatureDetailViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val calibrationService: CalibrationService,
) : ViewModel()
{

    companion object {
        private val TAG = FeatureDetailViewModel::class.simpleName
    }

    val featureUpdates: State<FeatureUpdate<*>?>
        get() = _featureUpdates

    // NUOVO: Mappa per feature multiple
    private val _featureUpdatesMap = mutableStateMapOf<String, FeatureUpdate<*>?>()
    val featureUpdatesMap: Map<String, FeatureUpdate<*>?> get() = _featureUpdatesMap
    private val _featureUpdatesMapFlow = MutableStateFlow<Map<String, FeatureUpdate<*>>>(emptyMap())
    val featureUpdatesMapFlow: StateFlow<Map<String, FeatureUpdate<*>>> = _featureUpdatesMapFlow.asStateFlow()


    // NUOVO: Job multipli
    private val observeFeatureJobs = mutableMapOf<String, Job>()

    private val _featureUpdates = mutableStateOf<FeatureUpdate<*>?>(null)

    fun startCalibration(deviceId: String, featureName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blueManager.nodeFeatures(deviceId)
                .find { it.name == featureName && it.name == Compass.NAME }
                ?.let { feature ->
                    val isCalibrated =
                        calibrationService.startCalibration(nodeId = deviceId, feature = feature)

                    if (!isCalibrated.status) {
                        blueManager.getConfigControlUpdates(nodeId = deviceId).collect {
                            if (it is CalibrationStatus) {
                                Log.d(TAG, "calibration status ${it.status}")
                            }
                        }
                    }
                }
        }
    }

    // Il tuo nuovo metodo per calibrazione multipla
    fun startMultipleCalibration(deviceId: String, featureNames: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val features = blueManager.nodeFeatures(deviceId)
                .filter { it.name in featureNames }
            features.forEach { feature ->
                if (feature.name == Compass.NAME) { // solo se è Compass
                    val isCalibrated = calibrationService.startCalibration(nodeId = deviceId, feature = feature)
                    if (!isCalibrated.status) {
                        blueManager.getConfigControlUpdates(nodeId = deviceId)
                            .onEach { update ->
                                if (update is CalibrationStatus) {
                                    Log.d(TAG, "Calibration ${feature.name} status: ${update.status}")
                                }
                            }
                            .launchIn(this)
                    }
                }
            }
        }
    }



    private var observeFeatureJob: Job? = null

    fun observeFeature(featureName: String, deviceId: String) {
        observeFeatureJobs[featureName]?.cancel()

        blueManager.nodeFeatures(deviceId).find { it.name == featureName }?.let { feature ->
            observeFeatureJobs[featureName] =
                blueManager.getFeatureUpdates(nodeId = deviceId, features = listOf(feature))
                    .flowOn(Dispatchers.IO)
                    .onEach { update ->
                        _featureUpdatesMap[featureName] = update
                        _featureUpdatesMapFlow.value =
                            _featureUpdatesMap.toMap() as Map<String, FeatureUpdate<*>> // Emetti copia immutabile
                        _featureUpdates.value = update // mantieni per retrocompatibilità
                    }.launchIn(viewModelScope)
        }
    }


    fun observeMultipleFeatures(featureNames: List<String>, deviceId: String) {
        featureNames.forEach { featureName ->
            observeFeature(featureName, deviceId)
        }
    }


    fun sendExtendedCommand(featureName: String, deviceId: String) {

        viewModelScope.launch {

            val feature =
                blueManager.nodeFeatures(deviceId).find { it.name == featureName } ?: return@launch

            if (feature is ExtConfiguration) {
                val command = ExtConfigCommands.buildConfigCommand(ExtConfigCommands.BANKS_STATUS)
                val response =
                    blueManager.writeFeatureCommand(
                        deviceId,
                        ExtendedFeatureCommand(feature, command)
                    )
                response?.let {
                    Log.d(TAG, response.toString())
                    it.commandId
                }
            }

            if (feature is HSDataLogConfig) {
                val commands = listOf(
                    HSDCmd.buildHSDGetCmdDevice(),
                    HSDCmd.buildHSDGetCmdTagConfig(),
                )
                commands.forEach {
                    val response =
                        blueManager.writeFeatureCommand(
                            deviceId,
                            HSDataLogCommand(feature = feature, cmd = it)
                        )
                    response?.let {
                        it.commandId
                    }

                    delay(1000)
                }
            }

            if (feature is PnPL) {
                val commands = listOf(
                    PnPLCmd.ALL,
                    PnPLCmd.DEVICE_INFO,
                    PnPLCmd.LOG_CONTROLLER,
                    PnPLCmd.ES1,
                    PnPLCmd.ES2,
                    PnPLCmd.ES3,
                    PnPLCmd.ES4,
                    PnPLCmd.ES5
                )
                commands.forEach {
                    val response =
                        blueManager.writeFeatureCommand(
                            deviceId,
                            PnPLCommand(feature = feature, cmd = it)
                        )
                    response?.let {
                        it.commandId
                    }

                    delay(1000)
                }
            }
        }
    }

    fun disconnectFeature(deviceId: String, featureName: String) {
        observeFeatureJob?.cancel()
        _featureUpdates.value = null
        viewModelScope.launch {
            val features = blueManager.nodeFeatures(deviceId).filter { it.name == featureName }
            blueManager.disableFeatures(
                nodeId = deviceId,
                features = features
            )
        }
    }
    fun disconnectAllFeatures(deviceId: String) {
        observeFeatureJobs.values.forEach { it.cancel() }
        observeFeatureJobs.clear()
        _featureUpdatesMap.clear()
        _featureUpdates.value = null
    }
}