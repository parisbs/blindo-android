package com.pbaltazar.blindo.utils.authentication.provider

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse
import com.pbaltazar.blindo.utils.extensions.toApiModel
import com.pbaltazar.blindo.utils.log.BlindoLogger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthenticationProvider(
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val firebaseAuth: FirebaseAuth
) : AuthenticationProvider {

    override suspend fun getUser(): User? =
        suspendCoroutine { continuation ->
            firebaseAuth.currentUser?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseAuth.currentUser?.also { user ->
                        continuation.resume(user.toApiModel())
                    } ?: continuation.resume(null)
                } else {
                    task.exception?.also { e ->
                        firebaseCrashlytics.recordException(e)
                        BlindoLogger.e(e)
                        continuation.resume(null)
                    } ?: continuation.resume(null)
                }
            } ?: continuation.resume(null)
    }

    override suspend fun sendVerificationEmail(): AuthenticationProviderResponse<Boolean> =
        suspendCoroutine { continuation ->
            firebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(AuthenticationProviderResponse.Success(true))
                } else {
                    task.exception?.also { e ->
                        firebaseCrashlytics.recordException(e)
                        BlindoLogger.e(e)
                        continuation.resume(
                            AuthenticationProviderResponse.Error(
                                AuthenticationProviderException.Error(e)
                            )
                        )
                    }
                        ?: continuation.resume(AuthenticationProviderResponse.Error(AuthenticationProviderException.Empty))
                }
            } ?: continuation.resume(AuthenticationProviderResponse.Error(AuthenticationProviderException.NotSignedIn))
    }

    override suspend fun getIdToken(): AuthenticationProviderResponse<String> =
        suspendCoroutine { continuation ->
            firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.token?.also { token ->
                        continuation.resume(AuthenticationProviderResponse.Success("Bearer $token"))
                    }
                        ?: continuation.resume(AuthenticationProviderResponse.Error(AuthenticationProviderException.Empty))
                } else {
                    task.exception?.also { e ->
                        firebaseCrashlytics.recordException(e)
                        BlindoLogger.e(e)
                        continuation.resume(
                            AuthenticationProviderResponse.Error(
                                AuthenticationProviderException.Error(e)
                            )
                        )
                    }
                        ?: continuation.resume(AuthenticationProviderResponse.Error(AuthenticationProviderException.UnknownError))
                }
            } ?: continuation.resume(AuthenticationProviderResponse.Error(AuthenticationProviderException.NotSignedIn))
    }

    override fun signOut() = firebaseAuth.signOut()
}
