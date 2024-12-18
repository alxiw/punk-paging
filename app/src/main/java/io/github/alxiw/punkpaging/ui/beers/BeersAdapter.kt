package io.github.alxiw.punkpaging.ui.beers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.alxiw.punkpaging.R
import io.github.alxiw.punkpaging.data.model.Beer
import io.github.alxiw.punkpaging.databinding.ItemBeerBinding
import io.github.alxiw.punkpaging.ui.ImageLoader
import io.github.alxiw.punkpaging.ui.load
import io.github.alxiw.punkpaging.util.DateFormatter.formatDate

class BeersAdapter(
    private val onItemClicked: (Beer) -> Unit,
    private val imageLoader: ImageLoader
) : PagingDataAdapter<Beer, BeersAdapter.BeersViewHolder>(BEER_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeersViewHolder {
        return BeersViewHolder.create(parent, onItemClicked, imageLoader)
    }

    override fun onBindViewHolder(holder: BeersViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class BeersViewHolder(
        private val binding: ItemBeerBinding,
        private val onItemClicked: (Beer) -> Unit,
        private val imageLoader: ImageLoader
    ) : RecyclerView.ViewHolder(binding.root) {

        private var beer: Beer? = null

        init {
            binding.root.setOnClickListener {
                beer?.let {
                    onItemClicked(it)
                }
            }
        }

        fun bind(beer: Beer) {
            this.beer = beer
            binding.itemId.text = String.format("#%s", beer.id)
            binding.itemName.text = beer.name
            binding.itemTagline.text = beer.tagline
            binding.itemAbv.text = String.format("%s%%", beer.abv)
            binding.itemDate.text = formatDate(beer.firstBrewed, true)
            binding.itemImage.load(imageLoader, beer.image)
        }

        companion object {
            fun create(parent: ViewGroup, onClickAction: (Beer) -> Unit, imageLoader: ImageLoader): BeersViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_beer,  parent,false)

                val binding = ItemBeerBinding.bind(view)

                return BeersViewHolder(binding, onClickAction, imageLoader)
            }
        }
    }

    companion object {
        private val BEER_COMPARATOR =
            object : DiffUtil.ItemCallback<Beer>() {
                override fun areItemsTheSame(oldItem: Beer, newItem: Beer): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: Beer, newItem: Beer): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
