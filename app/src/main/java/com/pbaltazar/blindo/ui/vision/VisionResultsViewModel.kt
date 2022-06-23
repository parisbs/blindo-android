package com.pbaltazar.blindo.ui.vision

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.ImageDescription
import com.pbaltazar.blindo.entities.enums.ImageDescriptionLanguages
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException
import com.pbaltazar.blindo.entities.inputs.ImageDescriptionInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse
import com.pbaltazar.blindo.usecases.MutationImageDescription
import com.pbaltazar.blindo.utils.authentication.provider.AuthenticationProvider
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import com.pbaltazar.blindo.utils.vision.BlindoVisionBridge
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class VisionResultsViewModel(
    private val backgroundContext: CoroutineContext,
    private val authenticationProvider: AuthenticationProvider,
    private val userPreferences: UserPreferences,
    private val mutationImageDescription: MutationImageDescription
) : ViewModel() {

    val imageToAnalize: Bitmap? get() {
        return BlindoVisionBridge.listener.getNodeScreenshot()
    }

    private val analisis = MutableLiveData<ImageAnalisis>()
    val imageAnalisis: LiveData<ImageAnalisis> get() = analisis

    private fun getImageDescriptionLanguage(): ImageDescriptionLanguages =
        userPreferences.getString("language", "ENGLISH").let { language ->
            ImageDescriptionLanguages.valueOf(language)
        }

    private fun getImageDescriptionInput(): ImageDescriptionInput? = imageToAnalize?.let {
        ImageDescriptionInput(
            it,
            getImageDescriptionLanguage()
        )
    }

    fun analizeImage() = viewModelScope.launch(backgroundContext) {
        getImageDescriptionInput()?.also { imageDescriptionInput ->
            authenticationProvider.getUser()?.apply {
                when (val tokenResponse = authenticationProvider.getIdToken()) {
                    is AuthenticationProviderResponse.Success -> when (val apiResponse = mutationImageDescription(imageDescriptionInput, tokenResponse.data)) {
                        is ApiResponse.Success -> apiResponse.data.apply {
                            analisis.postValue(ImageAnalisis.Success(imageDescriptionInput.image, this))
                        }
                        is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                            is ApiException.EmptyResponse -> analisis.postValue(ImageAnalisis.Empty)
                            is ApiException.WithErrors -> analisis.postValue(
                                ImageAnalisis.Error(apiException.errorsList.joinToString("\n"))
                            )
                            is ApiException.CallFailure -> analisis.postValue(
                                ImageAnalisis.Error(apiException.error.localizedMessage ?: apiException.error.toString())
                            )
                        }
                    }
                    is AuthenticationProviderResponse.Error -> when (val authenticationProviderException = tokenResponse.error) {
                        is AuthenticationProviderException.Error -> analisis.postValue(
                            ImageAnalisis.Error(authenticationProviderException.error.localizedMessage ?: authenticationProviderException.error.toString())
                        )
                        else -> analisis.postValue(ImageAnalisis.Error("No signed user"))
                    }
                }
            } ?: analisis.postValue(ImageAnalisis.Error("No signed user"))
        } ?: analisis.postValue(ImageAnalisis.Error("No image to analize"))
    }

    sealed class ImageAnalisis {
        class Success(val image: Bitmap, val imageDescription: ImageDescription): ImageAnalisis()
        object Empty: ImageAnalisis()
        class Error(val reason: String): ImageAnalisis()
    }
}
