package com.keyvani.breaking_news.repository


import androidx.room.withTransaction
import com.keyvani.breaking_news.api.ApiServices
import com.keyvani.breaking_news.db.FavNews
import com.keyvani.breaking_news.db.LastNews
import com.keyvani.breaking_news.db.NewsDao
import com.keyvani.breaking_news.db.NewsDatabase
import com.keyvani.breaking_news.utils.Resource
import com.keyvani.breaking_news.utils.networkBoundResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import java.util.concurrent.TimeUnit


class NewsRepository @Inject constructor(
    private val newsApi: ApiServices,
    private val newsDao: NewsDao,
    private val newsDatabase: NewsDatabase
) {


    /**
    returns a Flow of Resource<List<LastNews>>.
    The function takes several parameters: country, pageSize, manualUpdate, onFetchSuccess, and onFetchFailed.
    The function uses the networkBoundResource function to fetch data from the network, save the result and return it.
    The function uses the newsDao to access the local data and newsApi to access the remote data.
    It maps the remote result to a new LastNews object and then uses the favoritedNews data to set
    the isFav flag of the new object.
     */
    fun getLastNews(
        country: String,
        pageSize: Int,
        manualUpdate: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit,
    ): Flow<Resource<List<LastNews>>> =
        networkBoundResource(
            query = {
                newsDao.getLastNewsList()
            },
            fetch = {
                val response = newsApi.getLastNews(country, pageSize)
                response.articles
            },
            saveFetchResult = { remoteResult ->
                val favoritedNews = newsDao.getAllFavNews().first()
                val lastNewsList =
                    remoteResult.map { remoteResults ->
                        val isFav = favoritedNews.any {
                            it.url == remoteResults.url
                        }
                        LastNews(
                            title = remoteResults.title,
                            url = remoteResults.url,
                            imgUrl = remoteResults.urlToImage,
                            isFav = isFav
                        )
                    }

                val favNews = lastNewsList.map { article ->
                    FavNews(article.url)
                }

                newsDatabase.withTransaction {
                    newsDao.deleteAllFav()
                    newsDao.addNewsIntoDB(lastNewsList)
                    newsDao.addNewsIntoFav(favNews)
                }
            },

            /**
            This code is part of the networkBoundResource function and specifically the shouldFetch lambda of the getLastNews() function.
            It is used to determine whether or not to fetch new data from the network. It takes a single parameter localNews,
            which is the data that is stored locally.
            If manualUpdate is true, the function returns true, indicating that new data should be fetched from the network.
            If manualUpdate is false, the function sorts the localNews by the updatedTime field in ascending order.
            It then checks the updatedTime of the oldest news item. If it is null or older than 60 minutes, the function returns true,
            indicating that new data should be fetched from the network, otherwise it returns false.
             */
            shouldFetch = { localNews ->
                if (manualUpdate) true else localNews.minByOrNull { it.updatedTime }?.updatedTime
                    ?.let { it < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(60) } ?: true
            },
            onFetchSuccess = onFetchSuccess,
            onFetchFailed = { t ->
                if (t !is HttpException && t !is IOException) {
                    throw t
                }
                onFetchFailed(t)
            }
        )

    //gets List of fav news local
    fun getAllFavNews(): Flow<List<LastNews>> =
        newsDao.getAllFavNews()

    //updates local news
    suspend fun updateNews(article: LastNews) {
        newsDao.updateNews(article)
    }



    //reset local fav list
    suspend fun favListReset() {
        newsDao.favListReset()
    }

    suspend fun searchNews(query: String, page :Int,pageSize: Int) = newsApi.searchLastNews(query,page,pageSize)


}