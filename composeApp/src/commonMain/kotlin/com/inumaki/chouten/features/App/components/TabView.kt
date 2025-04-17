package com.inumaki.chouten.features.App.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.inumaki.chouten.features.Discover.components.conditional
import com.inumaki.chouten.theme.ChoutenTheme
import com.inumaki.chouten.theme.LocalDeviceInfo

data class TabBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController) {
    var selectedTabIndex by rememberSaveable {
        mutableStateOf(1)
    }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(ChoutenTheme.colors.container)
                    .padding(top = 12.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabBarItems.forEachIndexed { index, item ->
                    Column(
                        modifier = Modifier
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
        }
    }
}