package com.keresman.springbootwizard.utils

import com.keresman.springbootwizard.factory.URLConnectionFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection

object FileUtils {

    fun readFromURL(urlString: String): String {
        val conn = try {
            URLConnectionFactory.getHttpUrlConnection(urlString)
        } catch (e: Exception) {
            throw RuntimeException("Failed to create connection: ${e.message}", e)
        }

        return try {
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("HTTP ${conn.responseCode}")
            }

            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                reader.readText()
            }
        } finally {
            conn.disconnect()
        }
    }
}