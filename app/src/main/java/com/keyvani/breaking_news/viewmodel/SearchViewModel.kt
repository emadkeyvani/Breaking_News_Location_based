package com.keyvani.breaking_news.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyvani.breaking_news.repository.NewsRepository
import com.keyvani.breaking_news.response.RemoteResponse
import com.keyvani.breaking_news.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: NewsRepository) :ViewModel(){

    private val _searchNews : MutableLiveData<Resource<RemoteResponse>> = MutableLiveData()
    val searchNews : LiveData<Resource<RemoteResponse>>
    get() = _searchNews

    var searchNewsPage = 1
    var searchPageSize = 100

    fun searchNews (query : String) = viewModelScope.launch {
        _searchNews.postValue(Resource.Loading())
        val response = repository.searchNews(query,searchNewsPage, searchPageSize )
        _searchNews.postValue(searchHandler(response))
    }

    /**
     This function is used is used to handle events or actions that are triggered by the UI.
     It is used to update the state of the ViewModel and to perform operations that affect the UI,
     such as navigating to a different screen or showing a message.
     */
    private fun searchHandler(response: Response<RemoteResponse>) : Resource<RemoteResponse>{

        if (response.isSuccessful){
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }

}