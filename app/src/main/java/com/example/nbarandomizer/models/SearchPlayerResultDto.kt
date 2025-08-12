package com.example.nbarandomizer.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchPlayerResultDto(
    val content: String,
    val image: String,
    val title: String,

    @SerialName("post_title")
    val name: String,

    @SerialName("link")
    val url: String
)