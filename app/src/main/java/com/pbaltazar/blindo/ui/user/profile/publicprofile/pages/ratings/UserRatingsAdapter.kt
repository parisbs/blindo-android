package com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.ratings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.blindo.apollito.utils.extensions.toTimeAgo
import com.bumptech.glide.Glide
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ItemAppCommentBinding
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.utils.extensions.toRatingString
import java.util.*

class UserRatingsAdapter(
    diffCallback: DiffUtil.ItemCallback<Rating>,
    private val clickListener: (Rating) -> (Unit)
) : PagingDataAdapter<Rating, UserRatingsViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRatingsViewHolder =
        UserRatingsViewHolder(
            ItemAppCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: UserRatingsViewHolder, position: Int) {
        getItem(position)?.also { item ->
            holder.itemBinding.apply {
                ViewCompat.setAccessibilityHeading(commentHeaderContainer, true)
                val unknownText = root.context.getString(R.string.appcomment_unknown_author)

                appCommentUserPicture.apply {
                    Glide.with(context)
                        .load(item.app?.packageIcon)
                        .placeholder(R.mipmap.generic_app_icon)
                        .centerCrop()
                        .into(this)
                }

                appCommentUserName.apply {
                    text = item.app?.packageLabel ?: unknownText
                }

                appCommentTotalAndDate.apply {
                    val date: Date = item.updatedAt ?: item.createdAt
                    text = context.getString(
                        R.string.appcomment__total_and_date,
                        item.total?.toRatingString() ?: 0,
                        date.toTimeAgo()
                    )
                }

                appCommentText.apply {
                    text = item.comment?.let {
                        if (it.length <= 60) it else "${it.substring(0, 59)}..."
                    } ?: context.getString(R.string.appcomment__no_comment)
                }

                root.setOnClickListener { clickListener(item) }
            }
        }
    }
}
