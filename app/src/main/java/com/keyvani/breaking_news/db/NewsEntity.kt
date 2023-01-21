package com.keyvani.breaking_news.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.keyvani.breaking_news.utils.Constants.FAVORITE_TABLE
import com.keyvani.breaking_news.utils.Constants.LAST_NEWS_TABLE
import com.keyvani.breaking_news.utils.Constants.SEARCH_RESULTS_TABLE

@Entity(tableName = LAST_NEWS_TABLE)
data class LastNews(
    val title: String?,
    @PrimaryKey val url: String,
    val imgUrl: String?,
    val isFav: Boolean,
    val updatedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = FAVORITE_TABLE)
data class FavNews(
    val newsLink: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Entity(tableName = SEARCH_RESULTS_TABLE, primaryKeys = ["searchQuery", "articleUrl"])
data class SearchResult(
    val searchQuery: String,
    val articleUrl: String,
    val queryPosition: Int
)