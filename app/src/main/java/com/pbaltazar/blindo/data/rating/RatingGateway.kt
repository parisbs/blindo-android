package com.pbaltazar.blindo.data.rating

import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.inputs.RatingInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

interface RatingGateway {

    suspend fun getAppRatings(appInput: AppInput): ApiResponse<List<Rating>>

    suspend fun getAppRatingsByPackageName(appInput: AppInput): ApiResponse<List<Rating>>

    suspend fun createRating(rating: Rating, idToken: String): ApiResponse<Rating>

    suspend fun updateRating(rating: Rating, idToken: String): ApiResponse<Rating>

    suspend fun listRatings(ratingInput: RatingInput): ApiResponse<List<Rating>>
}
