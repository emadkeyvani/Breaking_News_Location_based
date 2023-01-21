package com.keyvani.breaking_news.ui.lastnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keyvani.breaking_news.R
import com.keyvani.breaking_news.adapter.NewsListAdapter
import com.keyvani.breaking_news.databinding.FragmentLastNewsBinding
import com.keyvani.breaking_news.utils.Resource
import com.keyvani.breaking_news.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LastNewsFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: FragmentLastNewsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLastNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsListAdapter = NewsListAdapter(
            itemClick = {
                val uri = Uri.parse(it.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                requireActivity().startActivity(intent)
            },
            favClick = {
                viewModel.favClick(it)
            }
        )

        //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter.StateRestorationPolicy
        //Defines how this Adapter wants to restore its state after a view reconstruction
        newsListAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY


        binding.apply {

            rvLastNews.apply {
                adapter = newsListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ItemAnimator
                //This class defines the animations that take place on items as changes are made to the adapter.
                //Subclasses of ItemAnimator can be used to implement custom animations for actions on ViewHolder items
                itemAnimator?.changeDuration = 0
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {

                //Getting list of last news from local source
                viewModel.lastNewsList.collect {
                    val result = it ?: return@collect

                    //Handling UI changes duo to list provided situation
                    swipeRefLayout.isRefreshing = result is Resource.Loading
                    rvLastNews.isVisible = !result.value.isNullOrEmpty()
                    tvErrorMsg.isVisible = result.errorMessage != null && result.value.isNullOrEmpty()
                    btnRetry.isVisible = result.errorMessage != null && result.value.isNullOrEmpty()
                    tvErrorMsg.text = getString(R.string.updateFailed, result.errorMessage?: getString(R.string.unexpectedError))

                    newsListAdapter.submitList(result.value) {
                        if (viewModel.autoScrollToTop) {
                            rvLastNews.scrollToPosition(0)
                            viewModel.autoScrollToTop = false
                        }

                    }

                }

            }

            swipeRefLayout.setOnRefreshListener {
                viewModel.manualUpdate()
            }

            btnRetry.setOnClickListener {
                viewModel.manualUpdate()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.cases.collect {
                    when (it) {
                        is MainViewModel.Event.ShowErrorMessage -> {
                            Toast.makeText(
                                requireContext(),
                                getString(
                                    R.string.updateFailed,
                                    it.error.localizedMessage
                                        ?: getString(R.string.unexpectedError)
                                ), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

        }

        setHasOptionsMenu(true)

    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_last_news, menu)
    }

    //Click handling on menu items
    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.btnUpdate -> {
                viewModel.manualUpdate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

}