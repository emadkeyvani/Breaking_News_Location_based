package com.keyvani.breaking_news.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyvani.breaking_news.db.LastNews
import com.keyvani.breaking_news.repository.NewsRepository
import com.keyvani.breaking_news.utils.Constants.COUNTRY_CODE
import com.keyvani.breaking_news.utils.Constants.PAGE_SIZE
import com.keyvani.breaking_news.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: NewsRepository,
) : ViewModel() {

    private val updateOperatorChannel = Channel<Update>()
    private val updateOperator = updateOperatorChannel.receiveAsFlow()

    private val caseChannel = Channel<Event>()
    val cases = caseChannel.receiveAsFlow()

    var autoScrollToTop = false

    //get last news from remote source
    val lastNewsList = updateOperator

        //flatMapLatest is used to transform an observable sequence of one type into another type.
        .flatMapLatest { update ->
            repository.getLastNews(
                COUNTRY_CODE,
                PAGE_SIZE,
                update == Update.MANUAL,
                onFetchSuccess = {
                    autoScrollToTop = true
                },
                onFetchFailed = { t ->
                    viewModelScope.launch {
                        caseChannel.send(Event.ShowErrorMessage(t))
                    }
                }
            )
        }
        //stateIn  is used to create a composable that represents a stateful value that can be updated by
        // the user or by the system.
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    //get fav list from local source
    val favoriteList = repository.getAllFavNews()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    //auto update on start app
    fun onStart() {
        if (lastNewsList.value !is Resource.Loading) {
            viewModelScope.launch {
                updateOperatorChannel.send(Update.AUTO)
            }
        }
    }

    //manual update by user
    fun manualUpdate() {
        if (lastNewsList.value !is Resource.Loading) {
            viewModelScope.launch {
                updateOperatorChannel.send(Update.MANUAL)
            }
        }
    }

    //check click on fav image and add news to fav list
    fun favClick(news: LastNews) {
        val isFav = news.isFav
        val updatedNews = news.copy(isFav = !isFav)
        viewModelScope.launch {
            repository.updateNews(updatedNews)
        }
    }

    //delete all fav list
    fun deleteAllFav() {
        viewModelScope.launch {
            repository.favListReset()
        }
    }

    //control event
    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }

    //state of update
    enum class Update {
        MANUAL, AUTO
    }

}