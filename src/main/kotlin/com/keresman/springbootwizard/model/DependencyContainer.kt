package com.keresman.springbootwizard.model

import com.google.gson.annotations.SerializedName

data class DependenciesContainer(
    @SerializedName("values")
    val values: List<DependencyGroup>? = null
)