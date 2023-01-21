package com.keyvani.breaking_news.response

/**
data object classes are used to define the structure of the data,
and they are often used in conjunction with a data access object (DAO) or a service class
to perform operations on the data.
 */
// inspire by an article on wikipedia :
// https://en.wikipedia.org/wiki/Data_transfer_object
data class RemoteDto(
    val title : String?,
    val url : String,
    val urlToImage: String?
)
