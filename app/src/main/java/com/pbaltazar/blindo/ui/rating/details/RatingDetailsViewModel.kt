package com.pbaltazar.blindo.ui.rating.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pbaltazar.blindo.entities.Rating

class RatingDetailsViewModel : ViewModel() {

    private val _rating = MutableLiveData<Rating>()

    fun setTargetRating(rating: Rating) {
        _rating.value = rating
    }

    fun getTargetRating(): Rating? = _rating.value
}
