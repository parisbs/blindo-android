package com.pbaltazar.blindo.ui.filter.apps

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentAppsFilterBinding
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.sorts.AppSort
import com.pbaltazar.blindo.ui.components.selectors.AccessibleRangeSelector
import com.pbaltazar.blindo.utils.constants.ARGUMENT_REQUIRE_REFRESH_FILTERS
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppsFilterFragment : Fragment() {

    private val appsFilterViewModel: AppsFilterViewModel by viewModel()
    private var binding: FragmentAppsFilterBinding? = null

    private lateinit var orderByHeader: TextView
    private lateinit var updatedAtCheckBox: CheckBox
    private lateinit var updatedAtSpinner: Spinner
    private lateinit var packageLabelCheckbox: CheckBox
    private lateinit var packageLabelSpinner: Spinner
    private lateinit var totalRatingCheckBox: CheckBox
    private lateinit var totalRatingSpinner: Spinner
    private lateinit var availablePacksCheckBox: CheckBox
    private lateinit var availablePacksSpinner: Spinner
    private lateinit var totalRatingRangeControl: TextView
    private lateinit var totalRatingRangeSelectorContainer: ConstraintLayout
    private lateinit var totalRatingRangeSelector: AccessibleRangeSelector

    private val spinnersAndCheckboxesMap: MutableMap<Int, CheckBox> = mutableMapOf()

    private val spinnersSelectionListener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (isFirstLaunch.not()) {
                if (spinnersAndCheckboxesMap[parent?.id ?: View.NO_ID]?.isChecked ?: false) {
                    processSpinnerValue(AppSort.valueFromSpinnerAndPosition(parent?.id ?: View.NO_ID, position))
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // Not required
        }
    }

    private var originalSort: List<AppSort> = listOf()
    private val sort: MutableList<AppSort> = mutableListOf()

    private val totalRatingExpandAction: AccessibilityNodeInfoCompat.AccessibilityActionCompat = AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND
    private val totalRatingCollapseAction: AccessibilityNodeInfoCompat.AccessibilityActionCompat = AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE
    private var originalIsTotalRatingRangeExpanded: Boolean = false
    private var isTotalRatingRangeExpanded: Boolean = false
    private var originalTotalRatingRange: FloatRange = FloatRange(1F, 5F)
    private val totalRatingRange: FloatRange
        get() =
        totalRatingRangeSelector.getRange()

    private var isFirstLaunch: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        originalSort = appsFilterViewModel.getAppSort()
        originalIsTotalRatingRangeExpanded = appsFilterViewModel.getIsAppTotalRatingRangeChecked()
        originalTotalRatingRange = appsFilterViewModel.getAppTotalRatingRange()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAppsFilterBinding.inflate(inflater, container, false)
        orderByHeader = binding!!.orderByHeader
        ViewCompat.setAccessibilityHeading(orderByHeader, true)
        updatedAtCheckBox = binding!!.commonOrderBy.lastUpdatedCheckbox
        updatedAtSpinner = binding!!.commonOrderBy.lastUpdatedSpinner
        packageLabelCheckbox = binding!!.packageLabelCheckbox
        packageLabelSpinner = binding!!.packageLabelSpinner
        totalRatingCheckBox = binding!!.totalRatingCheckbox
        totalRatingSpinner = binding!!.totalRatingSpinner
        availablePacksCheckBox = binding!!.availablePacksCheckbox
        availablePacksSpinner = binding!!.availablePacksSpinner
        totalRatingRangeControl = binding!!.totalRatingRangeHeader
        ViewCompat.setAccessibilityHeading(totalRatingRangeControl, true)
        totalRatingRangeSelectorContainer = binding!!.totalRatingRangeExpandableContainer
        totalRatingRangeSelector = binding!!.totalRatingRangeSelector
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    override fun onResume() {
        super.onResume()
        sort.clear()
        sort.addAll(originalSort)
        refreshOrderByStates(true)
        isTotalRatingRangeExpanded = originalIsTotalRatingRangeExpanded.not()
        totalRatingRangeSelector.setRange(originalTotalRatingRange)
        totalRatingRangeExpandableController()
        isFirstLaunch = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filters, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.applyFilters -> {
                saveFilters()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun setupUi() {
        setupCheckboxes()
        setupSpinners()
        setupTotalRatingRange()
    }

    private fun refreshOrderByStates(isInitialState: Boolean = false) {
        AppSort.valuesOf("UPDATED_AT_").forEach {
            refreshOrderByState(updatedAtCheckBox, updatedAtSpinner, it, R.string.filter__updated_at, isInitialState)
        }
        AppSort.valuesOf("PACKAGE_LABEL_").forEach {
            refreshOrderByState(packageLabelCheckbox, packageLabelSpinner, it, R.string.apps_filter__package_label, isInitialState)
        }
        AppSort.valuesOf("TOTAL_RATING_").forEach {
            refreshOrderByState(totalRatingCheckBox, totalRatingSpinner, it, R.string.apps_filter__total_rating, isInitialState)
        }
        AppSort.valuesOf("AVAILABLE_PACKS_").forEach {
            refreshOrderByState(availablePacksCheckBox, availablePacksSpinner, it, R.string.apps_filter__available_packs, isInitialState)
        }
    }

    private fun refreshOrderByState(
        checkBox: CheckBox,
        spinner: Spinner,
        appSort: AppSort,
        textRes: Int,
        isInitialState: Boolean = false
    ) {
        if (sort.contains(appSort)) {
            setOrderByValues(textRes, checkBox, true, spinner, appSort, isInitialState)
        } else {
            setOrderByValues(textRes, checkBox, false, spinner, null, isInitialState)
        }
    }

    private fun setOrderByValues(
        textRes: Int,
        checkBox: CheckBox,
        isChecked: Boolean,
        spinner: Spinner,
        appSort: AppSort? = null,
        isInitialState: Boolean = false
    ) {
        val notCheckedText = getString(textRes)
        if (isInitialState) {
            checkBox.isChecked = isChecked
            spinner.isEnabled = isChecked
            if (isChecked) {
                appSort?.also {
                    spinner.setSelection(it.spinnerIndex, true)
                }
            }
        }
        appSort?.also {
            val checkedText = "${getString(textRes)}, ${sort.indexOf(it) + 1}"
            if (
                isChecked &&
                    TextUtils.equals(checkedText, checkBox.text).not()
            ) {
                checkBox.text = checkedText
            }
        } ?: run {
            if (
                checkBox.isChecked.not() &&
                    TextUtils.equals(notCheckedText, checkBox.text).not()
            ) {
                checkBox.text = notCheckedText
            }
        }
    }

    private fun setupCheckboxes() {
        updatedAtCheckBox.setOnCheckedChangeListener { _, isChecked ->
            setCheckboxState(isChecked, updatedAtSpinner)
        }
        packageLabelCheckbox.setOnCheckedChangeListener { _, isChecked ->
            setCheckboxState(isChecked, packageLabelSpinner)
        }
        totalRatingCheckBox.setOnCheckedChangeListener { _, isChecked ->
            setCheckboxState(isChecked, totalRatingSpinner)
        }
        availablePacksCheckBox.setOnCheckedChangeListener { _, isChecked ->
            setCheckboxState(isChecked, availablePacksSpinner)
        }
        ViewCompat.setAccessibilityLiveRegion(updatedAtCheckBox, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE)
        ViewCompat.setAccessibilityLiveRegion(packageLabelCheckbox, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE)
        ViewCompat.setAccessibilityLiveRegion(totalRatingCheckBox, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE)
        ViewCompat.setAccessibilityLiveRegion(availablePacksCheckBox, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE)
    }

    private fun setCheckboxState(isChecked: Boolean, spinner: Spinner) {
        spinner.isEnabled = isChecked
        if (isChecked.not()) {
            clearSpinnerValuesFromSortList(spinner)
        } else {
            spinner.setSelection(0, true)
            processSpinnerValue(AppSort.valueFromSpinnerAndPosition(spinner.id, 0))
        }
    }

    private fun setupSpinners() {
        updatedAtSpinner.onItemSelectedListener = spinnersSelectionListener
        packageLabelSpinner.onItemSelectedListener = spinnersSelectionListener
        totalRatingSpinner.onItemSelectedListener = spinnersSelectionListener
        availablePacksSpinner.onItemSelectedListener = spinnersSelectionListener
        spinnersAndCheckboxesMap.putAll(
            mapOf(
                Pair(updatedAtSpinner.id, updatedAtCheckBox),
                Pair(packageLabelSpinner.id, packageLabelCheckbox),
                Pair(totalRatingSpinner.id, totalRatingCheckBox),
                Pair(availablePacksSpinner.id, availablePacksCheckBox)
            )
        )
    }

    private fun setupTotalRatingRange() {
        ViewCompat.setAccessibilityDelegate(totalRatingRangeControl, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.apply {
                    isCheckable = true
                    if (isTotalRatingRangeExpanded) {
                        removeAction(totalRatingExpandAction)
                        addAction(totalRatingCollapseAction)
                        isChecked = true
                    } else {
                        removeAction(totalRatingCollapseAction)
                        addAction(totalRatingExpandAction)
                        isChecked = false
                    }
                }
            }

            override fun performAccessibilityAction(host: View, action: Int, args: Bundle?): Boolean {
                when (action) {
                    totalRatingExpandAction.id, totalRatingCollapseAction.id -> {
                        totalRatingRangeExpandableController()
                        return true
                    }
                    else -> {
                        return super.performAccessibilityAction(host, action, args)
                    }
                }
            }
        })
        totalRatingRangeControl.setOnClickListener {
            totalRatingRangeExpandableController()
        }
    }

    private fun totalRatingRangeExpandableController() {
        if (isTotalRatingRangeExpanded) {
            totalRatingRangeControl.setCompoundDrawablesRelative(resources.getDrawable(R.drawable.ic_check_box_outline_blank_black_24dp), null, null, null)
            totalRatingRangeSelectorContainer.visibility = View.GONE
        } else {
            totalRatingRangeControl.setCompoundDrawablesRelative(resources.getDrawable(R.drawable.ic_check_box_black_24dp), null, null, null)
            totalRatingRangeSelectorContainer.visibility = View.VISIBLE
        }
        isTotalRatingRangeExpanded = isTotalRatingRangeExpanded.not()
    }

    private fun processSpinnerValue(appSort: AppSort) {
        val invertedAppSort = appSort.invert()
        var currentPosicion = sort.size
        if (sort.contains(invertedAppSort)) {
            currentPosicion = sort.indexOf(invertedAppSort)
            sort.remove(invertedAppSort)
        }
        if (sort.contains(appSort).not()) {
            sort.add(currentPosicion, appSort)
            refreshOrderByStates()
        }
    }

    private fun clearSpinnerValuesFromSortList(spinner: Spinner) {
        sort.removeAll(AppSort.valuesFromSpinner(spinner.id))
        refreshOrderByStates()
    }

    private fun saveFilters() {
        appsFilterViewModel.setAppSort(sort)
        appsFilterViewModel.setIsAppTotalRatingRangeChecked(isTotalRatingRangeExpanded)
        if (isTotalRatingRangeExpanded) {
            appsFilterViewModel.setAppTotalRatingRange(totalRatingRange)
        }
        findNavController().apply {
            previousBackStackEntry?.savedStateHandle?.set(
                ARGUMENT_REQUIRE_REFRESH_FILTERS,
                originalSort.equals(sort).not() ||
                    originalIsTotalRatingRangeExpanded.equals(isTotalRatingRangeExpanded).not() ||
                    if (isTotalRatingRangeExpanded) originalTotalRatingRange.equals(totalRatingRange).not() else false
            )
            popBackStack()        }
    }
}
