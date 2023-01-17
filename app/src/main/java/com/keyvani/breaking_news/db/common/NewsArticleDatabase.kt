package com.keyvani.breaking_news.db.common

import androidx.room.Database
import androidx.room.RoomDatabase
import com.keyvani.breaking_news.db.search.SearchQueryRemoteKey
import com.keyvani.breaking_news.db.search.SearchQueryRemoteKeyDao

@Database(
    entities = [NewsArticle::class, BreakingNews::class, SearchResult::class, SearchQueryRemoteKey::class],
    version = 2
)
abstract class NewsArticleDatabase : RoomDatabase() {

    abstract fun newsArticleDao(): NewsArticleDao

    abstract fun searchQueryRemoteKeyDao(): SearchQueryRemoteKeyDao

}