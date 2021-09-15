package com.pbaltazar.blindo.data.comment

import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.inputs.CommentInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

interface CommentGateway {

    suspend fun getAppRatings(appInput: AppInput): ApiResponse<List<Rating>>

    suspend fun getAppRatingsByPackageName(appInput: AppInput): ApiResponse<List<Rating>>

    suspend fun createComment(rating: Rating, idToken: String): ApiResponse<Rating>

    suspend fun updateComment(rating: Rating, idToken: String): ApiResponse<Rating>

    suspend fun listComments(commentInput: CommentInput): ApiResponse<List<Rating>>
}
