package com.keyvani.breaking_news.di

import com.keyvani.breaking_news.api.ApiServices
import com.keyvani.breaking_news.utils.Constants.BASE_URL
import com.keyvani.breaking_news.utils.Constants.NETWORK_TIMEOUT
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.keyvani.breaking_news.BuildConfig
import com.keyvani.breaking_news.utils.Constants.API_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun provideBaseUrl() = BASE_URL

    @Provides
    @Singleton
    fun provideConnectionTimeout() = NETWORK_TIMEOUT

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    /**
    The client is configured with a logging interceptor and a request interceptor.
    The logging interceptor is set to log headers and body information, and the request interceptor adds an apiKey
    query parameter to the client's requests using the value of a constant API_KEY.
    The function will only be executed if the value of BuildConfig.DEBUG is true.
     */
    @Singleton
    @Provides
    fun provideClient() = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val requestInterceptor = Interceptor { chain ->
            val url = chain.request()
                .url
                .newBuilder()

                //Sending APIKEY as a query parameter in header
                .addQueryParameter("apiKey", API_KEY)
                .build()

            val request = chain.request()
                .newBuilder()
                .url(url)
                .build()
            return@Interceptor chain.proceed(request)
        }

        OkHttpClient
            .Builder()
            .addInterceptor(requestInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    } else {
        OkHttpClient
            .Builder()
            .build()
    }

    /**
    This code defines a function called provideRetrofit() that returns an instance of the ApiServices interface.
    The function takes three parameters: baseUrl, gson, and client. baseUrl is the base URL for the API,
    gson is a Gson object used for serialization and deserialization of JSON, and client is an OkHttp client.
    The function creates a Retrofit object using the provided baseUrl, client, and gson and then uses the Retrofit
    object to create an implementation of the ApiServices interface.
    This implementation will be used to make API calls.
     */
    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String, gson: Gson, client: OkHttpClient): ApiServices =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiServices::class.java)
}