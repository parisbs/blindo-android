package com.pbaltazar.blindo.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.pbaltazar.blindo.databinding.FragmentSearchBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.ui.home.HomeAdapter
import com.pbaltazar.blindo.ui.home.HomeComparator
import com.pbaltazar.blindo.utils.pagination.ui.PaginationStateAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModel()
    private val searchFragmentArgs: SearchFragmentArgs by navArgs()
    private var binding: FragmentSearchBinding? = null

    private lateinit var searchRecycler: RecyclerView

    private val searchAdapter: HomeAdapter = HomeAdapter(
        HomeComparator,
        { app ->
            onAppClickListener(app)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchViewModel.setQuery(searchFragmentArgs.query)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        searchRecycler = binding!!.searchRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun setupUi() {
        searchRecycler.adapter = searchAdapter.withLoadStateFooter(
            footer = PaginationStateAdapter({ searchAdapter.retry() })
        )
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.searchResults.collectLatest { pagingData: PagingData<App> ->
                searchAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
            }
        }
    }

    private fun onAppClickListener(app: App) {
        findNavController().navigate(
            SearchFragmentDirections.actionFromSearchToAppDetails(app)
        )
    }
}
