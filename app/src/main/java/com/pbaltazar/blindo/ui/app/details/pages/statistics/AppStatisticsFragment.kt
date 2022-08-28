package com.pbaltazar.blindo.ui.app.details.pages.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentAppStatisticsBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.ui.app.details.AppViewModel
import com.pbaltazar.blindo.ui.app.details.pages.AppPagerHelper
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.pbaltazar.blindo.utils.extensions.setExplainingTooltip
import com.pbaltazar.blindo.utils.extensions.setValueWithAccessibilitySupport
import com.wizeline.viewstate.State
import com.wizeline.viewstate.ViewState

class AppStatisticsFragment : BlindoFragment<FragmentAppStatisticsBinding>() {

    private lateinit var appViewModel: AppViewModel

    private lateinit var appStatisticsViewState: ViewState
    private lateinit var totalRating: TextView
    private lateinit var totalRatingBar: RatingBar
    private lateinit var uiRating: TextView
    private lateinit var uiRatingBar: RatingBar
    private lateinit var screenreadersRating: TextView
    private lateinit var screenreadersRatingBar: RatingBar
    private lateinit var labelsRating: TextView
    private lateinit var labelsRatingBar: RatingBar
    private lateinit var functionsRating: TextView
    private lateinit var functionsRatingBar: RatingBar
    private lateinit var performanceRating: TextView
    private lateinit var performanceRatingBar: RatingBar

    private var total: Float = 0F
    set(value) {
        field = value
        totalRatingBar.setValueWithAccessibilitySupport(value)
    }
    private var ui: Float = 0F
    set(value) {
        field = value
        uiRatingBar.setValueWithAccessibilitySupport(value)
    }
    private var screenreaders: Float = 0F
    set(value) {
        field = value
        screenreadersRatingBar.setValueWithAccessibilitySupport(value)
    }
    private var labels: Float = 0F
    set(value) {
        field = value
        labelsRatingBar.setValueWithAccessibilitySupport(value)
    }
    private var functions: Float = 0F
    set(value) {
        field = value
        functionsRatingBar.setValueWithAccessibilitySupport(value)
    }
    private var performance: Float = 0F
    set(value) {
        field = value
        performanceRatingBar.setValueWithAccessibilitySupport(value)
    }

    private var currentApp: App? = null
    private var isLoading: Boolean = false

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appViewModel = AppPagerHelper.appViewModelListener.getAppViewModel()!!
        currentApp = AppPagerHelper.appViewModelListener.getCurrentApp()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAppStatisticsBinding.inflate(inflater, container, false)
        appStatisticsViewState = binding!!.appStatisticsViewState
        totalRating = binding!!.ratingBars.totalRating
        totalRatingBar = binding!!.ratingBars.totalRatingBar
        uiRating = binding!!.ratingBars.uiRating
        uiRatingBar = binding!!.ratingBars.uiRatingBar
        screenreadersRating = binding!!.ratingBars.screenreadersRating
        screenreadersRatingBar = binding!!.ratingBars.screenreadersRatingBar
        labelsRating = binding!!.ratingBars.labelsRating
        labelsRatingBar = binding!!.ratingBars.labelsRatingBar
        functionsRating = binding!!.ratingBars.functionsRating
        functionsRatingBar = binding!!.ratingBars.functionsRatingBar
        performanceRating = binding!!.ratingBars.performanceRating
        performanceRatingBar = binding!!.ratingBars.performanceRatingBar
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeStatistics()
    }

    override fun onResume() {
        super.onResume()
        currentApp = AppPagerHelper.appViewModelListener.getCurrentApp()
        if ((currentApp?.numberOfRatings ?: 0) > 0 && currentApp?.uiRating == null) {
            loadAppStatistics()
        } else {
            setupApp()
        }
    }

    private fun subscribeStatistics() = appViewModel.statistics.observe(viewLifecycleOwner) {
        when (val response = it) {
            is AppViewModel.AppStatistics.Success -> {
                currentApp = response.statistics
                showAppStatistics(response.statistics)
                appStatisticsViewState.setState(State.CONTENT)
            }
            is AppViewModel.AppStatistics.Empty -> appStatisticsViewState.setState(State.EMPTY)
            is AppViewModel.AppStatistics.Error -> appStatisticsViewState.apply {
                setErrorDescriptionText(response.reason)
                appStatisticsViewState.setState(State.ERROR)
            }
        }
        isLoading = false
    }

    private fun loadAppStatistics() {
        if (isLoading.not()) {
            isLoading = true
            appStatisticsViewState.setState(State.LOADING)
            appViewModel.getAppStatistics(currentApp?.id ?: "")
        }
    }

    private fun showAppStatistics(app: App) {
        total = app.totalRating ?: 0F
        ui = app.uiRating ?: 0F
        screenreaders = app.screenreadersRating ?: 0F
        labels = app.labelsRating ?: 0F
        functions = app.functionsRating ?: 0F
        performance = app.performanceRating ?: 0F
    }

    private fun setupUi() {
        isLoading = true
        appStatisticsViewState.setState(State.LOADING)
        uiRating.setExplainingTooltip(R.string.ratingbars__ui_description)
        uiRatingBar.setExplainingTooltip(R.string.ratingbars__ui_description)
        screenreadersRating.setExplainingTooltip(R.string.ratingbars__screenreaders_description)
        screenreadersRatingBar.setExplainingTooltip(R.string.ratingbars__screenreaders_description)
        labelsRating.setExplainingTooltip(R.string.ratingbars__labels_description)
        labelsRatingBar.setExplainingTooltip(R.string.ratingbars__labels_description)
        functionsRating.setExplainingTooltip(R.string.ratingbars__functions_description)
        functionsRatingBar.setExplainingTooltip(R.string.ratingbars__functions_description)
        performanceRating.setExplainingTooltip(R.string.ratingbars__performance_description)
        performanceRatingBar.setExplainingTooltip(R.string.ratingbars__performance_description)
        totalRating.setExplainingTooltip(R.string.ratingbars__total_description)
        totalRatingBar.setExplainingTooltip(R.string.ratingbars__total_description)
        appStatisticsViewState.setOnRetryClickListener {
            loadAppStatistics()
        }
        setupApp()
    }

    private fun setupApp() {
        currentApp?.also { app ->
            showAppStatistics(app)
            appStatisticsViewState.setState(State.CONTENT)
            isLoading = false
        } ?: appStatisticsViewState.setState(State.ERROR)
    }
}
