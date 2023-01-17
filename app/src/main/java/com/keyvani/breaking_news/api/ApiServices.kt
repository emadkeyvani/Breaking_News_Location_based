package com.keyvani.breaking_news.api

import com.keyvani.breaking_news.response.NewsResponse
import com.keyvani.breaking_news.utils.Constants.API_KEY
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiServices{

    //https://newsapi.org/v2/top-headlines?country=us&apiKey=****

    @Headers("X-Api-Key: $API_KEY")
    @GET("top-headlines")
    suspend fun getBreakingNews(
        @Query("country") countryCode: String
    ): NewsResponse

    @Headers("X-Api-Key: $API_KEY")
    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): NewsResponse

}