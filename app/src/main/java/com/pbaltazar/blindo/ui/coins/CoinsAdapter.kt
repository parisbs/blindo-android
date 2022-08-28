package com.pbaltazar.blindo.ui.coins

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.blindoapp.uitools.recyclerview.Adapter
import com.blindoapp.uitools.recyclerview.ViewHolder
import com.pbaltazar.blindo.databinding.ItemCoinBinding
import com.pbaltazar.blindo.entities.purchases.inapp.InApp

class CoinsAdapter(
    val onItemClickListener: (item: InApp) -> Unit
) : Adapter<InApp>() {

    @SuppressLint("SetTextI18n")
    override fun bind(item: InApp, viewHolder: ViewHolder) {
        (viewHolder as CoinsViewHolder).itemBinding.apply {
            title.text = item.name
            item.offer?.pricingPhase?.also { pricingPhase ->
                cost.text = "${pricingPhase.formatedPrice} ${pricingPhase.priceCurrencyCode}"
            }
            root.apply {
                ViewCompat.setAccessibilityHeading(this, true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tooltipText = item.description
                }
                setOnClickListener { onItemClickListener(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        CoinsViewHolder(ItemCoinBinding.inflate(LayoutInflater.from(parent.context), parent, false))
}
