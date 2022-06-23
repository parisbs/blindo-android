package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.vision.VisionGateway
import com.pbaltazar.blindo.entities.ImageDescription
import com.pbaltazar.blindo.entities.inputs.ImageDescriptionInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class MutationImageDescription(
    private val visionGateway: VisionGateway
) {
    suspend operator fun invoke(
        imageDescriptionInput: ImageDescriptionInput,
        idToken: String
    ): ApiResponse<ImageDescription> =
        visionGateway.imageDescription(imageDescriptionInput, idToken)
}
