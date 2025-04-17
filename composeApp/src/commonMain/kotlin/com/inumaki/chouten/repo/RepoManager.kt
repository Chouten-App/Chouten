package com.inumaki.chouten.repo

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ktor.client

@Serializable
data class Repository(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val modules: List<Module>
)

@Serializable
data class Module(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val filePath: String,
    val subtypes: List<String>,
    val iconPath: String
)

object RepoManager {
    suspend fun addRepo(url: String) {
        // check if url is github url
        if (!url.contains("github.com")) return

        // parse owner and repo name
        val split = url.split("/")
        val repo = split.last()
        val owner = split.get(split.size - 2)

        // get metadata.json file from repo
        val response = client.get("https://raw.githubusercontent.com/${owner}/${repo}/main/metadata.json")
        if (response.status.value != 200) return

        val metadataString = response.bodyAsText()

        // store metadata.json in Documents/Repos/{repo id from the metadata.json}/metadata.json
        // and create Modules folder in Documents/Repos/{repo id from the metadata.json}/
        val repoData = Json.decodeFromString<Repository>(metadataString)

        FileManager.storeString(repoData.id, metadataString)
    }

    fun removeRepo(id: String) {
    }

    fun installModule(id: String) {
    }

    fun updateModule(id: String) {
    }

    fun removeModule(id: String) {
    }
}

expect object FileManager {
    fun storeString(at: String, data: String)
}