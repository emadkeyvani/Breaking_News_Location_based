package com.keyvani.breaking_news.db.search

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.keyvani.breaking_news.utils.Constants.SEARCH_QUERY_REMOTE_KEYS

@Entity(tableName = SEARCH_QUERY_REMOTE_KEYS)
data class SearchQueryRemoteKey(
    @PrimaryKey val searchQuery: String,
    val nextPageKey: Int
)