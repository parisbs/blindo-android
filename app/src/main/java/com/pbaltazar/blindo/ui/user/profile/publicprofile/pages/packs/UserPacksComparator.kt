package com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.packs

import android.text.TextUtils
import androidx.recyclerview.widget.DiffUtil
import com.pbaltazar.blindo.entities.Pack

object UserPacksComparator : DiffUtil.ItemCallback<Pack>() {
    override fun areItemsTheSame(oldItem: Pack, newItem: Pack): Boolean =
        TextUtils.equals(oldItem.id, newItem.id)

    override fun areContentsTheSame(oldItem: Pack, newItem: Pack): Boolean =
        TextUtils.equals(oldItem.id, newItem.id)
}
