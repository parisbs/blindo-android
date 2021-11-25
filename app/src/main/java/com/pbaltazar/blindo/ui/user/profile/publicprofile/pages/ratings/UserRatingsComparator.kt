package com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.ratings

import android.text.TextUtils
import androidx.recyclerview.widget.DiffUtil
import com.pbaltazar.blindo.entities.Rating

object UserRatingsComparator : DiffUtil.ItemCallback<Rating>() {
    override fun areItemsTheSame(oldItem: Rating, newItem: Rating): Boolean =
        TextUtils.equals(oldItem.id, newItem.id)

    override fun areContentsTheSame(oldItem: Rating, newItem: Rating): Boolean =
        TextUtils.equals(oldItem.id, newItem.id)
}
