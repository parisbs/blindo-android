package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.comment.CommentGateway
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.responses.ApiResponse

class MutationUpdateComment(
    private val commentGateway: CommentGateway
) {
    suspend operator fun invoke(
        rating: Rating, idToken: String
    ): ApiResponse<Rating> =
        commentGateway.updateComment(rating, idToken)
}
