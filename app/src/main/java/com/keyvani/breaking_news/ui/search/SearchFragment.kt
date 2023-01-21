package com.keyvani.breaking_news.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keyvani.breaking_news.R
import com.keyvani.breaking_news.adapter.SearchAdapter
import com.keyvani.breaking_news.databinding.FragmentSearchBinding
import com.keyvani.breaking_news.utils.Resource
import com.keyvani.breaking_news.utils.isVisible
import com.keyvani.breaking_news.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val searchAdapter = SearchAdapter(
            itemClick = {
                val uri = Uri.parse(it.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                requireActivity().startActivity(intent)
            }
        )


        //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter.StateRestorationPolicy
        //Defines how this Adapter wants to restore its state after a view reconstruction
        searchAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.apply {

            rvSearch.apply {
                adapter = searchAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ItemAnimator
                //This class defines the animations that take place on items as changes are made to the adapter.
                //Subclasses of ItemAnimator can be used to implement custom animations for actions on ViewHolder items
                itemAnimator?.changeDuration = 0
            }

            //Getting search result from remote source
            searchViewModel.searchNews.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Success -> {
                        pbSearch.isVisible(false, rvSearch)
                        result.value?.let {
                            if (it.articles.isEmpty()) {
                                emptyLayout.isVisible(true, rvSearch)
                            } else {
                                emptyLayout.isVisible(false, rvSearch)
                                searchAdapter.submitList(it.articles)
                            }
                        }
                    }
                    is Resource.Error -> {
                        pbSearch.isVisible(false, rvSearch)
                        pbSearch.isVisible(false, emptyLayout)
                        result.errorMessage.let {
                            Timber.i(it)
                        }
                    }
                    is Resource.Loading -> {
                        pbSearch.isVisible(true, rvSearch)
                        pbSearch.isVisible(true, emptyLayout)
                    }

                }
            }
        }

        setHasOptionsMenu(true)

    }


    //Providing search functionality for magnifier icon in menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchViewModel.searchNews(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

}