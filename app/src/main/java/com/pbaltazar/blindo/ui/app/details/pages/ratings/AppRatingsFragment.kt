package com.pbaltazar.blindo.ui.app.details.pages.ratings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blindoapp.uitools.recyclerview.PaginationScrollListener
import com.pbaltazar.blindo.databinding.FragmentAppRatingsBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.inputs.RatingInput
import com.pbaltazar.blindo.ui.app.details.AppFragmentDirections
import com.pbaltazar.blindo.ui.app.details.AppViewModel
import com.pbaltazar.blindo.ui.app.details.pages.AppPagerHelper
import com.wizeline.viewstate.State
import com.wizeline.viewstate.ViewState

class AppRatingsFragment : Fragment() {

    private lateinit var appViewModel: AppViewModel
    private var binding: FragmentAppRatingsBinding? = null

    private lateinit var appRatingsViewState: ViewState
    private lateinit var appRatingsRecycler: RecyclerView
    private val appRatingsAdapter: AppRatingsAdapter = AppRatingsAdapter({ rating ->
        ratingOnClickListener(rating)
    })

    private var currentApp: App? = null
    private var isLoading: Boolean = false
    private var hasNextPage: Boolean = false
    private var nextPageToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appViewModel = AppPagerHelper.appViewModelListener.getAppViewModel()!!
        currentApp = AppPagerHelper.appViewModelListener.getCurrentApp()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAppRatingsBinding.inflate(inflater, container, false)
        appRatingsViewState = binding!!.appRatingsViewState
        appRatingsRecycler = binding!!.appRatingsRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeRatings()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        currentApp = AppPagerHelper.appViewModelListener.getCurrentApp()
        if (currentApp?.numberOfRatings ?: 0 > 0 && currentApp?.ratings == null) {
            loadRatings()
        } else {
            setupApp()
        }
    }

    private fun subscribeRatings() = appViewModel.ratings.observe(this, Observer {
        when (val response = it) {
            is AppViewModel.RatingsList.Success -> {
                if (appRatingsAdapter.itemCount < 1) {
                    appRatingsViewState.setState(State.CONTENT)
                }
                if (appRatingsAdapter.items.containsAll(response.ratings).not()) {
                    appRatingsAdapter.appendItems(response.ratings)
                    hasNextPage = response.hasNextPage
                    nextPageToken = response.nextPageToken
                }
            }
            is AppViewModel.RatingsList.Empty -> {
                if (appRatingsAdapter.itemCount < 1) {
                    appRatingsViewState.setState(State.EMPTY)
                }
            }
            is AppViewModel.RatingsList.Error -> {
                if (appRatingsAdapter.itemCount < 1) {
                    appRatingsViewState.apply {
                        setErrorDescriptionText(response.reason)
                        setState(State.ERROR)
                    }
                }
            }
        }
        isLoading = false
    })

    private fun loadRatings() {
        if (isLoading.not()) {
            isLoading = true
            if (appRatingsAdapter.itemCount < 1) {
                appRatingsViewState.setState(State.LOADING)
                hasNextPage = false
                nextPageToken = null
            }
            appViewModel.loadRatings(
                AppInput(
                    id = if (appViewModel.getIsQueryById()) currentApp?.id ?: "" else "",
                    packageName = if (appViewModel.getIsQueryById().not()) currentApp?.packageName ?: "" else "",
                    ratingInput = RatingInput(
                        sort = appViewModel.getRatingsSort(),
                        pageSize = appViewModel.getRatingsPageSize(),
                        nextPageToken = nextPageToken
                    )
                )
            )
        }
    }

    private fun setupUi() {
        isLoading = true
        appRatingsViewState.setState(State.LOADING)
        appRatingsRecycler.apply {
            adapter = appRatingsAdapter
            addOnScrollListener(object : PaginationScrollListener(
                layoutManager?.let { it as LinearLayoutManager } ?: LinearLayoutManager(this@AppRatingsFragment.requireContext())
            ) {
                override fun hasNextPage(): Boolean = this@AppRatingsFragment.hasNextPage

                override fun prefetchDistance(): Int = 10

                override fun isLoading(): Boolean = this@AppRatingsFragment.isLoading

                override fun loadMoreItems() = this@AppRatingsFragment.loadRatings()
            })
        }
        appRatingsViewState.setOnRetryClickListener {
            loadRatings()
        }
        setupApp()
    }

    private fun setupApp() {
        currentApp?.ratings?.also { ratingConnection ->
            ratingConnection.ratings?.takeIf { it.isNotEmpty() }?.also { ratings ->
                if (appRatingsAdapter.itemCount < 1) {
                    appRatingsViewState.setState(State.CONTENT)
                    appRatingsAdapter.appendItems(ratings)
                    hasNextPage = ratingConnection.hasNextPage
                    nextPageToken = ratingConnection.nextPageToken
                    isLoading = false
                }
            } ?: appRatingsViewState.setState(State.EMPTY)
        } ?: appRatingsViewState.setState(State.ERROR)
    }

    private fun ratingOnClickListener(rating: Rating) {
        findNavController().navigate(
            AppFragmentDirections.actionFromAppDetailsToCommentDetails(rating)
        )
    }
}
