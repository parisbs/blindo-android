package com.pbaltazar.blindo.ui.user.backup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.blindoapp.uitools.recyclerview.Adapter
import com.blindoapp.uitools.recyclerview.ViewHolder
import com.bumptech.glide.Glide
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ItemAppPackBinding
import com.pbaltazar.blindo.entities.Pack
import com.wizeline.simpleapollo.utils.extensions.toTimeAgo
import java.util.*

class BackupAdapter(
    private val clickListener: (Pack) -> (Unit)
) : Adapter<Pack>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        BackupViewHolder(
            ItemAppPackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bind(item: Pack, viewHolder: ViewHolder) {
        (viewHolder as BackupViewHolder).itemBinding.apply {
            ViewCompat.setAccessibilityHeading(packHeaderContainer, true)
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
                    item.app?.packageLabel ?: context.getString(R.string.appcomment_unknown_author),
                    item.updatedAt?.toTimeAgo() ?: context.getString(R.string.appcomment_unknown_author)
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
