package com.keyvani.breaking_news.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// inspired by an open source code on GitHub :
// https://github.com/codinginflow/SimpleCachingExample/blob/Part-4_Room-Cache/app/src/main/java/com/codinginflow/simplecachingexample/util/NetworkBoundResource.kt

/**
A networkBoundResource is a class that implements the logic for handling data that is fetched from a network resource,
such as an API.It is used to implement the offline-first approach, where the app always tries to retrieve data from
a local cache first and if it is not available, it fetches the data from the network.
 */
/**
responsibilities:
Retrieve data from a local cache, if available.
Fetch data from the network.
Save the fetched data to the local cache.
Provide a way for the UI to observe the data and update accordingly.
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
    crossinline onFetchSuccess: () -> Unit = { },
    crossinline onFetchFailed: (Throwable) -> Unit = { }
) = channelFlow {
    val data = query().first()

    if (shouldFetch(data)) {
        val loading = launch {
            query().collect { send(Resource.Loading(it)) }
        }

        try {
            saveFetchResult(fetch())
            onFetchSuccess()
            loading.cancel()
            query().collect { send(Resource.Success(it)) }
        } catch (t: Throwable) {
            onFetchFailed(t)
            loading.cancel()
            query().collect { send(Resource.Error(String(), it)) }
        }
    } else {
        query().collect { send(Resource.Success(it)) }
    }
}