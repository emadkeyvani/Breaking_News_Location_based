package com.keyvani.breaking_news.ui.favorite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keyvani.breaking_news.R
import com.keyvani.breaking_news.adapter.NewsListAdapter
import com.keyvani.breaking_news.databinding.FragmentFavoriteBinding
import com.keyvani.breaking_news.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoriteFragment : Fragment(){

    private lateinit var binding: FragmentFavoriteBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val favAdapter = NewsListAdapter(
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
        favAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.apply {
            rvShared.apply {
                adapter = favAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            //Collecting a list of favorite items
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.favoriteList.collect {
                    val fav = it ?: return@collect

                    favAdapter.submitList(fav)
                    tvNoFavoriteNews.isVisible = fav.isEmpty()
                    rvShared.isVisible = fav.isNotEmpty()
                }
            }
        }

        setHasOptionsMenu(true)
    }

    //Showing menu in top of fragment
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_favorite, menu)
    }

    //Click handling for item in menu
    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.btnFavListDelete -> {
                viewModel.deleteAllFav()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

}