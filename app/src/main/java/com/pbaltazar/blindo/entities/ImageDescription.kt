package com.pbaltazar.blindo.entities

import com.pbaltazar.blindo.entities.enums.ImageDescriptionLanguages

data class ImageDescription(
    val description: String,
    val descriptionConfidence: Float,
    val descriptionTags: List<String?>,
    val descriptionLanguage: ImageDescriptionLanguages,
    val imageText: String?,
    val left: Int
) {
    val percentageOfConfidence: String get() {
        val percentage: String = (descriptionConfidence * 100).toString().split(".").first()
        return "${percentage}%"
    }
}
