package com.pbaltazar.blindo.data.vision

import com.pbaltazar.blindo.entities.ImageDescription
import com.pbaltazar.blindo.entities.inputs.ImageDescriptionInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

interface VisionGateway {

    suspend fun imageDescription(imageDescriptionInput: ImageDescriptionInput, idToken: String): ApiResponse<ImageDescription>
}
