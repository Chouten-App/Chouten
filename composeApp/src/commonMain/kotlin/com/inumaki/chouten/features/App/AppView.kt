package com.inumaki.chouten.features.App

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
import com.inumaki.chouten.features.Discover.HomeView
import com.inumaki.chouten.features.Discover.HomeViewModel
import com.inumaki.chouten.theme.ChoutenTheme
import com.inumaki.chouten.theme.LocalDeviceInfo

@Composable
fun AppView(
    viewModel: AppViewModel = viewModel { AppViewModel() }
) {
    val init = viewModel.init.collectAsState()

    val navController = rememberNavController()
    val discoverViewModel = viewModel { DiscoverViewModel() }
    val homeViewModel = viewModel { HomeViewModel() }

    ChoutenTheme {
        Scaffold(
            backgroundColor = ChoutenTheme.colors.background,
            contentColor = ChoutenTheme.colors.fg,
            bottomBar = {
                if (!LocalDeviceInfo.current.isTablet) {
                    TabView(
                        viewModel.tabBarItems,
                        navController
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                NavHost(
                    navController = navController,
                    startDestination = viewModel.discoverTab.title,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    composable(viewModel.homeTab.title) {
                        HomeView(viewModel = homeViewModel)
                    }

                    composable(viewModel.discoverTab.title) {
                        DiscoverView(viewModel = discoverViewModel)
                    }

                    composable(viewModel.reposTab.title) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Repos")
                        }
                    }
                }

                if (LocalDeviceInfo.current.isTablet) {
                    TabView(
                        viewModel.tabBarItems,
                        navController
                    )
                }
            }
        }
    }
}