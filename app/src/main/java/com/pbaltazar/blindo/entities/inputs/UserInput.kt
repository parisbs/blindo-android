package com.pbaltazar.blindo.entities.inputs

data class UserInput(
    val id: String,
    val idToken: String = "",
    val packInput: PackInput = PackInput(),
    val ratingInput: RatingInput = RatingInput()
)
