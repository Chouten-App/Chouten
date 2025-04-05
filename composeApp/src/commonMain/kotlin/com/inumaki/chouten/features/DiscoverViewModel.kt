package com.inumaki.chouten.features

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inumaki.chouten.Models.DiscoverSection
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

class DiscoverViewModel: ViewModel() {
    var state = mutableStateOf(
        LoadingState.INIT
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
            try {
                val result = withContext(Dispatchers.IO) {
                    Relay.discover() // Runs in a background thread
                }
                println("SECTIONS: $result")
                _sections.value = result
                state.value = LoadingState.SUCCESS
            } catch (e: Exception) {
                e.printStackTrace()
                state.value = LoadingState.ERROR
            }
        }
    }
}