package com.pbaltazar.blindo.ui.home

import android.text.TextUtils
import androidx.recyclerview.widget.DiffUtil
import com.pbaltazar.blindo.entities.App

object HomeComparator : DiffUtil.ItemCallback<App>() {
    override fun areItemsTheSame(oldItem: App, newItem: App): Boolean =
        TextUtils.equals(oldItem.id, newItem.id)

    override fun areContentsTheSame(oldItem: App, newItem: App): Boolean =
        oldItem.equals(newItem)
}
