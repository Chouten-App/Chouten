package com.inumaki.chouten.features.App

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inumaki.relaywasm.models.DiscoverSection
import com.inumaki.chouten.Models.LoadingState
import com.inumaki.chouten.components.Repo_Regular
import com.inumaki.chouten.components.Repo_Solid
import com.inumaki.chouten.features.App.components.TabBarItem
import com.inumaki.chouten.features.Discover.DiscoverViewModel
import com.inumaki.chouten.relay.Relay
import com.inumaki.chouten.repo.RepoManager
import com.inumaki.chouten.wasm3.WasmRuntime
import com.inumaki.chouten.wasm3.loadWasmModuleBytes
import com.inumaki.relaywasm.Settings
import com.inumaki.relaywasm.SettingsManager
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Compass
import compose.icons.fontawesomeicons.solid.Compass
import compose.icons.fontawesomeicons.solid.Home
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel: ViewModel() {
    val homeTab = TabBarItem(title = "Home", selectedIcon = FontAwesomeIcons.Solid.Home, unselectedIcon = FontAwesomeIcons.Solid.Home)
    val discoverTab = TabBarItem(title = "Discover", selectedIcon = FontAwesomeIcons.Solid.Compass, unselectedIcon = FontAwesomeIcons.Regular.Compass)
    val reposTab = TabBarItem(title = "Repos", selectedIcon = Repo_Solid, unselectedIcon = Repo_Regular)

    // creating a list of all the tabs
    val tabBarItems = listOf(homeTab, discoverTab, reposTab)

    private var _settings = MutableStateFlow<Settings?>(null)
    var settings: StateFlow<Settings?> = _settings
        .onStart { getSettings() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            null
        )
    private val _init = MutableStateFlow<Boolean>(false)
    val init: StateFlow<Boolean> = _init
        .onStart { setupApp() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            false
        )

    private val _values = MutableStateFlow<Map<Pair<String, String>, Any>>(emptyMap())
    val values: StateFlow<Map<Pair<String, String>, Any>> = _values

    private fun setupApp() {
        viewModelScope.launch {
            _init.value = true

            // RepoManager.addRepo("https://github.com/inumakieu/OfficialRepo")
        }
    }

    private fun getSettings() {
        viewModelScope.launch {
            _settings.value = SettingsManager.loadSettings()
        }
    }

    fun setSettingInGroup(groupId: String, key: String, value: Any) {
        _values.value = HashMap(_values.value).apply { put(groupId to key, value) }
    }
}