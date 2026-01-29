/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.st.demo.audio.AudioScreen
import com.st.demo.device_detail.BleDeviceDetail
import com.st.demo.device_list.BleDeviceList
import com.st.demo.feature_detail.FeatureDetail
import com.st.demo.feature_detail.PotholeDetection
import com.st.demo.feature_detail.SensorTileAndSmartphone
import com.st.demo.model.SecureStorageManager
import com.st.demo.ui.theme.HomeUser
import com.st.demo.ui.theme.InfoApp
import com.st.demo.ui.theme.Login
import com.st.demo.ui.theme.Registration
import com.st.demo.ui.theme.RegistrationSuccess
import com.st.demo.ui.theme.StDemoTheme
import com.st.demo.ui.theme.WelcomeScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
            ) {
                // Immagine di background
                Image(
                    painter = painterResource(id = R.drawable.road),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds // o ContentScale.FillBounds a seconda delle tue esigenze
                )


                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()

    StDemoTheme {
        NavHost(
            navController = navController, startDestination = "list") {

            composable(route = "welcome"){
                WelcomeScreen(navController = navController)
            }
            composable(route = "info") {
                InfoApp(
                    navController = navController
                )
            }

            composable(route = "login"){
                Login(
                    viewModel = hiltViewModel(),
                    navController = navController
                )
            }

            composable(route = "registration") {
                Registration(
                    viewModel = hiltViewModel(),
                    navController = navController
                )
            }

            composable(
                route = "home/{email}",
                arguments = listOf(
                    navArgument("email") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email")!!

                HomeUser(
                    navController = navController,
                    email = email,
                    viewModel = hiltViewModel(),
                    secureStorageManager = SecureStorageManager(context = LocalContext.current)
                )
            }

            composable(route= "okRegistrazione"){
                RegistrationSuccess(
                    navController = navController,
                    viewModel = hiltViewModel()
                )
            }




            composable(route = "list") {
                BleDeviceList(
                    viewModel = hiltViewModel(),
                    navController = navController
                )
            }

            composable(
                route = "detail/{deviceId}",
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                    BleDeviceDetail(
                        viewModel = hiltViewModel(),
                        navController = navController,
                        deviceId = deviceId
                    )
                }
            }

            composable(
                route = "feature/{deviceId}/{featureName}",
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType },
                    navArgument("featureName") { type = NavType.StringType })
            ) { backStackEntry ->
                backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                    backStackEntry.arguments?.getString("featureName")?.let { featureName ->
                        FeatureDetail(
                            viewModel = hiltViewModel(),
                            navController = navController,
                            deviceId = deviceId,
                            featureName = featureName
                        )
                    }
                }
            }

            composable(
                route = "feature/{deviceId}/{featureName}/HARSmartphone",
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType },
                    navArgument("featureName") { type = NavType.StringType })
            ) { backStackEntry ->
                backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                    backStackEntry.arguments?.getString("featureName")?.let { featureName ->
                        SensorTileAndSmartphone(
                            featureViewModel = hiltViewModel(),
                            recognitionViewModel = hiltViewModel(),
                            navController = navController,
                            deviceId = deviceId,
                            featureName = featureName
                        )
                    }
                }
            }

            composable(
                route = "feature/{deviceId}/{featureName}/mlc",
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType },
                    navArgument("featureName") { type = NavType.StringType })
            ) { backStackEntry ->
                backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                    backStackEntry.arguments?.getString("featureName")?.let { featureName ->
                        PotholeDetection(
                            viewModel = androidx.hilt.navigation.compose.hiltViewModel(),
                            viewModelBle = hiltViewModel(),
                            navController = navController,
                            deviceId = deviceId,
                            featureName = featureName
                        )
                    }
                }
            }

            composable(
                route = "audio/{deviceId}",
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                    AudioScreen(
                        viewModel = hiltViewModel(),
                        navController = navController,
                        deviceId = deviceId
                    )
                }
            }
        }
    }
}