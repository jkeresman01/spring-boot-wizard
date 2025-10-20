package com.keresman.springbootwizard.model

import com.google.gson.annotations.SerializedName

data class DependencyContainer(
    @SerializedName("values")
    val values: List<DependencyGroup>? = null
)