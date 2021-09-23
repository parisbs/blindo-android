package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.rating.RatingGateway
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.inputs.RatingInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryListRatings(
    private val ratingGateway: RatingGateway
) {
    suspend operator fun invoke(ratingInput: RatingInput): ApiResponse<List<Rating>> =
        ratingGateway.listRatings(ratingInput)
}
