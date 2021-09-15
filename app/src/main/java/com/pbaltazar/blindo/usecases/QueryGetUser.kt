package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.user.UserGateway
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryGetUser(
    private val userGateway: UserGateway
) {
    suspend operator fun invoke(sub: String, idToken: String): ApiResponse<User> =
        userGateway.getUser(sub, idToken)
}
