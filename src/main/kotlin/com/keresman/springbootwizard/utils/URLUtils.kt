package com.keresman.springbootwizard.utils

import java.net.URLEncoder

object URLUtils {
    fun encode(value: String): String {
        return try {
            URLEncoder.encode(value, "UTF-8").replace("+", "%20")
        } catch (e: Exception) {
            value
        }
    }
}