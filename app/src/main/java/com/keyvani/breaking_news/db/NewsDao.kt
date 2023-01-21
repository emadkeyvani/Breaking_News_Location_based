package com.keyvani.breaking_news.db

import androidx.paging.PagingSource
import androidx.room.*
import com.keyvani.breaking_news.utils.Constants.FAVORITE_TABLE
import com.keyvani.breaking_news.utils.Constants.LAST_NEWS_TABLE
import com.keyvani.breaking_news.utils.Constants.SEARCH_RESULTS_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {

    //getting last news from local source
    @Query("SELECT * FROM $FAVORITE_TABLE INNER JOIN $LAST_NEWS_TABLE ON newsLink = url")
    fun getLastNewsList(): Flow<List<LastNews>>

    //adding news to local source
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNewsIntoDB(articles: List<LastNews>)

    //Making a favorite in database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNewsIntoFav(favNews: List<FavNews>)

    //Deleting favorites from database
    @Query("DELETE FROM $FAVORITE_TABLE")
    suspend fun deleteAllFav()

    //Getting favorite list from data base
    @Query("SELECT * FROM $LAST_NEWS_TABLE WHERE isFav = 1")
    fun getAllFavNews(): Flow<List<LastNews>>

    //Update news in database
    @Update
    suspend fun updateNews(news: LastNews)


    @Query("UPDATE $LAST_NEWS_TABLE SET isFav = 0")
    suspend fun favListReset()





}