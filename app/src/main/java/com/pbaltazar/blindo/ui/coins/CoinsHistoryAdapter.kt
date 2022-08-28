package com.pbaltazar.blindo.ui.coins

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.blindo.apollito.utils.extensions.toTimeAgo
import com.blindoapp.uitools.recyclerview.Adapter
import com.blindoapp.uitools.recyclerview.ViewHolder
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ItemCoinsHistoryBinding
import com.pbaltazar.blindo.entities.Coin
import com.pbaltazar.blindo.entities.enums.CoinState
import com.pbaltazar.blindo.entities.enums.CoinType
import com.pbaltazar.blindo.entities.purchases.inapp.InApp

class CoinsHistoryAdapter : Adapter<Coin>() {

    var coinsProducts: List<InApp> = emptyList()

    @SuppressLint("SetTextI18n")
    override fun bind(item: Coin, viewHolder: ViewHolder) {
        (viewHolder as CoinsHistoryViewHolder).itemBinding.apply {
            title.apply {
                ViewCompat.setAccessibilityHeading(this, true)
                coinsProducts.first { it.id == item.productId }.also { inApp ->
                    text = inApp.name
                }
            }
            orderId.apply {
                text = item.latestPurchase.orderId
            }
            isAcknowledged.apply {
                val state = root.context.getString(
                    when (item.state) {
                        CoinState.PENDING -> R.string.coins__purchase_state_pending
                        CoinState.PURCHASED -> R.string.coins__purchase_state_purchased
                        CoinState.CANCELED -> R.string.coins__purchase_state_canceled
                    }
                )
                val acknowledged = root.context.getString(
                    if (item.latestPurchase.isAcknowledged) R.string.coins__acknowledged
                else R.string.coins__not_acknowledged
                )
                text = "$state (${acknowledged})"
            }
            purchaseType.apply {
                text = root.context.getString(
                    when (item.type) {
                        CoinType.STANDARD -> R.string.coins__type_standard
                        CoinType.PROMO -> R.string.coins__type_promo
                        CoinType.REWARDED -> R.string.coins__type_rewarded
                        CoinType.TEST -> R.string.coins__type_test
                    }
                )
            }
            purchasedAt.apply {
                text = item.latestPurchase.purchasedAt.toTimeAgo()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = CoinsHistoryViewHolder(
        ItemCoinsHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
}
