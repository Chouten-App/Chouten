package com.inumaki.chouten.Models

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class DiscoverData @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid = Uuid.random(),
    val url: String,
    val titles: Titles,
    val poster: String,
    val banner: String?,
    val description: String,
    val label: Label,
    val indicator: String?,
    val isWidescreen: Boolean = false,
    val current: Int?,
    val total: Int?
)

//val sanitizedDescription: String
//    get() {
//        val regexPattern = "<[^>]+>"
//        return try {
//            val pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE)
//            val matcher = pattern.matcher(description)
//            matcher.replaceAll("")
//        } catch (e: Exception) {
//            description
//        }
//    }