package com.inumaki.chouten.features

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
import com.inumaki.chouten.features.components.Carousel
import com.inumaki.chouten.features.components.List
import com.inumaki.chouten.Models.LoadingState

@Composable
fun DiscoverView(
    viewModel: DiscoverViewModel = viewModel { DiscoverViewModel() }
) {
    val sections by viewModel.sections.collectAsState()

    val listState = rememberLazyListState()

    if (viewModel.state.value == LoadingState.SUCCESS) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            state = listState,
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            sections?.let { validSections ->
                // Iterate over the non-null list of sections
                items(items = validSections) { section ->
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
            } ?: item {
                // Handle the case where sections is null (e.g., show a placeholder message)
                Text(text = "No sections available")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Loading...")
        }
    }
}
