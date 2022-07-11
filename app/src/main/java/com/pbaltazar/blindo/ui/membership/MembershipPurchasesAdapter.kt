package com.pbaltazar.blindo.ui.membership

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.blindo.apollito.utils.extensions.toTimeAgo
import com.blindoapp.uitools.recyclerview.Adapter
import com.blindoapp.uitools.recyclerview.ViewHolder
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ItemMembershipPurchaseBinding
import com.pbaltazar.blindo.entities.Purchase
import com.pbaltazar.blindo.entities.purchases.enums.ProductType

class MembershipPurchasesAdapter : Adapter<Purchase>() {

    override fun bind(item: Purchase, viewHolder: ViewHolder) {
        (viewHolder as MembershipPurchaseViewHolder).itemBinding.apply {
            ViewCompat.setAccessibilityHeading(orderId, true)
            orderId.text = item.orderId
            acknowledged.text = root.context.getString(
                if (item.isAcknowledged) R.string.membership__subscription_purchase_acknowledged
                else R.string.membership__subscription_purchase_not_acknowledged
            )
            startAt.text = root.context.getString(
                R.string.membership__subscription_purchase_start_at,
                item.startAt.toTimeAgo()
            )
            expireAt.text = root.context.getString(
                R.string.membership__subscription_purchase_expire_at,
                item.expireAt.toTimeAgo()
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_membership_purchase -> MembershipPurchaseViewHolder(
            ItemMembershipPurchaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
        else -> super.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int = when (items[position].kind) {
        ProductType.SUBSCRIPTION -> R.layout.item_membership_purchase
        else -> throw IllegalArgumentException("Invalid product type.")
    }
}
