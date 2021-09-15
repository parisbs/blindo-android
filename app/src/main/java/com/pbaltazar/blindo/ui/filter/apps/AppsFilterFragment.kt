package com.pbaltazar.blindo.ui.filter.apps

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentFilterBinding
import com.pbaltazar.blindo.entities.enums.AppSort
import com.pbaltazar.blindo.utils.constants.APP_SORT
import com.pbaltazar.blindo.utils.extensions.isNullOrEmptyOrBlank
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppsFilterFragment : Fragment() {

    private val appsFilterViewModel: AppsFilterViewModel by viewModel()
    private var binding: FragmentFilterBinding? = null

    private lateinit var orderByHeader: TextView
    private lateinit var packageLabelCheckbox: CheckBox
    private lateinit var packageLabel: TextView
    private lateinit var packageLabelSpinner: Spinner
    private lateinit var packageLabelPriority: EditText
    private lateinit var totalRatingCheckbox: CheckBox
    private lateinit var totalRating: TextView
    private lateinit var totalRatingSpinner: Spinner
    private lateinit var totalRatingPriority: EditText
    private lateinit var availablePacksCheckbox: CheckBox
    private lateinit var availablePacks: TextView
    private lateinit var availablePacksSpinner: Spinner
    private lateinit var availablePacksPriority: EditText
    private lateinit var saveFilter: ImageButton

    private lateinit var sort: List<AppSort>

    private var packageLabelSpinnerSelection: Int = -1
    private var totalRatingSpinnerSelection: Int = -1
    private var availablePacksSpinnerSelection: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        orderByHeader = binding!!.orderByHeader
        ViewCompat.setAccessibilityHeading(orderByHeader, true)
        packageLabelCheckbox = binding!!.orderByContent.packageLabelCheckbox
        packageLabel = binding!!.orderByContent.packageLabel
        packageLabelSpinner = binding!!.orderByContent.packageLabelSpinner
        packageLabelPriority = binding!!.orderByContent.packageLabelPriority
        totalRatingCheckbox = binding!!.orderByContent.totalRatingCheckbox
        totalRating = binding!!.orderByContent.totalRating
        totalRatingSpinner = binding!!.orderByContent.totalRatingSpinner
        totalRatingPriority = binding!!.orderByContent.totalRatingPriority
        availablePacksCheckbox = binding!!.orderByContent.availablePacksCheckbox
        availablePacks = binding!!.orderByContent.availablePacks
        availablePacksSpinner = binding!!.orderByContent.availablePacksSpinner
        availablePacksPriority = binding!!.orderByContent.availablePacksPriority
        saveFilter = binding!!.saveFilter
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

    private fun setupUi() {
        sort = appsFilterViewModel.getAppSort()
        if (Build.VERSION.SDK_INT >= 22) {
            setAccessibilityNavigationOrder()
        }
        setupPackageLabelState()
        setupTotalRatingState()
        setupAvailablePacksState()
        setupCheckboxes()
        setupSpinners()
        setupSaveButton()
    }

    private fun setAccessibilityNavigationOrder() {
        packageLabel.accessibilityTraversalAfter = R.id.packageLabelCheckbox
        packageLabelSpinner.accessibilityTraversalAfter = R.id.packageLabel
        packageLabelPriority.accessibilityTraversalAfter = R.id.packageLabelSpinner
        totalRating.accessibilityTraversalAfter = R.id.totalRatingCheckbox
        totalRatingSpinner.accessibilityTraversalAfter = R.id.totalRating
        totalRatingPriority.accessibilityTraversalAfter = R.id.totalRatingSpinner
        availablePacks.accessibilityTraversalAfter = R.id.availablePacksCheckbox
        availablePacksSpinner.accessibilityTraversalAfter = R.id.availablePacks
        availablePacksPriority.accessibilityTraversalAfter = R.id.availablePacksSpinner
    }

    private fun setupPackageLabelState() {
        when {
            true == sort.contains(AppSort.PACKAGE_LABEL_ASC) -> {
                packageLabelCheckbox.isChecked = true
                packageLabelSpinnerSelection = 0
//                packageLabelSpinner.setSelection(packageLabelSpinnerSelection)
                packageLabelPriority.text = Editable.Factory.getInstance().newEditable(
                    sort.indexOf(AppSort.PACKAGE_LABEL_ASC).toString()
                )
            }
            true == sort.contains(AppSort.PACKAGE_LABEL_DESC) -> {
                packageLabelCheckbox.isChecked = true
                packageLabelSpinnerSelection = 1
                packageLabelSpinner.setSelection(packageLabelSpinnerSelection)
                packageLabelPriority.text = Editable.Factory.getInstance().newEditable(
                    sort.indexOf(AppSort.PACKAGE_LABEL_DESC).toString()
                )
            }
            else -> {
                packageLabelCheckbox.isChecked = false
                packageLabelSpinner.isEnabled = false
                packageLabelPriority.isEnabled = false
            }
        }
    }

    private fun setupTotalRatingState() {
        when {
            true == sort.contains(AppSort.TOTAL_RATING_ASC) -> {
                totalRatingCheckbox.isChecked = true
                totalRatingSpinnerSelection = 0
                totalRatingSpinner.setSelection(totalRatingSpinnerSelection)
                totalRatingPriority.text = Editable.Factory.getInstance().newEditable(
                    sort.indexOf(AppSort.TOTAL_RATING_ASC).toString()
                )
            }
            true == sort.contains(AppSort.TOTAL_RATING_DESC) -> {
                totalRatingCheckbox.isChecked = true
                totalRatingSpinnerSelection = 1
                totalRatingSpinner.setSelection(totalRatingSpinnerSelection)
                totalRatingPriority.text = Editable.Factory.getInstance().newEditable(
                    sort.indexOf(AppSort.TOTAL_RATING_DESC).toString()
                )
            }
            else -> {
                totalRatingCheckbox.isChecked = false
                totalRatingSpinner.isEnabled = false
                totalRatingPriority.isEnabled = false
            }
        }
    }

    private fun setupAvailablePacksState() {
        when {
            true == sort.contains(AppSort.AVAILABLEPACKS_ASC) -> {
                availablePacksCheckbox.isChecked = true
                availablePacksSpinnerSelection = 0
                availablePacksSpinner.setSelection(availablePacksSpinnerSelection)
                availablePacksPriority.text = Editable.Factory.getInstance().newEditable(
                    sort.indexOf(AppSort.AVAILABLEPACKS_ASC).toString()
                )
            }
            true == sort.contains(AppSort.AVAILABLE_PACKS_DESC) -> {
                availablePacksCheckbox.isChecked = true
                availablePacksSpinnerSelection = 1
                availablePacksSpinner.setSelection(availablePacksSpinnerSelection)
                availablePacksPriority.text = Editable.Factory.getInstance().newEditable(
                    sort.indexOf(AppSort.AVAILABLE_PACKS_DESC).toString()
                )
            }
            else -> {
                availablePacksCheckbox.isChecked = false
                availablePacksSpinner.isEnabled = false
                availablePacksPriority.isEnabled = false
            }
        }
    }

    private fun setupCheckboxes() {
        packageLabelCheckbox.setOnCheckedChangeListener { _, isChecked ->
            packageLabelSpinner.isEnabled = isChecked
            packageLabelPriority.isEnabled = isChecked
        }
        totalRatingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            totalRatingSpinner.isEnabled = isChecked
            totalRatingPriority.isEnabled = isChecked
        }
        availablePacksCheckbox.setOnCheckedChangeListener { _, isChecked ->
            availablePacksSpinner.isEnabled = isChecked
            availablePacksPriority.isEnabled = isChecked
        }
    }

    private fun setupSpinners() {
        packageLabelSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                packageLabelSpinnerSelection = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                packageLabelSpinnerSelection = -1
            }
        }
        totalRatingSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                totalRatingSpinnerSelection = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                totalRatingSpinnerSelection = -1
            }
        }
        availablePacksSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                availablePacksSpinnerSelection = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                availablePacksSpinnerSelection = -1
            }
        }
    }

    private fun setupSaveButton() {
        saveFilter.setOnClickListener {
            saveFilters()
        }
    }

    private fun saveFilters() {
        val valuesInOrder = HashMap<Int, AppSort>()
        val filters = mutableListOf<AppSort?>()
        if (packageLabelCheckbox.isChecked) {
            val index = packageLabelPriority.text.toString()
            when (packageLabelSpinnerSelection) {
                0 -> {
                    if (index.isNullOrEmptyOrBlank()) {
                        valuesInOrder[9991] = AppSort.PACKAGE_LABEL_ASC
                     }else {
                        valuesInOrder[index.toInt()] = AppSort.PACKAGE_LABEL_ASC
                    }
                }
                1 -> {
                    if (index.isNullOrEmptyOrBlank()) {
                        valuesInOrder[9991] = AppSort.PACKAGE_LABEL_DESC
                    } else {
                        valuesInOrder[index.toInt()] = AppSort.PACKAGE_LABEL_DESC
                    }
                }
            }
        }
        if (totalRatingCheckbox.isChecked) {
            val index = totalRatingPriority.text.toString()
            when (totalRatingSpinnerSelection) {
                0 -> {
                    if (index.isNullOrEmptyOrBlank()) {
                        valuesInOrder[9992] = AppSort.TOTAL_RATING_ASC
                    } else {
                        valuesInOrder[index.toInt()] = AppSort.TOTAL_RATING_ASC
                    }
                }
                1 -> {
                    if (index.isNullOrEmptyOrBlank()) {
                        valuesInOrder[9992] = AppSort.TOTAL_RATING_DESC
                    } else {
                        valuesInOrder[index.toInt()] = AppSort.TOTAL_RATING_DESC
                    }
                }
            }
        }
        if (availablePacksCheckbox.isChecked) {
            val index = availablePacksPriority.text.toString()
            when (availablePacksSpinnerSelection) {
                0 -> {
                    if (index.isNullOrEmptyOrBlank()) {
                        valuesInOrder[9993] = AppSort.AVAILABLEPACKS_ASC
                    } else {
                        valuesInOrder[index.toInt()] = AppSort.AVAILABLEPACKS_ASC
                    }
                }
                1 -> {
                    if (index.isNullOrEmptyOrBlank()) {
                        valuesInOrder[9993] = AppSort.AVAILABLE_PACKS_DESC
                    } else {
                        valuesInOrder[index.toInt()] = AppSort.AVAILABLE_PACKS_DESC
                    }
                }
            }
        }
        valuesInOrder.keys.sorted().forEach { i ->
            filters.add(valuesInOrder.get(i))
        }
        appsFilterViewModel.setAppSort(
            filters.toList().mapNotNull { it }
        )
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            APP_SORT,
            appsFilterViewModel.getAppSort()
        )
        findNavController().popBackStack()
    }
}
