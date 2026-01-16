/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
@file:OptIn(ExperimentalPermissionsApi::class)

package com.st.demo.device_list

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.st.demo.R
import kotlinx.coroutines.delay

fun ByteArray.toHexString() = joinToString("") { it.toString(16).padStart(2, '0') }

@Composable
fun LeDevice(
    modifier: Modifier = Modifier,
    timestamp: Long,
    deviceName: String,
    deviceAddress: String,
    protocolDeviceId: Int,
    protocolFwId: Int,
    protocolId: Int,
    payloadData: String
)
{
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 10.dp
    ) {

        var isAnimated by remember { mutableStateOf(false) }

        LaunchedEffect(key1 = timestamp) {
            if (timestamp!=0L) {
                isAnimated = true
                delay(100)
                isAnimated = false
            }
        }

        val animatedColor by animateColorAsState(
            if (isAnimated) {
                Color.Cyan
            } else {
                Color.Black
            },
            label = "color"
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = "Name = $deviceName"
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = "Address = $deviceAddress"
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = "DeviceID = 0x${
                    Integer.toHexString(protocolDeviceId)
                        .padStart(2, '0')
                }"
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = "firmwareId = 0x${
                    Integer.toHexString(protocolFwId)
                        .padStart(2, '0')
                }"
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = "protocolId = 0x${
                    Integer.toHexString(protocolId)
                        .padStart(4, '0')
                }"
            )
            Row {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "payloadData = "
                )
                Text(
                    modifier = Modifier.padding(4.dp),
                    color = animatedColor,
                    text = "0x${
                        payloadData
                    }"
                )
            }
        }
    }
}

@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalMaterialApi::class
)
@SuppressLint("MissingPermission")
@Composable
fun BleDeviceList(
    viewModel: BleDeviceListViewModel, navController: NavHostController
)
{

    var doNotShowRationale by rememberSaveable {
        mutableStateOf(false)
    }

    val locationPermissionState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        else listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    if (locationPermissionState.allPermissionsGranted) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .scrollable(
                    state = scrollState, orientation = Orientation.Vertical
                )
        ) {
            val devices = viewModel.scanBleDevices.collectAsState()
            val devicesLe = viewModel.scanBleLeDevices.collectAsState()
            val isBleScanning by viewModel.isLEScanning.collectAsState()
            val isRefreshing by viewModel.isLoading.collectAsState()
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isRefreshing,
                onRefresh = { viewModel.startScan(true) }
            )

            Box(
                modifier = Modifier.padding(16.dp)
            ){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD).copy(alpha = 0.75f)
                    )
                )
                {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    )
                    {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_bluetooth),
                                contentDescription = "Icona Bluetooth",
                                modifier = Modifier.size(44.dp),
                            )

                            // Pulsante con effetto glass
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.3f))
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.startScan(false) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aggiorna",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Dispositivi Bluetooth trovati:",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (devices.value.isEmpty() && devicesLe.value.second.isEmpty() && isRefreshing.not()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ricerca device in corso..",
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if(isBleScanning) {
                                stringResource(R.string.st_le_deviceList_title)
                            } else {
                                stringResource(R.string.st_deviceList_title)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(pullRefreshState)
                    ){
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ){
                            itemsIndexed(devices.value) { index, item ->
                                // Card con effetto glass
                                Card(
                                    onClick = { navController.navigate("detail/${item.device.address}") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .padding(vertical = 6.dp)
                                        .fillMaxWidth()
                                        .shadow(8.dp, RoundedCornerShape(16.dp))
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 16.dp)
                                        ) {
                                            Text(
                                                text = item.device.name,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.device.address,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Pulsante dettagli con effetto glass
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.3f))
                                                .border(
                                                    width = 1.dp,
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    navController.navigate("detail/${item.device.address}")
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Dettagli",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        PullRefreshIndicator(
                            refreshing = isRefreshing,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter),
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            scale = true
                        )
                    }
                }
            }

            LaunchedEffect(Unit) {
                viewModel.startScan(true)
            }
        }

    } else {
        if (doNotShowRationale) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text("Feature not available")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text("The Location and Record Audio is important for this app. Please grant the permission.")

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        modifier = Modifier.weight(0.5f),
                        onClick = { locationPermissionState.launchMultiplePermissionRequest() }) {
                        Text("Ok!")
                    }

                    Spacer(Modifier.width(4.dp))

                    Button(
                        modifier = Modifier.weight(0.5f),
                        onClick = { doNotShowRationale = true }) {
                        Text("Nope")
                    }
                }
            }
        }
    }
}