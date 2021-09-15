package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.user.UserGateway
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.responses.ApiResponse

class MutationUpdateUser(
    private val userGateway: UserGateway
) {
    suspend operator fun invoke(
        user: User,
        idToken: String
    ): ApiResponse<User> =
        userGateway.updateUser(user, idToken)
}
