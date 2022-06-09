package com.multi.producthunt.domain.model

import com.multi.producthunt.network.model.ApiResult
import com.multi.producthunt.network.model.map
import com.multi.producthunt.network.model.response.UserResponse
import com.multi.producthunt.network.util.CommonFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserDomain(
    val id: Int,
    val name: String,
    val username: String,
    val headline: String?,
    val profileImage: String?,
    val coverImage: String?,
)

fun CommonFlow<ApiResult<List<UserResponse>>>.toDomain(): Flow<ApiResult<List<UserDomain>>> {
    return this.map { response ->
        response.map { list ->
            list.map { userResponse ->
                userResponse.toDomain()
            }
        }
    }
}

fun Flow<ApiResult<UserResponse>>.toDomain(): Flow<ApiResult<UserDomain>> {
    return this.map {
        it.map { userResponse ->
            userResponse.toDomain()
        }
    }
}

fun UserResponse.toDomain(): UserDomain {
    return UserDomain(
        id = this.id,
        name = this.name,
        username = this.username,
        headline = this.headline,
        profileImage = this.profileImage,
        coverImage = this.coverImage
    )
}