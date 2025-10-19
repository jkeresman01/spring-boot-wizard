package com.keresman.springbootwizard.model

import com.google.gson.annotations.SerializedName

data class Metadata(
    @SerializedName("type")
    val type: MetadataGroup? = null,

    @SerializedName("language")
    val language: MetadataGroup? = null,

    @SerializedName("bootVersion")
    val bootVersion: MetadataGroup? = null,

    @SerializedName("packaging")
    val packaging: MetadataGroup? = null,

    @SerializedName("javaVersion")
    val javaVersion: MetadataGroup? = null,

    @SerializedName("dependencies")
    val dependencies: DependenciesContainer? = null
)