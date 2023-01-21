package com.keyvani.breaking_news.utils

import com.keyvani.breaking_news.BuildConfig

object Constants {

    const val NETWORK_TIMEOUT = 60L

    const val BASE_URL = "https://newsapi.org/v2/"
    const val API_KEY: String = BuildConfig.NEWS_API_ACCESS_KEY
    var COUNTRY_CODE = ""
    const val PAGE_SIZE = 100

    const val NEWS_DATABASE = "news_database"
    const val LAST_NEWS_TABLE = "last_news_table"
    const val FAVORITE_TABLE = "favorite_table"

    const val SEARCH_RESULTS_TABLE = "search_results_table"




}

