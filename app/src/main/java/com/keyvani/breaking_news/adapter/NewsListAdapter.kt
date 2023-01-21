package com.keyvani.breaking_news.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.keyvani.breaking_news.R
import com.keyvani.breaking_news.databinding.ItemNewsBinding
import com.keyvani.breaking_news.db.LastNews


class NewsListAdapter(
    private val itemClick: (LastNews) -> Unit,
    private val favClick: (LastNews) -> Unit,
) : ListAdapter<LastNews, NewsListAdapter.ViewHolder>(NewsDiffUtils()) {

    private lateinit var binding: ItemNewsBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(


            binding,
            itemClick = { pos ->
                val news = getItem(pos)
                if (news != null) {
                    itemClick(news)
                }
            },
            favClick = { pos ->
                val news = getItem(pos)
                if (news != null) {
                    favClick(news)
                }
            }
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    /**
    A ViewHolder inner class in an adapter class in Kotlin is used to hold references to the views in the layout
    for a single item in the adapter's data set. The purpose of the ViewHolder is to improve the performance of
    the adapter by caching the views and avoiding the need to look them up each time the adapter needs to bind data
    to the item's views. The ViewHolder class is typically defined as an inner class within the adapter class,
    and it holds references to the views in the item layout that are used to display data.
     */
    inner class ViewHolder(
        private val binding: ItemNewsBinding,
        private val itemClick: (Int) -> Unit,
        private val favClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: LastNews) {
            binding.apply {
                Glide.with(itemView)
                    .load(article.imgUrl)
                    .error(R.drawable.image_placeholder)
                    .into(imageView)

                textViewTitle.text = article.title ?: ""

                imageViewBookmark.apply {
                    setImageResource(
                        when {
                            article.isFav -> R.drawable.baseline_favorite_24
                            else -> R.drawable.baseline_favorite_border_24
                        }
                    )
                    // Click handling on items
                    setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            favClick(pos)
                        }
                    }
                }

                root.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        itemClick(pos)
                    }
                }

            }
        }
    }
}

/**
it's being used to compare two items of type LastNews in a RecyclerView.
The method areItemsTheSame compares the url property of the two LastNews objects and returns true if they are the same, otherwise false
The method areContentsTheSame compares the two LastNews objects and returns true if they are the same, otherwise false.
 */

class NewsDiffUtils : DiffUtil.ItemCallback<LastNews>() {

    override fun areItemsTheSame(oldItem: LastNews, newItem: LastNews) =
        oldItem.url == newItem.url

    override fun areContentsTheSame(oldItem: LastNews, newItem: LastNews) =
        oldItem == newItem
}