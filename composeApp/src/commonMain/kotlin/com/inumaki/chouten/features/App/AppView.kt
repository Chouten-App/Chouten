package com.inumaki.chouten.features.App

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Colors
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.ui.graphics.SolidColor
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.inumaki.chouten.components.NetworkImage
import com.inumaki.chouten.features.App.components.TabView
import com.inumaki.chouten.features.Discover.DiscoverView
import com.inumaki.chouten.features.Discover.DiscoverViewModel
import com.inumaki.chouten.features.Home.HomeView
import com.inumaki.chouten.features.Home.HomeViewModel
import com.inumaki.chouten.helpers.topBorder
import com.inumaki.chouten.theme.ChoutenTheme
import com.inumaki.chouten.theme.LocalDeviceInfo
import com.inumaki.relaywasm.Settings
import com.inumaki.relaywasm.WasmRuntime
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalHazeApi::class)
@Suppress("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AppView(
    viewModel: AppViewModel = viewModel { AppViewModel() },
    wasmRuntime: WasmRuntime
) {
    val init = viewModel.init.collectAsState()
    val settings = viewModel.settings.collectAsState()
    val values by viewModel.values.collectAsState()

    val navController = rememberNavController()
    val discoverViewModel = viewModel { DiscoverViewModel(
        wasmRuntime
    ) }
    val homeViewModel = viewModel { HomeViewModel() }

    val hazeState = remember { HazeState() }

    var visible by remember { mutableStateOf(false) }

    val blackOpacity by animateFloatAsState(
        targetValue = if (visible) 0.2f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = 2000,
            easing = CubicBezierEasing(0.1f, 1f, 0.2f, 1f)
        ),
        label = "blackOpacity"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 0.9f else 1f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = 2000,
            easing = CubicBezierEasing(0.1f, 1f, 0.2f, 1f)
        ),
        label = "scale"
    )

    val radius by animateDpAsState(
        targetValue = if (visible) 12.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = 2000,
            easing = CubicBezierEasing(0.1f, 1f, 0.2f, 1f)
        ),
        label = "radius"
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val screenName = backStackEntry?.destination?.route

    // LaunchedEffect(Unit) { visible = true }

    ChoutenTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .clip(RoundedCornerShape(radius)),
                contentAlignment = Alignment.TopCenter
            ) {
                Scaffold(
                    backgroundColor = ChoutenTheme.colors.background,
                    contentColor = ChoutenTheme.colors.fg,
                    bottomBar = {
                        if (!LocalDeviceInfo.current.isTablet) {
                            TabView(
                                viewModel.tabBarItems,
                                navController,
                                hazeState
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
                                .hazeSource(hazeState)
                        ) {
                            composable(viewModel.homeTab.title) {
                                HomeView(viewModel = homeViewModel)
                            }

                            composable(viewModel.discoverTab.title) {
                                DiscoverView(viewModel = discoverViewModel, hazeState = hazeState)
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
                                navController,
                                hazeState
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .hazeEffect(state = hazeState) {
                            backgroundColor = Color(0xFF0C0C0C)
                            blurEnabled = true
                            blurRadius = 40.dp
                            progressive = HazeProgressive.verticalGradient(
                                startIntensity = 1f,
                                endIntensity = 0f
                            )
                        }
                        .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 50.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .hazeEffect(state = hazeState) {
                            backgroundColor = Color(0xFF0C0C0C)
                            blurRadius = 40.dp
                            inputScale = HazeInputScale.Auto
                            progressive = HazeProgressive.verticalGradient(
                                startIntensity = 1f,
                                endIntensity = 0f
                            )
                        }
                        .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 50.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    when (screenName) {
                        "Discover" -> {
                            Text(
                                text = settings.value?.module?.name ?: "Discover",
                                color = ChoutenTheme.colors.fg,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> {
                            Text(
                                text = screenName ?: "",
                                color = ChoutenTheme.colors.fg,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(blackOpacity)
                    .background(Color.Black)
            )

            AnimatedBottomBox(
                settings.value,
                values
            ) { groupId, key, value ->
                viewModel.setSettingInGroup(groupId, key, value)
            }
        }
    }
}

@Composable
fun AnimatedBottomBox(
    settings: Settings?,
    values: Map<Pair<String, String>, Any>,
    onValueChanged: (groupId: String, key: String, value: Any) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    // Animate a fraction instead of absolute pixels (0 = offscreen, 1 = visible)
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = 2000,
            easing = CubicBezierEasing(0.1f, 1f, 0.2f, 1f)
        ),
        label = "boxSlide"
    )

    // LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .graphicsLayer {
                    // Translate vertically relative to the box’s own height
                    translationY = (1f - progress) * size.height
                }
                .clip(RoundedCornerShape(20.dp))
                .background(ChoutenTheme.colors.background)
        ) {
            // Topbar

            // Settings
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 60.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            color = ChoutenTheme.colors.container,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = 0.5.dp,
                            color = ChoutenTheme.colors.border,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            NetworkImage(
                                "https://cdn2.steamgriddb.com/icon/a0e7be097b3b5eb71d106dd32f2312ac.png",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clipToBounds()
                                    .blur(20.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                ChoutenTheme.colors.container.copy(alpha = 0.4f),
                                                ChoutenTheme.colors.container
                                            )
                                        )
                                    )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NetworkImage(
                                    "https://cdn2.steamgriddb.com/icon/a0e7be097b3b5eb71d106dd32f2312ac.png",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = 0.5.dp,
                                            color = ChoutenTheme.colors.border,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                )

                                Column {
                                    Text(
                                        settings?.module?.name ?: "No settings.",
                                        color = ChoutenTheme.colors.fg,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        settings?.module?.author ?: "No settings.",
                                        color = ChoutenTheme.colors.fg,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Text(
                            settings?.module?.description ?: "No settings.",
                            color = ChoutenTheme.colors.fg,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 12.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(
                                color = ChoutenTheme.colors.overlay,
                                shape = RoundedCornerShape(50)
                            )
                            .border(
                                width = 0.5.dp,
                                color = ChoutenTheme.colors.border,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            settings?.module?.version ?: "N/A",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChoutenTheme.colors.fg
                        )
                    }
                }

                settings?.group?.forEach { group ->
                    Column {
                        Text(
                            group.name,
                            color = ChoutenTheme.colors.fg,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 4.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = ChoutenTheme.colors.container,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = ChoutenTheme.colors.border,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            group.setting.forEachIndexed { index, setting ->
                                val currentValue = values[group.id to setting.key] ?: setting.default

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        Text(
                                            setting.label,
                                            color = ChoutenTheme.colors.fg,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        if (setting.description != null) {
                                            Text(
                                                setting.description!!,
                                                color = ChoutenTheme.colors.fg,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier
                                                    .alpha(0.7f)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.weight(1f))

                                    when (setting.type) {
                                        "bool" -> {
                                            Switch(
                                                checked = currentValue as? Boolean ?: false,
                                                onCheckedChange = { newValue ->
                                                    println(newValue)
                                                    onValueChanged(group.id, setting.key, newValue)
                                                }
                                            )
                                        }
                                        "string" -> {
                                            SettingTextField(
                                                values[group.id to setting.key] as? String ?: "???"
                                            ) { newText ->
                                                onValueChanged(group.id, setting.key, newText)
                                            }
                                        }
                                        else -> {

                                        }
                                    }
                                }

                                if (index < group.setting.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(ChoutenTheme.colors.border)
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun SettingTextField(
    currentValue: String,
    onValueChanged: (String) -> Unit
) {
    BasicTextField(
        value = currentValue,
        textStyle = TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = ChoutenTheme.colors.fg,
        ),
        cursorBrush = SolidColor(ChoutenTheme.colors.accent),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = ChoutenTheme.colors.overlay,
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 0.5.dp,
                color = ChoutenTheme.colors.border,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        singleLine = true,
        onValueChange = { newText ->
            onValueChanged(newText)
        }
    )

    /*
    Box(
        ,
        contentAlignment = Alignment.CenterStart
    ) {

    }

     */
}