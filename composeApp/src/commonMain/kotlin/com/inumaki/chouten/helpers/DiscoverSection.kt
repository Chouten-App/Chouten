package com.inumaki.chouten.helpers

import com.inumaki.chouten.Models.DiscoverData
import com.inumaki.chouten.Models.DiscoverSection
import com.inumaki.chouten.Models.Label
import com.inumaki.chouten.Models.Titles
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
fun convertMapToDiscoverSections(jsItem: Map<String, Any>): DiscoverSection {
    val title = jsItem["title"] as String
    val type = jsItem["type"] as Double

    val items: MutableList<DiscoverData> = mutableListOf()

    for (data in jsItem["data"] as List<*>) {
        val jsData = data as Map<String, Any>

        println(jsData)

        val url = jsData["url"] as String
        val titlesData = jsData["titles"] as Map<String, Any>
        val titles = Titles(
            primary = titlesData["primary"] as String,
            secondary = null // titlesData["secondary"] as String?
        )

        val poster = jsData["poster"] as String
        val banner = jsData["banner"] as String
        val description = jsData["description"] as String
        val indicator = jsData.get("indicator") as String?

        items.add(
            DiscoverData(
                id = Uuid.random(),
                url,
                titles,
                poster,
                banner,
                description,
                label = Label(text = "", color = ""),
                indicator,
                isWidescreen = false,
                current = null,
                total = null
            )
        )
    }

    return DiscoverSection(
        title,
        type = type.toInt(),
        list = items
    )
}