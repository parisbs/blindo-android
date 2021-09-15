package com.pbaltazar.blindo.utils.pagination.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ItemPaginationLoadStateBinding

class PaginationStateAdapter(
    val retryCall: () -> Unit
) : LoadStateAdapter<PaginationStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): PaginationStateViewHolder =
        PaginationStateViewHolder(
            ItemPaginationLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: PaginationStateViewHolder, loadState: LoadState) {
        holder.itemBinding.apply {
            retry.setOnClickListener { retryCall() }
            when (loadState) {
                is LoadState.NotLoading -> {
                    this.loadState.visibility = View.GONE
                    retry.visibility = View.GONE
                }
                is LoadState.Loading -> {
                    retry.visibility = View.GONE
                    this.loadState.apply {
                        text = context.getString(R.string.viewstate__loading_title)
                        visibility = View.VISIBLE
                    }
                }
                is LoadState.Error -> {
                    this.loadState.apply {
                        text = loadState.error.localizedMessage ?: loadState.error.toString()
                        visibility = View.VISIBLE
                    }
                    retry.visibility = View.VISIBLE
                }
            }
        }
    }
}
