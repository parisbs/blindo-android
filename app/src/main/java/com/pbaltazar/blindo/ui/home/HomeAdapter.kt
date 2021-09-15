package com.pbaltazar.blindo.ui.home

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ItemAppBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.utils.extensions.toRatingString

class HomeAdapter(
    diffCallback: DiffUtil.ItemCallback<App>,
    private val clickListener: (App) -> (Unit)
) : PagingDataAdapter<App, HomeViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder =
        HomeViewHolder(
            ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        getItem(position)?.also { item ->
            holder.itemBinding.apply {
                ViewCompat.setAccessibilityHeading(appHeaderContainer, true)
                appIcon.apply {
                    Glide.with(context)
                        .load(item.packageIcon)
                        .placeholder(R.mipmap.generic_app_icon)
                        .centerCrop()
                        .into(this)
                }
                appLabel.text = Html.fromHtml(item.packageLabel).toString()
                appCategory.text = item.category.split("_").mapNotNull {
                    it.lowercase().replaceFirstChar { it.uppercase() }
                }.joinToString(" ")
                appRating.apply {
                    val ratingsText = item.numberOfRatings.takeUnless { it == 0 }?.let { numberOfRatings ->
                        context.resources.getQuantityString(
                            R.plurals.appitem__ratings_details,
                            numberOfRatings,
                            item.totalRating?.toRatingString(),
                            numberOfRatings
                        )
                    } ?: context.getString(R.string.appitem__no_ratings)
                    text = ratingsText
                }
                appPacks.apply {
                    val  labelsText = item.availablePacks.takeUnless { it == 0 }?.let { availablePacks ->
                        context.resources.getQuantityString(
                            R.plurals.appitem__labels_details,
                            availablePacks,
                            availablePacks
                        )
                    } ?: context.getString(R.string.appitem__no_labels)
                    text = labelsText
                }
                root.setOnClickListener {
                    clickListener(item)
                }
            }
        }
    }
}
