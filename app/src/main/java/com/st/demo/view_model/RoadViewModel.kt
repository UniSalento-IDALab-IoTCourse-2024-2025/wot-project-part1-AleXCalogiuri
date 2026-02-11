package com.st.demo.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.demo.intents.RoadIntent
import com.st.demo.intents.UserIntent
import com.st.demo.model.Road
import com.st.demo.use_case.GetAllRoads
import com.st.demo.use_case.GetRoadByCity
import com.st.demo.wrappers.Resource
import com.st.demo.wrappers.RoadViewState
import com.st.demo.wrappers.UserViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoadViewModel @Inject constructor(
    val allRoads: GetAllRoads,
    val roadByCity: GetRoadByCity
): ViewModel()
{
    private val _state = MutableStateFlow(RoadViewState())
    val state: StateFlow<RoadViewState> = _state

    private val _intentChannel = Channel<RoadIntent>(Channel.UNLIMITED)

    fun sendIntent(intent: RoadIntent) {
        viewModelScope.launch {
            _intentChannel.send(intent)
        }
    }

    init {
        processIntents()
    }
    private fun processIntents() {
        viewModelScope.launch {
            _intentChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    is RoadIntent.GetAllRoad -> executeSuspend { allRoads() }
                    is RoadIntent.GetRoadByCity -> executeSuspend { roadByCity(intent.city) }
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
                is Road -> currentState.copy(isLoading = false, road = data, message = null)
                is List<*> -> currentState.copy(isLoading = false, roads = data.filterIsInstance<Road>(), message = null)

                else -> currentState.copy(isLoading = false, message = "Unknown data type")
            }
        }
    }
}