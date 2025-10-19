package com.keresman.springbootwizard.service

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import com.keresman.springbootwizard.factory.URLConnectionFactory
import com.keresman.springbootwizard.model.Metadata
import com.keresman.springbootwizard.utils.FileUtils
import com.keresman.springbootwizard.utils.URLUtils
import java.net.HttpURLConnection
import java.util.concurrent.CompletableFuture

class MetadataService {
    companion object {
        private val LOG = Logger.getInstance(MetadataService::class.java)
        private const val METADATA_URL = "https://start.spring.io/metadata/client"
    }

    private var cachedMetadata: Metadata? = null
    @Volatile
    private var isLoading = false
    private val gson = Gson()

    fun interface MetadataCallback {
        fun onMetadataLoaded(metadata: Metadata?, error: Exception?)
    }

    fun fetchMetadataAsync(): CompletableFuture<Metadata?> {
        return CompletableFuture.supplyAsync { fetchMetadataSync() }
    }

    fun fetchMetadataSync(): Metadata? {
        cachedMetadata?.let { return it }

        if (isLoading) {
            return waitForCachedMetadata()
        }

        isLoading = true
        return try {
            val jsonResponse = FileUtils.readFromURL(METADATA_URL)
            parseAndCacheMetadata(jsonResponse)
        } catch (e: Exception) {
            LOG.warn("Failed to fetch Spring metadata: ${e.message}")
            null
        } finally {
            isLoading = false
        }
    }


    fun downloadProjectZip(downloadUrl: String): ByteArray {
        LOG.info("Downloading project from: $downloadUrl")

        val conn = try {
            URLConnectionFactory.getHttpUrlConnection(downloadUrl)
        } catch (e: Exception) {
            throw RuntimeException("Failed to create connection: ${e.message}", e)
        }

        return try {
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("HTTP ${conn.responseCode}: ${conn.responseMessage}")
            }
            conn.inputStream.readAllBytes()
        } finally {
            conn.disconnect()
        }
    }


    fun buildDownloadUrl(baseUrl: String, params: Map<String, String>): String {
        val queryString = params
            .filterValues { it.isNotEmpty() }
            .entries
            .joinToString("&") { (key, value) ->
                "${URLUtils.encode(key)}=${URLUtils.encode(value)}"
            }
        return "$baseUrl/starter.zip?$queryString"
    }

    fun clearCache() {
        cachedMetadata = null
    }


    private fun waitForCachedMetadata(): Metadata? {
        return try {
            Thread.sleep(100)
            cachedMetadata
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            null
        }
    }


    private fun parseAndCacheMetadata(jsonResponse: String): Metadata? {
        return try {
            val metadata = gson.fromJson(jsonResponse, Metadata::class.java)
            cachedMetadata = metadata
            metadata
        } catch (e: Exception) {
            LOG.warn("Failed to parse metadata JSON: ${e.message}")
            null
        }
    }
}