package com.keyvani.breaking_news.adapter.common

import androidx.recyclerview.widget.DiffUtil
import com.keyvani.breaking_news.db.common.NewsArticle

class NewsArticleComparator : DiffUtil.ItemCallback<NewsArticle>() {

    override fun areItemsTheSame(oldItem: NewsArticle, newItem: NewsArticle) =
        oldItem.url == newItem.url

    override fun areContentsTheSame(oldItem: NewsArticle, newItem: NewsArticle) =
        oldItem == newItem
}