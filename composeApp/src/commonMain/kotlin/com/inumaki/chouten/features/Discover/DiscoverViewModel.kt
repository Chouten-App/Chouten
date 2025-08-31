package com.inumaki.chouten.features.Discover

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inumaki.chouten.Models.LoadingState
import com.inumaki.chouten.relay.Relay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

import com.inumaki.relaywasm.LogManager
import com.inumaki.relaywasm.WasmRuntime
import com.inumaki.relaywasm.models.ChoutenError
import com.inumaki.relaywasm.models.DiscoverSection
import com.inumaki.relaywasm.models.RustResult
import com.inumaki.relaywasm.models.RustResult.Ok
import com.inumaki.relaywasm.models.RustResult.Err

class DiscoverViewModel(
    private val wasmRuntime: WasmRuntime
): ViewModel() {

    var wasmModule = wasmRuntime.loadModule()

    var state = mutableStateOf(
        LoadingState.LOADING
    )

    private val _sections = MutableStateFlow<List<DiscoverSection>?>(null)
    val sections: StateFlow<List<DiscoverSection>?> = _sections
        .onStart { loadData() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            emptyList()
        )

    private fun loadData() {
        viewModelScope.launch {
            state.value = LoadingState.LOADING
            
            val resultValue = withContext(Dispatchers.IO) {
                wasmModule.callFunction("discover_wrapper")
            }

            println(resultValue)
            val resultJson = Json.decodeFromString<RustResult<List<DiscoverSection>, ChoutenError>>(resultValue)

            when (resultJson) {
                is Ok -> {
                    val sections = resultJson.value  // <- access `value` here
                    
                    if (sections.isEmpty()) {
                        state.value = LoadingState.EMPTY
                        return@launch
                    }

                    _sections.value = sections
                    state.value = LoadingState.SUCCESS
                }
                is Err -> {
                    val error = resultJson.error     // <- access `error` here
                    println("Got error: $error")
                    state.value = LoadingState.ERROR
                    return@launch
                }
            }
            
        }
    }
}