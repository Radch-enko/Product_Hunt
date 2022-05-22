package com.multi.producthunt.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("username")
    val username: String,
    @SerialName("headline")
    val headline: String? = null,
    @SerialName("profileImage")
    val profileImage: String? = null,
    @SerialName("coverImage")
    val coverImage: String? = null,
)