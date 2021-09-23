package com.pbaltazar.blindo.ui.filter.apps

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentAppsFilterBinding
import com.pbaltazar.blindo.entities.enums.AppSort
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

    private var isFirstLaunch: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        originalSort = appsFilterViewModel.getAppSort()
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
        isFirstLaunch = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.apps_filter, menu)
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
            if (isChecked) {
                checkBox.text = "${getString(textRes)}, ${sort.indexOf(it) + 1}"
            }
        } ?: run {
            if (checkBox.isChecked.not()) {
                checkBox.text = getString(textRes)
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
        findNavController().apply {
            previousBackStackEntry?.savedStateHandle?.set(
                ARGUMENT_REQUIRE_REFRESH_FILTERS,
                originalSort.equals(sort).not()
            )
            popBackStack()        }
    }
}
