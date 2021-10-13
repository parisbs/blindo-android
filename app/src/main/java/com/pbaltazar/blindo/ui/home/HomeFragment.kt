package com.pbaltazar.blindo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.pbaltazar.blindo.databinding.FragmentHomeBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.ui.filter.FilterableFragment
import com.pbaltazar.blindo.ui.filter.FiltersSet
import com.pbaltazar.blindo.utils.pagination.ui.PaginationStateAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : FilterableFragment() {

    private val homeViewModel: HomeViewModel by viewModel()
    private var binding: FragmentHomeBinding? = null

    private lateinit var homeRecycler: RecyclerView

    private val homeAdapter: HomeAdapter = HomeAdapter(
        HomeComparator,
        { app ->
            onAppClickListener(app)
        }
    )

    override val filtersSet: FiltersSet
        get() = FiltersSet.APP

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        homeRecycler = binding!!.homeRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onFiltersChange(isChanged: Boolean) {
        if (isChanged) {
            homeAdapter.refresh()
            homeRecycler.scrollToPosition(0)
        }
    }

    private fun setupUi() {
        homeRecycler.adapter = homeAdapter.withLoadStateFooter(
            footer = PaginationStateAdapter({ homeAdapter.retry() })
        )
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.apps.collectLatest { pagingData: PagingData<App> ->
                homeAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
            }
        }
    }

    private fun onAppClickListener(app: App) {
        findNavController().navigate(
            HomeFragmentDirections.actionFromHomeToAppDetails(app)
        )
    }
}
