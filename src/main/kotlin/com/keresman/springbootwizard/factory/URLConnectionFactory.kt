package com.keresman.springbootwizard.factory

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object URLConnectionFactory {
    private const val TIMEOUT = 10_000
    private const val REQUEST_METHOD_GET = "GET"
    private const val HEADER_USER_AGENT = "User-Agent"
    private const val USER_AGENT_VALUE = "Mozilla/5.0"

    @Throws(IOException::class)
    fun getHttpUrlConnection(path: String): HttpURLConnection {
        val connection = URL(path).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT
        connection.readTimeout = TIMEOUT
        connection.requestMethod = REQUEST_METHOD_GET
        connection.addRequestProperty(HEADER_USER_AGENT, USER_AGENT_VALUE)
        return connection
    }
}