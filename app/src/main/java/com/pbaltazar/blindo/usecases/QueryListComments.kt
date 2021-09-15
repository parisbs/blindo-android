package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.comment.CommentGateway
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.inputs.CommentInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryListComments(
    private val commentGateway: CommentGateway
) {
    suspend operator fun invoke(commentInput: CommentInput): ApiResponse<List<Rating>> =
        commentGateway.listComments(commentInput)
}
