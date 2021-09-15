package com.pbaltazar.blindo.utils.authentication.provider

import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse

interface AuthenticationProvider {

    suspend fun getUser(): User?

    suspend fun sendVerificationEmail(): AuthenticationProviderResponse<Boolean>

    suspend fun getIdToken(): AuthenticationProviderResponse<String>

    fun signOut()
}
