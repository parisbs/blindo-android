package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.rating.RatingGateway
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryGetAppRatingsByPackageName(
    private val ratingGateway : RatingGateway
) {
    suspend operator fun invoke(appInput: AppInput): ApiResponse<List<Rating>> =
        ratingGateway.getAppRatingsByPackageName(appInput)
}
