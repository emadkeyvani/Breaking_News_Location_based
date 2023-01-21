package com.keyvani.breaking_news.di

import android.content.Context
import androidx.room.Room
import com.keyvani.breaking_news.db.NewsDatabase
import com.keyvani.breaking_news.utils.Constants.NEWS_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context, NewsDatabase::class.java, NEWS_DATABASE
    )
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()


    @Provides
    @Singleton
    fun provideDao(db : NewsDatabase) = db.newsDao()

}