package com.pbaltazar.blindo.ui.filter

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.components.filters.FiltersScreen
import com.pbaltazar.blindo.components.filters.entities.orderby.OrderByEnum
import com.pbaltazar.blindo.components.filters.entities.orderby.OrderBySelection
import com.pbaltazar.blindo.databinding.FragmentFiltersBinding
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.utils.constants.ARGUMENT_REQUIRE_REFRESH_FILTERS
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.pbaltazar.blindo.utils.preferences.OnUserPreferencesChangeListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class FiltersFragment : BlindoFragment<FragmentFiltersBinding>(),
    OnUserPreferencesChangeListener {

    private val filtersViewModel : FiltersViewModel by viewModel()
    private val filtersFragmentArgs: FiltersFragmentArgs by navArgs()

    private lateinit var filtersScreen: FiltersScreen

    private lateinit var filtersSet: FiltersSet

    private val keysToListen: MutableList<String> = mutableListOf()
    private var requiresRefresh: Boolean = false

    override val isSearchable: Boolean
        get() = false

    override fun onUserPreferencesChange(key: String) {
        if (keysToListen.contains(key)) {
            requiresRefresh = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filtersSet = filtersFragmentArgs.filtersSet
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            subtitle = this@FiltersFragment.getString(
                R.string.filters__subtitle,
                title
            )
            title = filtersFragmentArgs.filtersSet.getTitle(requireContext())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFiltersBinding.inflate(inflater, container, false)
        filtersScreen = binding!!.appsPacksFilters
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filtersScreen.showOrderBySection = filtersSet.showOrderBySection
        filtersScreen.autoAddCommonElements = filtersSet.autoAddCommonElements
        filtersScreen.addElementsFromArray(filtersSet.arrayResId)
        setSavedValues()
        filtersViewModel.registerOnUserPreferencesChangeListener(this as OnUserPreferencesChangeListener)
    }

    override fun onDestroy() {
        filtersViewModel.unregisterOnUserPreferencesChangeListener(this as OnUserPreferencesChangeListener)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filters, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.applyFilters -> {
                saveFilters()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setSavedValues() {
        setSavedOrderByValues()
        setSavedRangesValues()
        setSavedCheckboxesValues()
    }

    private fun setSavedOrderByValues() {
        if (filtersScreen.showOrderBySection) {
            val orderBySelections: MutableList<OrderBySelection> = mutableListOf()
            val key: String = filtersSet.getPreferencesKeyForTypeAndId(requireContext(), FiltersScreen.Companion.FilterType.ORDER_BY_TYPE, filtersScreen.id)
            filtersViewModel.getString(
                key,
                filtersSet.getOrderByDefault()
            ).split(",").forEach { item ->
                (filtersSet.orderByEnum.java.enumConstants.filter { it.name.equals(item) }.takeIf { it.size == 1 }?.first() as? OrderByEnum)?.also { orderByEnum ->
                    filtersScreen.getOrderByElements().forEach { orderByElementFilter ->
                        if (orderByEnum.associatedIds().contains(orderByElementFilter.id)) {
                            orderBySelections.add(
                                OrderBySelection(
                                    orderByElementFilter.id,
                                    true,
                                    orderByEnum.getDirection()
                                )
                            )
                        }
                    }
                }
            }
            filtersScreen.setOrderBySelections(orderBySelections.toList())
            keysToListen.add(key)
        }
    }

    private fun setSavedRangesValues() {
        filtersScreen.getRangeElements().takeIf { it.size > 0 }?.forEach { rangeFilter ->
            val key: String = filtersSet.getPreferencesKeyForTypeAndId(requireContext(), FiltersScreen.Companion.FilterType.RANGE_TYPE, rangeFilter.id)
            val isExpanded = filtersViewModel.getBoolean(key, filtersSet.isRangeCheckedDefault(requireContext(), rangeFilter.id))
            val range: FloatRange = filtersViewModel.getFloatRange(
                key,
                filtersSet.getFloatRangeDefault(requireContext(), rangeFilter.id)
            )
            rangeFilter.isExpanded = isExpanded
            rangeFilter.setFloatRange(range)
            keysToListen.add(key)
        }
    }

    private fun setSavedCheckboxesValues() {
        filtersScreen.getCheckboxElements().takeIf { it.size > 0 }?.forEach { checkboxFilter ->
            val key = filtersSet.getPreferencesKeyForTypeAndId(requireContext(), FiltersScreen.Companion.FilterType.CHECKBOX_TYPE, checkboxFilter.id)
            val isChecked: Boolean = filtersViewModel.getBoolean(
                key,
                filtersSet.getCheckboxDefault(requireContext(), checkboxFilter.id)
            )
            checkboxFilter.isChecked = isChecked
            keysToListen.add(key)
        }
    }

    private fun saveFilters() {
        saveOrderBy()
        saveRanges()
        saveCheckboxes()
        findNavController().apply {
            previousBackStackEntry?.savedStateHandle?.set(
                ARGUMENT_REQUIRE_REFRESH_FILTERS,
                requiresRefresh
            )
            popBackStack()
        }
    }

    private fun saveOrderBy() {
        val key: String = filtersSet.getPreferencesKeyForTypeAndId(requireContext(), FiltersScreen.Companion.FilterType.ORDER_BY_TYPE, filtersScreen.id)
        filtersScreen.getOrderBySelections().mapNotNull { orderBySelection ->
            filtersSet.orderByEnum.java.enumConstants.mapNotNull { it as OrderByEnum }.filter {
                it.associatedIds().contains(orderBySelection.elementId) &&
                    it.getDirection().equals(orderBySelection.direction)
            }.takeIf { it.size == 1 }
                ?.first()?.getName()
        }.joinToString(",").also { orderByValue ->
            filtersViewModel.setString(key, orderByValue)
        }
    }

    private fun saveRanges() {
        filtersScreen.getRangeElements().forEach { rangeFilter ->
            val key: String = filtersSet.getPreferencesKeyForTypeAndId(requireContext(), FiltersScreen.Companion.FilterType.RANGE_TYPE, rangeFilter.id)
            rangeFilter.isExpanded.also { isExpanded ->
                filtersViewModel.setBoolean(key, isExpanded)
                if (isExpanded) {
                    rangeFilter.getFloatRange().also { rangeValue ->
                        filtersViewModel.setFloatRange(key, rangeValue)
                    }
                }
            }
        }
    }

    private fun saveCheckboxes() {
        filtersScreen.getCheckboxElements().forEach { checkboxFilter ->
            val key: String = filtersSet.getPreferencesKeyForTypeAndId(requireContext(), FiltersScreen.Companion.FilterType.CHECKBOX_TYPE, checkboxFilter.id)
            checkboxFilter.isChecked.also { checkboxValue ->
                filtersViewModel.setBoolean(key, checkboxValue)
            }
        }
    }
}
