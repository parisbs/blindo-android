package com.pbaltazar.blindo.ui.app.details.pages.packs

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blindoapp.uitools.recyclerview.PaginationScrollListener
import com.pbaltazar.blindo.MainNavigationDirections
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentAppPacksBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.connections.PackConnection
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.inputs.PackInput
import com.pbaltazar.blindo.ui.app.details.AppFragmentDirections
import com.pbaltazar.blindo.ui.app.details.AppViewModel
import com.pbaltazar.blindo.ui.app.details.pages.AppPagerHelper
import com.pbaltazar.blindo.ui.filter.FilterableFragment
import com.pbaltazar.blindo.ui.filter.FiltersSet
import com.wizeline.viewstate.State
import com.wizeline.viewstate.ViewState

class AppPacksFragment : FilterableFragment() {

    private lateinit var appViewModel: AppViewModel
    private var binding: FragmentAppPacksBinding? = null

    private lateinit var appPacksViewState: ViewState
    private lateinit var appPacksRecycler: RecyclerView
    private val appPacksAdapter: AppPacksAdapter = AppPacksAdapter({ pack ->
        packOnClickListener(pack)
    })

    private var currentApp: App? = null
    private var isLoading: Boolean = false
    private var hasNextPage: Boolean = false
    private var nextPageToken: String? = null
    private var requiresRefresh: Boolean = false

    override val filtersSet: FiltersSet
        get() = FiltersSet.APP_PACKS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appViewModel = AppPagerHelper.appViewModelListener.getAppViewModel()!!
        currentApp = AppPagerHelper.appViewModelListener.getCurrentApp()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAppPacksBinding.inflate(inflater, container, false)
        appPacksViewState = binding!!.appPacksViewState
        appPacksRecycler = binding!!.appPacksRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribePacks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        if (currentApp == null && requiresRefresh.not()) {
            currentApp = AppPagerHelper.appViewModelListener.getCurrentApp()
        }
        if (currentApp?.availablePacks ?: 0 > 0 && currentApp?.packs == null) {
            loadPacks()
        } else {
            setupApp()
        }
    }

    override fun onFiltersChange(isChanged: Boolean) {
        requiresRefresh = isChanged
        if (requiresRefresh) {
            currentApp?.also {
                currentApp = it.copy(
                    packs = null
                )
            }
            appPacksAdapter.clearItems()
        }
    }

    private fun subscribePacks() = appViewModel.packs.observe(this, Observer {
        when (val response = it) {
            is AppViewModel.PacksList.Success -> {
                if (requiresRefresh) {
                    currentApp?.also { app ->
                        currentApp = app.copy(
                            packs = PackConnection(
                                packs = response.packs,
                                hasNextPage = response.hasNextPage,
                                nextPageToken = response.nextPageToken
                            )
                        )
                    }
                    requiresRefresh = false
                }
                if (appPacksAdapter.itemCount < 1) {
                    appPacksViewState.setState(State.CONTENT)
                }
                if (appPacksAdapter.items.containsAll(response.packs).not()) {
                    appPacksAdapter.appendItems(response.packs)
                    hasNextPage = response.hasNextPage
                    nextPageToken = response.nextPageToken
                }
            }
            is AppViewModel.PacksList.Empty -> {
                if (appPacksAdapter.itemCount < 1) {
                    appPacksViewState.setState(State.EMPTY)
                }
            }
            is AppViewModel.PacksList.Error -> {
                if (appPacksAdapter.itemCount < 1) {
                    appPacksViewState.apply {
                        setErrorDescriptionText(response.reason)
                        setState(State.ERROR)
                    }
                }
            }
        }
        isLoading = false
    })

    private fun loadPacks() {
        if (isLoading.not()) {
            isLoading = true
            if (appPacksAdapter.itemCount < 1) {
                appPacksViewState.setState(State.LOADING)
                hasNextPage = false
                nextPageToken = null
            }
            appViewModel.loadPacks(
                AppInput(
                    id = if (appViewModel.getIsQueryById()) currentApp?.id ?: "" else "",
                    packageName = if (appViewModel.getIsQueryById().not()) currentApp?.packageName ?: "" else "",
                    packInput = PackInput(
                        sort = appViewModel.getPackSort(),
                        pageSize = appViewModel.getPacksPageSize(),
                        nextPageToken = nextPageToken
                    )
                )
            )
        }
    }

    private fun setupUi() {
        isLoading = true
        appPacksViewState.setState(State.LOADING)
        appPacksRecycler.apply {
            adapter = appPacksAdapter
            addOnScrollListener(object : PaginationScrollListener(
                layoutManager?.let { it as LinearLayoutManager } ?: LinearLayoutManager(this@AppPacksFragment.requireContext())
            ) {
                override fun hasNextPage(): Boolean = this@AppPacksFragment.hasNextPage

                override fun prefetchDistance(): Int = 10

                override fun isLoading(): Boolean = this@AppPacksFragment.isLoading

                override fun loadMoreItems() = loadPacks()
            })
        }
        appPacksViewState.setOnRetryClickListener {
            loadPacks()
        }
        setupApp()
    }

    private fun setupApp() {
        currentApp?.packs?.also { packConnection ->
            packConnection.packs?.takeIf { it.isNotEmpty() }?.also { packs ->
                if (appPacksAdapter.itemCount < 1) {
                    appPacksAdapter.appendItems(packs)
                    hasNextPage = packConnection.hasNextPage
                    nextPageToken = packConnection.nextPageToken
                }
                appPacksViewState.setState(State.CONTENT)
                isLoading = false
            } ?: appPacksViewState.setState(State.EMPTY)
        } ?: appPacksViewState.setState(State.ERROR)
    }

    private fun packOnClickListener(pack: Pack) {
        findNavController().navigate(
            AppFragmentDirections.actionFromAppDetailsToPackDetails(pack)
        )
    }
}
