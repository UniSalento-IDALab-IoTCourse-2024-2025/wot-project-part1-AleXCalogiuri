package com.st.demo.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.st.demo.R


@Composable
fun InfoApp(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f) // Occupa lo spazio disponibile in alto
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD).copy(alpha = 0.75f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img),
                                contentDescription = "Icona informazioni",
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "A cosa serve?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "Questa applicazione utilizza una SensorTile.Box PRO per monitorare lo stato di salute del manto stradale" +
                                    ",la rilevazione segue due stati:",
                            fontSize = 17.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 50.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ActivityList()
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4).copy(alpha = 0.75f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img),
                                contentDescription = "Icona funzionalità",
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Quali funzionalità offre?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Rilevamento attività in tempo reale tramite Anomaly detection.\n" +
                                    "• Connessione rapida ai dispositivi Bluetooth.\n" +
                                    "• Visualizzazione intuitiva e moderna.",
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 56.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }


    }
}

@Composable
fun ActivityList() {
    Column(modifier = Modifier.padding(start = 56.dp)) {
        ActivityItem(text = "Good", iconResId = R.drawable.car_road)
        ActivityItem(text = "Pothole detected", iconResId = R.drawable.pothole)
    }
}

@Composable
fun ActivityItem(text: String, iconResId: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = "$text icon",
            modifier = Modifier.size(42.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 16.sp, color = Color.Black)
    }
}
