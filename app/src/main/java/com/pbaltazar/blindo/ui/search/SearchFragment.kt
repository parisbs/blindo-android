package com.pbaltazar.blindo.ui.search

import android.content.Context
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentSearchBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.ui.home.HomeAdapter
import com.pbaltazar.blindo.ui.home.HomeComparator
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.pbaltazar.blindo.utils.pagination.ui.PaginationStateAdapter
import com.pbaltazar.blindo.utils.search.RecentSearchesProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : BlindoFragment<FragmentSearchBinding>() {

    private val searchViewModel: SearchViewModel by viewModel()
    private val searchFragmentArgs: SearchFragmentArgs by navArgs()

    private lateinit var searchRecycler: RecyclerView

    private val searchAdapter: HomeAdapter = HomeAdapter(
        HomeComparator
    ) { app ->
        onAppClickListener(app)
    }

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SearchRecentSuggestions(this.requireContext(), RecentSearchesProvider.AUTHORITY, RecentSearchesProvider.MODE)
            .saveRecentQuery(searchFragmentArgs.query, null)
        searchViewModel.setQuery(searchFragmentArgs.query)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = this@SearchFragment.getString(
                R.string.search__app_title,
                searchFragmentArgs.query
            )
            subtitle = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        searchRecycler = binding!!.searchRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        searchRecycler.adapter = searchAdapter.withLoadStateFooter(
            footer = PaginationStateAdapter { searchAdapter.retry() }
        )
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.searchResults.collectLatest { pagingData: PagingData<App> ->
                searchAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
            }
        }
    }

    private fun onAppClickListener(app: App) {
        findNavController().navigate(
            SearchFragmentDirections.actionFromSearchToAppDetails(
                packageName = app.packageName,
                app = app
            )
        )
    }
}
