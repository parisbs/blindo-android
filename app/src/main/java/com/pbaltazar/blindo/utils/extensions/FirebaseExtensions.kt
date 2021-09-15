package com.pbaltazar.blindo.utils.extensions

import com.google.firebase.auth.FirebaseUser
import com.pbaltazar.blindo.entities.User

fun FirebaseUser.toApiModel(): User = User(
    sub = uid,
    email = email ?: uid,
    name = displayName ?: "",
    picture = photoUrl?.toString(),
    isVerified = isEmailVerified
)
