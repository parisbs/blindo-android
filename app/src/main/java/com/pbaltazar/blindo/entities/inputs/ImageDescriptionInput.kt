package com.pbaltazar.blindo.entities.inputs

import android.graphics.Bitmap
import com.pbaltazar.blindo.entities.enums.ImageDescriptionLanguages
import java.io.ByteArrayOutputStream

data class ImageDescriptionInput(
    val image: Bitmap,
    val language: ImageDescriptionLanguages
) {

    fun imageToByteArray(): ByteArray = ByteArrayOutputStream().let { stream ->
        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
    }
}
