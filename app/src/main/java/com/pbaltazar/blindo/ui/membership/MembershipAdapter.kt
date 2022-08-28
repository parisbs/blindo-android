package com.pbaltazar.blindo.ui.membership

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blindoapp.uitools.recyclerview.Adapter
import com.blindoapp.uitools.recyclerview.ViewHolder
import com.pbaltazar.blindo.components.subscriptions.SubscriptionInfo
import com.pbaltazar.blindo.entities.purchases.subscriptions.Subscription

class MembershipAdapter(
    private val onOfferSelectedListener: SubscriptionInfo.OnOfferSelectedListener
) : Adapter<Subscription>() {

    override fun bind(item: Subscription, viewHolder: ViewHolder) {
        ((viewHolder as MembershipViewHolder).itemView as SubscriptionInfo).apply {
            setSubscription(item)
            setOnOfferSelectedListener(onOfferSelectedListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MembershipViewHolder(
            SubscriptionInfo(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        )
}
