package com.keyvani.breaking_news.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.keyvani.breaking_news.R
import com.keyvani.breaking_news.databinding.ItemNewsBinding
import com.keyvani.breaking_news.response.RemoteDto

class SearchAdapter(
    private val itemClick: (RemoteDto) -> Unit,
) : ListAdapter<RemoteDto, SearchAdapter.ViewHolder>(SearchDiffUtils()) {

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
            }
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    inner class ViewHolder(
        private val binding: ItemNewsBinding,
        private val itemClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: RemoteDto) {
            binding.apply {
                Glide.with(itemView)
                    .load(article.urlToImage)
                    .error(R.drawable.image_placeholder)
                    .into(imageView)

                textViewTitle.text = article.title ?: ""
                root.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        itemClick(pos)
                    }
                }
                imageViewBookmark.visibility = View.INVISIBLE
            }
        }
    }
}

/**
it's being used to compare two items of type LastNews in a RecyclerView.
The method areItemsTheSame compares the url property of the two LastNews objects and returns true if they are the same, otherwise false
The method areContentsTheSame compares the two LastNews objects and returns true if they are the same, otherwise false.
 */

class SearchDiffUtils : DiffUtil.ItemCallback<RemoteDto>() {
    override fun areItemsTheSame(oldItem: RemoteDto, newItem: RemoteDto) =
        oldItem.url == newItem.url

    override fun areContentsTheSame(oldItem: RemoteDto, newItem: RemoteDto) =
        oldItem == newItem
}

