package com.keyvani.breaking_news.api

import com.keyvani.breaking_news.response.RemoteResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices{

    //https://newsapi.org/v2/top-headlines?country=us&apiKey=****

    @GET("top-headlines")
    suspend fun getLastNews(
        @Query("country") countryCode: String,
        @Query("pageSize") pageSize:Int
    ): RemoteResponse

    @GET("everything")
    suspend fun searchLastNews(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<RemoteResponse>

}