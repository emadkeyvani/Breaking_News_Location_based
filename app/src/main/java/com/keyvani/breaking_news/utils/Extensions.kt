package com.keyvani.breaking_news.utils

import android.view.View
import androidx.appcompat.widget.SearchView


//A function for visibility handling in different situations
fun View.isVisible(isShowLoading: Boolean, container: View) {
    if (isShowLoading) {
        this.visibility = View.VISIBLE
        container.visibility = View.GONE
    } else {
        this.visibility = View.GONE
        container.visibility = View.VISIBLE
    }
}