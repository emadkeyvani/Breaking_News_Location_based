package com.keyvani.breaking_news.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [LastNews::class, FavNews::class, SearchResult::class],
    version = 6
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun newsDao(): NewsDao


}