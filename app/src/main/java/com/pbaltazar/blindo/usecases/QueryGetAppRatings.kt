package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.comment.CommentGateway
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryGetAppRatings(
    private val commentGateway: CommentGateway
) {
    suspend operator fun invoke(appInput: AppInput): ApiResponse<List<Rating>> =
        commentGateway.getAppRatings(appInput)
}
