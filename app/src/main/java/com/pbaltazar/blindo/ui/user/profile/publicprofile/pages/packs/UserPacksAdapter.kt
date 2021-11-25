package com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.packs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ItemAppPackBinding
import com.pbaltazar.blindo.entities.Pack
import com.wizeline.simpleapollo.utils.extensions.toTimeAgo
import java.util.*

class UserPacksAdapter(
    diffCallback: DiffUtil.ItemCallback<Pack>,
    private val clickListener: (Pack) -> (Unit)
) : PagingDataAdapter<Pack, UserPacksViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPacksViewHolder =
        UserPacksViewHolder(
            ItemAppPackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: UserPacksViewHolder, position: Int) {
        getItem(position)?.also { item ->
            holder.itemBinding.apply {
                ViewCompat.setAccessibilityHeading(packHeaderContainer, true)
                val UNKNOWN_TEXT = root.context.getString(R.string.appcomment_unknown_author)

                appPackUserPicture.apply {
                    Glide.with(context)
                        .load(item.app?.packageIcon)
                        .placeholder(R.mipmap.generic_app_icon)
                        .centerCrop()
                        .into(this)
                }

                appPackUserName.apply {
                    text = context.getString(
                        R.string.apppack__author,
                        item.app?.packageLabel ?: UNKNOWN_TEXT,
                        item.updatedAt?.toTimeAgo() ?: UNKNOWN_TEXT
                    )
                }

                appPackLanguage.apply {
                    text = context.getString(
                        R.string.apppack__language,
                        Locale.Builder().setLanguage(item.language).build().displayLanguage
                    )
                }

                appPackDownloads.apply {
                    text = context.getString(
                        R.string.apppack__downloads,
                        item.downloads
                    )
                }

                appPackLabels.apply {
                    text = context.getString(
                        R.string.apppack__labels,
                        item.numberOfLabels
                    )
                }

                root.setOnClickListener { clickListener(item) }
            }
        }
    }
}
