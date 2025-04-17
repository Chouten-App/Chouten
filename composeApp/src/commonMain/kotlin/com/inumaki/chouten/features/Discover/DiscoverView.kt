package com.inumaki.chouten.features.Discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inumaki.chouten.Models.DiscoverSection
import com.inumaki.chouten.features.Discover.components.Carousel
import com.inumaki.chouten.features.Discover.components.List
import com.inumaki.chouten.Models.LoadingState
import com.inumaki.chouten.components.TitleCard
import com.inumaki.chouten.features.Discover.components.LoadingCarousel
import com.inumaki.chouten.features.Discover.components.LoadingList

@Composable
fun DiscoverView(
    viewModel: DiscoverViewModel = viewModel { DiscoverViewModel() }
) {
    val sections by viewModel.sections.collectAsState()

    when (viewModel.state.value) {
        LoadingState.LOADING -> LoadingDiscoverView()
        LoadingState.SUCCESS -> SuccessDiscoverView(sections!!)
        LoadingState.EMPTY -> {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TitleCard("No Content available.", description = "There was no content found for this module. Either try searching or using a different module.")
            }
        }
        LoadingState.ERROR -> Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TitleCard("An Error occurred.", description = "There was an error while executing the discover function. The error will be shown here in the future.")
        }
    }
}

@Composable
fun LoadingDiscoverView() {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            LoadingCarousel()
        }

        for (i in 0..3) {
            item {
                LoadingList()
            }
        }
    }
}

@Composable
fun SuccessDiscoverView(sections: List<DiscoverSection>) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        items(items = sections) { section ->
            when (section.type) {
                0 -> {
                    Carousel(data = section)
                }

                1 -> {
                    List(
                        title = section.title,
                        list = section.list
                    )
                }
                // Handle other possible sections here
                else -> {
                    Text(text = "Unknown section")
                }
            }
        }
    }
}
