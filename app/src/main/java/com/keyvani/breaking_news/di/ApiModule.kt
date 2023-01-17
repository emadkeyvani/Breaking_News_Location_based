package com.keyvani.breaking_news.di

import com.keyvani.breaking_news.api.ApiServices
import com.keyvani.breaking_news.utils.Constants.BASE_URL
import com.keyvani.breaking_news.utils.Constants.NETWORK_TIMEOUT
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideBaseUrl() = BASE_URL

    @Provides
    @Singleton
    fun provideConnectionTimeout() = NETWORK_TIMEOUT

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String, gson: Gson): ApiServices =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiServices::class.java)
}