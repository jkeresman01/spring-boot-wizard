package com.keresman.springbootwizard.model

import com.google.gson.annotations.SerializedName

data class DependencyGroup(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("values")
    val values: List<Dependency>? = null
)