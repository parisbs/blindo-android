package com.pbaltazar.blindo.ui.app.details

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentAppBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.ui.app.details.pages.AppViewModelListener
import com.pbaltazar.blindo.ui.app.details.pages.AppPagerHelper
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.pbaltazar.blindo.utils.extensions.isNullOrEmptyOrBlank
import com.wizeline.viewstate.State
import com.wizeline.viewstate.ViewState
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppFragment : BlindoFragment<FragmentAppBinding>() {

    private val appViewModel: AppViewModel by viewModel()
    private val appFragmentArgs: AppFragmentArgs by navArgs()

    private lateinit var appViewState: ViewState
    private lateinit var appViewPager: ViewPager
    private lateinit var appTabs: TabLayout
    private lateinit var pagerAdapter: AppPagerAdapter

    private var currentApp: App? = null
    private var isLoadingApp: Boolean = false

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        currentApp = appFragmentArgs.app
        AppPagerHelper.appViewModelListener = object : AppViewModelListener {
            override fun getCurrentApp(): App? = currentApp

            override fun getAppViewModel(): AppViewModel = appViewModel
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = appFragmentArgs.app.packageLabel
            subtitle = if (appFragmentArgs.app.category.isNullOrEmptyOrBlank().not())
                appFragmentArgs.app.category
            else
                appFragmentArgs.app.packageName
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAppBinding.inflate(inflater, container, false)
        appViewState = binding!!.appViewState
        appViewPager = binding!!.appViewPager
        appTabs = binding!!.appTabs
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeAppDetails()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navRatingCreator -> {
                findNavController().navigate(
                    AppFragmentDirections.actionFromAppDetailsToCommentCreator(currentApp!!)
                )
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isLoadingApp.not()) {
            loadApp()
        }
    }

    private fun loadApp() {
        currentApp?.also { app ->
            isLoadingApp = true
            appViewState.setState(State.LOADING)
            if (app.id.isNotEmpty()) {
                appViewModel.setIsQueryById(true)
                appViewModel.getApp(id = app.id)
            } else {
                appViewModel.setIsQueryById(false)
                appViewModel.getApp(packageName = app.packageName)
            }
        } ?: findNavController().popBackStack()
    }

    private fun subscribeAppDetails() = appViewModel.appDetails.observe(this, Observer {
        when (val response = it) {
            is AppViewModel.AppDetails.Success -> {
                currentApp = currentApp?.let { app ->
                    if (appViewModel.getIsQueryById()) {
                        response.app.copy(
                            id = app.id,
                            packageName = app.packageName,
                            packageIcon = app.packageIcon,
                            packageLabel = app.packageLabel,
                            category = app.category,
                            numberOfRatings = app.numberOfRatings,
                            availablePacks = app.availablePacks
                        )
                    } else {
                        response.app.copy(
                            packageName = app.packageName
                        )
                    }
                } ?: response.app
                appViewState.setState(State.CONTENT)
                setupViewPager()
            }
            is AppViewModel.AppDetails.Empty -> appViewState.setState(State.EMPTY)
            is AppViewModel.AppDetails.Error -> {
                appViewState.setErrorDescriptionText(response.errorMessage)
                appViewState.setState(State.ERROR)
            }
        }
        isLoadingApp = false
    })

    private fun setupViewPager() {
        pagerAdapter = AppPagerAdapter(requireContext(), childFragmentManager)
        appTabs.setupWithViewPager(
            appViewPager.apply {
                adapter = pagerAdapter
            }
        )
        appTabs.apply {
            getTabAt(1)?.text = getString(
                R.string.appdetails__packs_tab,
                currentApp?.availablePacks ?: 0
            )
            getTabAt(2)?.text = getString(
                R.string.appdetails__comments_tab,
                currentApp?.numberOfRatings ?: 0
            )
        }
    }

    private fun setupUi() {
        appViewState.apply {
            setOnRetryClickListener {
                loadApp()
            }
        }
    }
}
