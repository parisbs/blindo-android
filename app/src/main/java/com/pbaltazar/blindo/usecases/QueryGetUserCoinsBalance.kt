package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.user.UserGateway
import com.pbaltazar.blindo.entities.inputs.UserInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryGetUserCoinsBalance(
    private val userGateway: UserGateway
) {
    suspend operator fun invoke(userInput: UserInput): ApiResponse<Int> =
        userGateway.getUserCoinsBalance(userInput)
}
