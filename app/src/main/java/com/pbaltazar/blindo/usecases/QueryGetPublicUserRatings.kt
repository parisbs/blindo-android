package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.user.UserGateway
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.inputs.UserInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryGetPublicUserRatings(
    private val userGateway : UserGateway
) {
    suspend operator fun invoke(userInput : UserInput): ApiResponse<User> =
        userGateway.getPublicUserRatings(userInput)
}
