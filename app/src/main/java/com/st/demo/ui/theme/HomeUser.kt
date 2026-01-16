package com.st.demo.ui.theme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.st.demo.intents.UserIntent
import com.st.demo.model.SecureStorageManager
import com.st.demo.model.User
import com.st.demo.view_model.LoginViewModel


@Composable
fun HomeUser(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
    email: String,
    secureStorageManager: SecureStorageManager
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Esegui al caricamento della pagina
    LaunchedEffect(Unit) {
        val jwtToken = secureStorageManager.getJwt()
        if (jwtToken != null) {
            viewModel.sendIntent(UserIntent.getUser("Bearer $jwtToken", email))
        } else {
            Log.e("JWT", "Token non trovato")
        }
    }

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    )
    {
        // Contenuto principale scrollabile
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card informazioni utente
            state.user?.let { user ->
                //salva tutto così evitiamo chiamate è osceno e poco sicuro ma funziona
                LaunchedEffect(user) {
                    secureStorageManager.saveUser(user)
                    secureStorageManager.saveRole(user.role)
                }
                UserInfoCard(user = user)

                // Contenuto basato sul ruolo
                when (user.role) {
                    "ROLE_ADMIN" -> AdminContent(viewModel, secureStorageManager)
                    "ROLE_OPERATORE" -> OperatoreContent(viewModel, secureStorageManager)
                    "ROLE_USER" -> UserContent(viewModel, secureStorageManager,navController)
                }

            }

            ActionButton("Esci") {
                secureStorageManager.clear("jwt")
                secureStorageManager.clear("role")
                secureStorageManager.clearUser()
                // Naviga alla schermata di login e pulisci il back stack
                navController.navigate("welcome") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color =  Color(0xFF3A7BD5) )
            }
        }
    }
}

@Composable
fun UserInfoCard(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD).copy(alpha = 0.75f),
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Benvenuto: ${user.nome}",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Email: ${user.email}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nome: ${user.nome} ${user.cognome}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Role: ${user.role}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AdminContent(viewModel: LoginViewModel, secureStorageManager: SecureStorageManager) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE0B2).copy(alpha = 0.75f), // Colore diverso per admin
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pannello Amministratore",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Gestione notifiche amministrative")
            // TODO: Rest call su NotificheAdmin

            ActionButton("Vedi lista Enti") {
                //TODO getAllOperatori se c'è sennò sticazzi tanto devo solo fare rilevamenti qui e perderei tempo :3
            }
        }
    }
}

@Composable
fun OperatoreContent(viewModel: LoginViewModel, secureStorageManager: SecureStorageManager) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFC8E6C9).copy(alpha = 0.75f), // Colore diverso per operatore
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pannello Operatore",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Gestione enti e operazioni")
            // TODO: Rest call su EnteRestController

            ActionButton("Vedi mappa rilevamenti") {
                //TODO rest call su roadManagement
            }
        }
    }
}

@Composable
fun UserContent(viewModel: LoginViewModel,
                secureStorageManager: SecureStorageManager,
                navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD).copy(alpha = 0.75f),
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Informazioni Dispositivo",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Qui poi metto le info del dispositivo")
            Spacer(modifier = Modifier.height(16.dp))

            ActionButton("Aggiungi Device") {
                navController.navigate("list")
            }
            // TODO: Rest call su Notifiche
        }
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A7BD5))
    ) {
        Text(text)
    }
}

@Composable
fun UserItem(user: User) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = "Name: ${user.nome}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
