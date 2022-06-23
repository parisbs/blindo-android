package com.pbaltazar.blindo.data.vision

import com.apollographql.apollo3.api.DefaultUpload
import com.blindo.apollito.api.ApollitoClient
import com.blindo.apollito.models.Response
import com.pbaltazar.blindo.data.ApiHelpers
import com.pbaltazar.blindo.entities.ImageDescription
import com.pbaltazar.blindo.entities.enums.ImageDescriptionLanguages
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.ImageDescriptionInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.ImageDescriptionMutation
import com.pbaltazar.blindo.graphql.type.ImageDescriptionLanguagesEnum
import com.pbaltazar.blindo.utils.extensions.toApiModel

class BlindoApiVisionGateway(
    private val blindoApiClient: ApollitoClient
) : ApiHelpers, VisionGateway {

    override suspend fun imageDescription(imageDescriptionInput: ImageDescriptionInput, idToken: String): ApiResponse<ImageDescription> =
        blindoApiClient.    mutate(
            ImageDescriptionMutation(
                input = com.pbaltazar.blindo.graphql.type.ImageDescriptionInput(
                    image = DefaultUpload.Builder()
                        .content(imageDescriptionInput.imageToByteArray())
                        .fileName("screenshot.png")
                        .contentType("image/png")
                        .contentLength(imageDescriptionInput.imageToByteArray().count().toLong())
                        .build(),
                    language = when (imageDescriptionInput.language) {
                        ImageDescriptionLanguages.ENGLISH -> ImageDescriptionLanguagesEnum.ENGLISH
                        ImageDescriptionLanguages.SPANISH -> ImageDescriptionLanguagesEnum.SPANISH
                        ImageDescriptionLanguages.JAPANESE -> ImageDescriptionLanguagesEnum.JAPANESE
                        ImageDescriptionLanguages.PORTUGUESE -> ImageDescriptionLanguagesEnum.PORTUGUESE
                        ImageDescriptionLanguages.SIMPLIFIED_CHINESE -> ImageDescriptionLanguagesEnum.SIMPLIFIED_CHINESE
                    }
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.imageDescription?.let { imageDescription ->
                    ApiResponse.Success(imageDescription.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }
}
