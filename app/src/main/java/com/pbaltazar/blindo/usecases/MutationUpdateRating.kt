package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.rating.RatingGateway
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.responses.ApiResponse

class MutationUpdateRating(
    private val ratingGateway: RatingGateway
) {
    suspend operator fun invoke(
        rating: Rating, idToken: String
    ): ApiResponse<Rating> =
        ratingGateway.updateRating(rating, idToken)
}
