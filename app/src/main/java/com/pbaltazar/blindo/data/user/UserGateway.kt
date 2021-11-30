package com.pbaltazar.blindo.data.user

import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.inputs.UserInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

interface UserGateway {

    suspend fun getUser(sub: String, idToken: String): ApiResponse<User>

    suspend fun authenticateUser(userInput: UserInput): ApiResponse<User>

    suspend fun getPublicUser(userInput: UserInput): ApiResponse<User>

    suspend fun getPublicUserPacks(userInput: UserInput): ApiResponse<User>

    suspend fun getPublicUserRatings(userInput: UserInput): ApiResponse<User>

    suspend fun createUser(user: User, idToken: String): ApiResponse<User>

    suspend fun updateUser(user: User, idToken: String): ApiResponse<User>
}
