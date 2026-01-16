package com.st.demo.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.st.demo.R


@Composable
fun WelcomeScreen(navController: NavController){
    //val secureStorageManager = SecureStorageManager(LocalContext.current)
    /*
    LaunchedEffect(Unit) {
        secureStorageManager.clearJwt()
        secureStorageManager.clearUser()
        secureStorageManager.clearDeviceId()
    }

     */
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            Spacer(modifier = Modifier.height(50.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ){
                BasicText(
                    text = "Benvenuti nell'app Road Surface Monitor",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = Color(0xFF3A7BD5),
                            offset = Offset(10f, 10f),
                            blurRadius = 20f
                        )
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp).width(270.dp)
                )
                Button(
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F3F4)),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        navController.navigate("info")
                    },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img),
                        contentDescription = "Icona Informazioni",
                        modifier = Modifier.size(20.dp),
                    )
                }

            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ){
                Button(
                    onClick = {
                        navController.navigate("login")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A7BD5)),
                    modifier = Modifier
                        .padding(horizontal = 50.dp)
                        .weight(1f)
                        .height(45.dp)
                ){
                    Image(
                        painter = painterResource(id = R.drawable.img_1),
                        contentDescription = "Icona Login",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Accedi",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = {
                        navController.navigate("registration")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A7BD5)),
                    modifier = Modifier
                        .padding(horizontal = 50.dp)
                        .weight(1f)
                        .height(45.dp)

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_1),
                        contentDescription = "Icona Login",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Registrazione",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

