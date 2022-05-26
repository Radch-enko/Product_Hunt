package com.multi.producthunt.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectResponse(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("tagline")
    val tagline: String,
    @SerialName("description")
    val description: String,
    @SerialName("thumbnail")
    val thumbnail: String? = null,
    @SerialName("media")
    val media: List<Media?> = emptyList(),
    @SerialName("createdDate")
    val createdDate: String,
    @SerialName("isVoted")
    val isVoted: Boolean,
    @SerialName("topics")
    val topics: List<Topic>,
    @SerialName("votesCount")
    val votesCount: Int
)