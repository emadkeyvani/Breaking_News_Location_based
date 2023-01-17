package com.keyvani.breaking_news.db.search

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keyvani.breaking_news.utils.Constants.SEARCH_QUERY_REMOTE_KEYS

@Dao
interface SearchQueryRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKey(remoteKey: SearchQueryRemoteKey)

    @Query("SELECT * FROM $SEARCH_QUERY_REMOTE_KEYS WHERE searchQuery = :searchQuery")
    suspend fun getRemoteKey(searchQuery: String): SearchQueryRemoteKey

}