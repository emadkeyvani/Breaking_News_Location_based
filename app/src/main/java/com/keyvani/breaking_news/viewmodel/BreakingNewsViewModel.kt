package com.keyvani.breaking_news.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyvani.breaking_news.db.common.NewsArticle
import com.keyvani.breaking_news.repository.NewsRepository
import com.keyvani.breaking_news.utils.Constants.COUNTRY_CODE
import com.keyvani.breaking_news.utils.DataStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class BreakingNewsViewModel @Inject constructor(
    private val repository: NewsRepository,
) : ViewModel() {

   // private val countryCode = COUNTRY_CODE
    private val refreshTriggerChannel = Channel<Refresh>()
    private val refreshTrigger = refreshTriggerChannel.receiveAsFlow()
    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()
    var pendingScrollToTopAfterRefresh = false

    val breakingNews = refreshTrigger
        .flatMapLatest { refresh ->
            repository.getBreakingNews(
                COUNTRY_CODE,
                refresh == Refresh.FORCE,
                onFetchSuccess = {
                    pendingScrollToTopAfterRefresh = true
                },
                onFetchFailed = { t ->
                    viewModelScope.launch {
                        eventChannel.send(Event.ShowErrorMessage(t))
                    }
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            repository.deleteNonBookmarkedArticlesOlderThan(
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            )
        }
    }

    fun onStart() {
        if (breakingNews.value !is DataStatus.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.NORMAL)
            }
        }
    }

    fun onManualRefresh() {
        if (breakingNews.value !is DataStatus.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.FORCE)
            }
        }
    }

    fun onBookmarkClick(article: NewsArticle) {
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            repository.updateArticle(updatedArticle)
        }
    }

    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }

    enum class Refresh {
        FORCE, NORMAL
    }
}