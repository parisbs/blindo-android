package com.pbaltazar.blindo.ui.app.details.pages.ratings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.blindo.apollito.utils.extensions.toTimeAgo
import com.blindoapp.uitools.recyclerview.Adapter
import com.blindoapp.uitools.recyclerview.ViewHolder
import com.bumptech.glide.Glide
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ItemAppCommentBinding
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.utils.extensions.toRatingString
import java.util.*

class AppRatingsAdapter(
    private val clickListener: (Rating) -> (Unit)
) : Adapter<Rating>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        AppRatingsViewHolder(
            ItemAppCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bind(item: Rating, viewHolder: ViewHolder) {
        (viewHolder as AppRatingsViewHolder).itemBinding.apply {
            ViewCompat.setAccessibilityHeading(commentHeaderContainer, true)
            appCommentUserPicture.apply {
                Glide.with(context)
                    .load(item.user?.picture)
                    .placeholder(R.mipmap.default_user_picture)
                    .centerCrop()
                    .into(this)
            }
            appCommentUserName.apply {
                text = item.user?.name ?: context.getString(R.string.appcomment_unknown_author)
                ViewCompat.setAccessibilityHeading(this, true)
            }
            appCommentTotalAndDate.apply {
                val date: Date = item.updatedAt ?: item.createdAt
                text = context.getString(
                    R.string.appcomment__total_and_date,
                    item.total?.toRatingString(),
                    date.toTimeAgo()
                )
            }
            appCommentText.apply {
                text = item.comment?.let {
                    if (it.length <= 60) {
                        it
                    } else {
                        "${it.subSequence(0, 60)}"
                    }
                } ?: context.getString(R.string.appcomment__no_comment)
            }
            root.setOnClickListener { clickListener(item) }
        }
    }
}
