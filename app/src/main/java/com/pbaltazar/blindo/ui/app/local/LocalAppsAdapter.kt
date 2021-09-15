package com.pbaltazar.blindo.ui.app.local

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blindoapp.uitools.recyclerview.Adapter
import com.blindoapp.uitools.recyclerview.ViewHolder
import com.pbaltazar.blindo.databinding.ItemAppBinding
import com.pbaltazar.blindo.entities.App

class LocalAppsAdapter(
    private val clickListener: (App) -> (Unit)
) : Adapter<App>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        LocalAppsViewHolder(
            ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bind(item: App, viewHolder: ViewHolder) {
        (viewHolder as LocalAppsViewHolder).itemBind.apply {
            appIcon.apply {
                setImageDrawable(context.packageManager.getApplicationIcon(item.packageName))
            }
            appLabel.text = item.packageLabel
            appCategory.text = item.packageName
            appRating.visibility = View.GONE
            appPacks.visibility = View.GONE
            root.setOnClickListener {
                clickListener(item)
            }
        }
    }
}
