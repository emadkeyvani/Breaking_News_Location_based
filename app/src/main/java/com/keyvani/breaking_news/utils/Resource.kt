package com.keyvani.breaking_news.utils


//free Article on medium (https://medium.com/dont-code-me-on-that/handle-process-results-with-kotlin-sealed-classes-d9a9375b8e09)
/**
This class is a class that represents a specific type of data that the app is working with,
such as a user or a list of items. It is typically used to model the data that is fetched from a network resource,
such as an API, or from a local cache.
 */
sealed class Resource<T>(
    val value: T? = null,
    val errorMessage: String? = null,
) {
    class Success<T>(value: T) : Resource<T>(value)
    class Loading<T>(value: T? = null) : Resource<T>(value)
    class Error<T>(errorMessage: String, value: T? = null) : Resource<T>(value, errorMessage)
}