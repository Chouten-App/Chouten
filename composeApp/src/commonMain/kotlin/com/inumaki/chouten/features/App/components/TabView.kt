package com.inumaki.chouten.features.App.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.inumaki.chouten.features.Discover.components.conditional
import com.inumaki.chouten.helpers.topBorder
import com.inumaki.chouten.theme.ChoutenTheme
import com.inumaki.chouten.theme.LocalDeviceInfo
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

data class TabBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController, hazeState: HazeState) {
    var selectedTabIndex by rememberSaveable {
        mutableStateOf(1)
    }

    var tabBarWidth by remember { mutableStateOf(0f) }

    val circleX by animateFloatAsState(
        targetValue = when (selectedTabIndex) {
            0 -> tabBarWidth / tabBarItems.size / 2f
            1 -> tabBarWidth / 2f
            2 -> tabBarWidth - tabBarWidth / tabBarItems.size / 2f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 400, easing = CubicBezierEasing(0.1f, 1f, 0.2f, 1f))
    )

    val pillX by animateDpAsState(
        targetValue = when (selectedTabIndex) {
            0 -> -(tabBarWidth / 5.4f).dp
            1 -> 0.dp
            2 -> (tabBarWidth / 5.4f).dp
            else -> 0.dp
        },
        animationSpec = tween(durationMillis = 400, easing = CubicBezierEasing(0.1f, 1f, 0.2f, 1f))
    )

    /*
    val animatedOffset by animateDpAsState(
        targetValue = when(selectedTabIndex) {
            0 -> { 45.0.dp }
            1 -> { ((dpWidth.toFloat() / 2) - 12).dp }
            2 -> { (dpWidth.toFloat() - 45 - 24).dp }
            else -> {
                0.0.dp
            }
        },
        label = "offset"
    )*/

    when (LocalDeviceInfo.current.isTablet) {
        true -> {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 30.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(ChoutenTheme.colors.container, shape = RoundedCornerShape(50))
                        .border(0.5.dp, ChoutenTheme.colors.border, shape = RoundedCornerShape(50))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tabBarItems.forEachIndexed { index, item ->
                        Box(
                            modifier = Modifier
                                .conditional(selectedTabIndex == index) {
                                    background(ChoutenTheme.colors.overlay, shape = RoundedCornerShape(50))
                                }
                                .conditional(selectedTabIndex == index) {
                                    border(0.5.dp, ChoutenTheme.colors.border, shape = RoundedCornerShape(50))
                                }
                                .width(44.dp)
                                .height(44.dp)
                                .clickable {
                                    selectedTabIndex = index
                                    navController.navigate(item.title)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTabIndex == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = ChoutenTheme.colors.fg,
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(20.dp)
                                    .alpha(if (selectedTabIndex == index) 1f else 0.7f)
                            )
                        }
                    }
                }
            }
        }
        false -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .hazeEffect(hazeState, style = HazeMaterials.thin(ChoutenTheme.colors.container))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .blur(40.dp) // blur applies to this whole box
                        .topBorder(color = ChoutenTheme.colors.border, height = 1f)
                        .onSizeChanged { tabBarWidth = it.width.toFloat() },
                    contentAlignment = Alignment.TopCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0x305E5CE6),
                            radius = 70.dp.toPx(),
                            center = Offset(
                                x = circleX,
                                y = -10.dp.toPx()
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .padding(top = 12.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabBarItems.forEachIndexed { index, item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .alpha(if (selectedTabIndex == index) 1.0f else 0.7f )
                                .clickable {
                                    selectedTabIndex = index
                                    navController.navigate(item.title)
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedTabIndex == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = ChoutenTheme.colors.fg,
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(20.dp)
                            )

                            Text(item.title, fontSize = 14.sp)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = pillX, y = (-2).dp)        // adjust position
                        .size(width = 32.dp, height = 4.dp)
                        .background(
                            color = ChoutenTheme.colors.accent,
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }
    }
}