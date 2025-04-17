package com.inumaki.chouten.repo

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeToURL

actual object FileManager {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun storeString(at: String, data: String) {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            directory = NSDocumentDirectory,
            inDomains = NSUserDomainMask
        )
        val documentsUrl = paths.first() as NSURL

        val reposDirectory = documentsUrl.URLByAppendingPathComponent("Repos")
        val repoDirectory = reposDirectory?.URLByAppendingPathComponent(at)
        val modulesDirectory = repoDirectory?.URLByAppendingPathComponent("Modules")

        modulesDirectory?.let {
            NSFileManager.defaultManager.createDirectoryAtURL(
                modulesDirectory,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )

            val nsString = NSString.create(string = data)
            val nsData = nsString.dataUsingEncoding(NSUTF8StringEncoding)
            repoDirectory.URLByAppendingPathComponent("metadata.json")?.let {
                nsData?.writeToURL(it, atomically = true)
            }
        }
    }
}