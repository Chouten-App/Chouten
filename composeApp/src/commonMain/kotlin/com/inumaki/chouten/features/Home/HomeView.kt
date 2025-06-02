package com.inumaki.chouten.features.Home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inumaki.chouten.features.App.components.TabView
import com.inumaki.chouten.features.Discover.DiscoverView
import com.inumaki.chouten.features.Discover.DiscoverViewModel
import com.inumaki.chouten.theme.ChoutenTheme
import com.inumaki.chouten.theme.LocalDeviceInfo

@Composable
fun HomeView(
    viewModel: HomeViewModel = viewModel { HomeViewModel() }
) {
    val init = viewModel.init.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Home")
        
        Text("Relay version: $(Relay.version)")
    }
}