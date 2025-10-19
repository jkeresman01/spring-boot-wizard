package com.keresman.springbootwizard.model

import com.google.gson.annotations.SerializedName

data class MetadataOption(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String
) {
    override fun toString() = name
}
