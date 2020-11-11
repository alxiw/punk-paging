package io.github.alxiw.punkpaging.ui.beers

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.alxiw.punkpaging.R
import io.github.alxiw.punkpaging.databinding.ItemLoadingStateBinding

class BeersLoadingAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<BeersLoadingAdapter.LoadingStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadingStateViewHolder {
        return LoadingStateViewHolder.create(parent, retry)
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bindState(loadState)
    }

    class LoadingStateViewHolder(
        binding: ItemLoadingStateBinding,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val tvErrorMessage: TextView = binding.tvErrorMessage
        private val progressBar: ProgressBar = binding.progressBar
        private val btnRetry: Button = binding.btnRetry

        init {
            btnRetry.setOnClickListener {
                retry.invoke()
            }
        }

        fun bindState(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                tvErrorMessage.text = loadState.error.message
            }
            progressBar.isVisible = loadState is LoadState.Loading
            tvErrorMessage.isVisible = loadState !is LoadState.Loading
            btnRetry.isVisible = loadState !is LoadState.Loading
        }

        companion object {
            fun create(parent: ViewGroup, retry: () -> Unit): LoadingStateViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading_state,  parent,false)

                val binding = ItemLoadingStateBinding.bind(view)

                return LoadingStateViewHolder(
                    binding,
                    retry
                )
            }
        }
    }
}
