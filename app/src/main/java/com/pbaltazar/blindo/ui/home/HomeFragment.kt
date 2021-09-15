package com.pbaltazar.blindo.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentHomeBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.enums.AppSort
import com.pbaltazar.blindo.utils.constants.APP_SORT
import com.pbaltazar.blindo.utils.pagination.ui.PaginationStateAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModel()
    private var binding: FragmentHomeBinding? = null

    private lateinit var homeRecycler: RecyclerView

    private val homeAdapter: HomeAdapter = HomeAdapter(
        HomeComparator,
        { app ->
            onAppClickListener(app)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        homeRecycler = binding!!.homeRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeFilters()
        setupUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.homeFilter -> {
                findNavController().navigate(
                    HomeFragmentDirections.actionFromHomeToAppsFilter()
                )
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun subscribeFilters() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<List<AppSort>>(
        APP_SORT)?.observe(this, Observer {
            homeAdapter.refresh()
    })

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
