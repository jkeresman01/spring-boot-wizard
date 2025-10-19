package com.keresman.springbootwizard.model

import com.google.gson.annotations.SerializedName

data class MetadataGroup(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("values")
    val values: List<MetadataOption>? = null
)