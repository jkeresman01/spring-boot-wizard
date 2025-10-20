package com.keresman.springbootwizard.model

import com.google.gson.annotations.SerializedName

data class Dependency(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null
)
