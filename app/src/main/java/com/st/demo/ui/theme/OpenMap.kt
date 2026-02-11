import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.demo.intents.RoadIntent
import com.st.demo.model.Road
import com.st.demo.view_model.RoadViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OpenMap(
    viewModel: RoadViewModel = hiltViewModel(),
    city: String
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Carica i dati al primo render
    LaunchedEffect(Unit) {
        if(city!=""){
            viewModel.sendIntent(RoadIntent.GetRoadByCity(city))
        }else{
            viewModel.sendIntent(RoadIntent.GetAllRoad)
        }

    }

    // Mostra errori
    state.message?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mappa OSM
        AndroidView(
            factory = { ctx ->
                // Configurazione osmdroid
                Configuration.getInstance().load(
                    ctx,
                    PreferenceManager.getDefaultSharedPreferences(ctx)
                )
                Configuration.getInstance().userAgentValue = ctx.packageName

                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(true)

                    // Centra su Cavallino
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(40.30670, 18.20379))
                }
            },
            update = { mapView ->
                // Aggiorna i marker quando cambiano le strade
                if (state.roads?.isNotEmpty() == true) {
                    addMarkersToMap(mapView, state.roads!!)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun addMarkersToMap(mapView: MapView, roads: List<Road>) {
    // Pulisci marker precedenti
    mapView.overlays.clear()

    roads.forEach { road ->
        road.segmenti?.forEach { segmento ->
            segmento.inizio?.let { punto ->
                val coords = punto.coordinates

                val marker = Marker(mapView)
                marker.position = GeoPoint(coords[1], coords[0])
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                marker.title = road.via
                marker.snippet = buildString {
                    append("Città: ${road.citta}\n")
                    append("Priorità: ${mapPriority(segmento.prioritaManutenzione)}\n")
                    append("CAP: ${road.cap}")
                    road.statoConservazione?.let {
                        append("\nStato: $it")
                    }
                }

                customizeMarkerByPriority(marker,
                    segmento.prioritaManutenzione,
                    mapView.context
                )

                mapView.overlays.add(marker)
            }
        }
    }

    // Centra sulla prima strada
    if (roads.isNotEmpty()) {
        roads.firstOrNull()?.segmenti?.firstOrNull()?.inizio?.coordinates?.let { coords ->
            mapView.controller.setCenter(GeoPoint(coords[1], coords[0]))
        }
    }

    mapView.invalidate()
}


private fun customizeMarkerByPriority(
    marker: Marker,
    priority: Int?,
    context: Context
) {
    val color = when(priority) {
        4 -> Color.RED           // Alta priorità - Rosso
        3 -> Color.rgb(255, 165, 0)  // Media priorità - Arancione
        2 -> Color.YELLOW        // Bassa priorità - Giallo
        1 -> Color.GREEN         // Minima priorità - Verde
        else -> Color.GRAY       // Priorità sconosciuta - Grigio
    }

    // Prendi l'icona default e applicale il colore
    val drawable = ContextCompat.getDrawable(
        context,
        org.osmdroid.library.R.drawable.marker_default
    )?.mutate()  // mutate() è importante per non cambiare tutti i marker

    drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    marker.icon = drawable
}

private fun mapPriority(priorita: Int?): String {
    val result = when(priorita) {
        4 -> "Alta priorità"
        3 -> "Media priorità"
        2 -> "Bassa priorità"
        else -> {"In salute"}
    }
    return result
}
