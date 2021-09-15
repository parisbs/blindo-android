package com.pbaltazar.blindo.utils.extensions

import android.view.View
import android.widget.RatingBar
import com.google.android.material.snackbar.Snackbar
import com.pbaltazar.blindo.R

fun View.setExplainingTooltip(explainingStringResource: Int) = this.setOnClickListener { view ->
    Snackbar.make(
        view,
        context.getString(explainingStringResource),
        Snackbar.LENGTH_LONG
    ).show()
}

fun RatingBar.setValueWithAccessibilitySupport(stars: Float) = this.apply {
    rating = stars
    contentDescription = context.resources.getQuantityString(
        R.plurals.ratingbars__rating_value,
        Math.round(stars),
        stars.toRatingString()
    )
}
