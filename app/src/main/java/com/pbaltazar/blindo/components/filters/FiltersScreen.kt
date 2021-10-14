package com.pbaltazar.blindo.components.filters

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getResourceIdOrThrow
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ComponentFiltersScreenBinding
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange
import com.pbaltazar.blindo.components.filters.elements.CheckboxFilter
import com.pbaltazar.blindo.components.filters.elements.OrderByElementFilter
import com.pbaltazar.blindo.components.filters.elements.RangeFilter
import com.pbaltazar.blindo.components.filters.entities.orderby.OrderBySelection
import com.pbaltazar.blindo.components.filters.extensions.toFiltersCheckbox
import com.pbaltazar.blindo.components.filters.extensions.toFiltersOrderByElement
import com.pbaltazar.blindo.components.filters.extensions.toFiltersRange

class FiltersScreen @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        enum class FilterType(
            @IdRes
            val id: Int
            ) {
            ORDER_BY_TYPE(OrderByElementFilter.FILTER_TYPE),
            RANGE_TYPE(RangeFilter.FILTER_TYPE),
            CHECKBOX_TYPE(CheckboxFilter.FILTER_TYPE);

            companion object {
                fun fromId(@IdRes id: Int): FilterType =
                    values().filter { it.id == id }.takeIf { it.size == 1 }
                        ?.first() ?: throw IllegalArgumentException("Invalid filter type ID.")
            }
        }
    }

    private val binding: ComponentFiltersScreenBinding

    private val filtersOrderBySection: FiltersOrderBySection
    private val filtersSection: LinearLayout

    var showOrderBySection: Boolean = false
    set(value) {
        field = value
        filtersOrderBySection.visibility = if (field) View.VISIBLE else View.GONE
    }
    var autoAddCommonElements: Boolean = false
    set(value) {
        field = value
        filtersOrderBySection.autoAddCommonElements = field
    }

    private val rangeFilterElements: MutableList<RangeFilter> = mutableListOf()
    private val checkboxFilterElements: MutableList<CheckboxFilter> = mutableListOf()

    init {
        binding = ComponentFiltersScreenBinding.inflate(LayoutInflater.from(context), this)

        filtersOrderBySection = binding.orderBySection
        filtersSection = binding.filtersSection

        var elementsReference: Int = View.NO_ID

        attrs?.also {
            val a = context.obtainStyledAttributes(it, R.styleable.FiltersScreen)

            showOrderBySection = a.getBoolean(R.styleable.FiltersScreen_showOrderBySection, false)
            autoAddCommonElements = a.getBoolean(R.styleable.FiltersScreen_autoAddOrderByCommonElements, false)
            elementsReference = a.getResourceId(R.styleable.FiltersScreen_filters, View.NO_ID)

            a.recycle()
        }

        if (elementsReference != View.NO_ID) {
            processElementsTypedArray(getFiltersArray(elementsReference))
        }
    }

    fun getOrderByElements(): List<OrderByElementFilter> =
        filtersOrderBySection.getElements()

    fun getRangeElements(): List<RangeFilter> =
        rangeFilterElements.toList()

    fun getCheckboxElements(): List<CheckboxFilter> =
        checkboxFilterElements.toList()

    fun getOrderBySelection(@IdRes elementId: Int): OrderBySelection =
        filtersOrderBySection.getSelection(elementId)

    fun setOrderBySelection(selection: OrderBySelection) =
        filtersOrderBySection.setSelection(selection)

    fun getOrderBySelections(): List<OrderBySelection> =
        filtersOrderBySection.getSelections()

    fun setOrderBySelections(selections: List<OrderBySelection>) =
        filtersOrderBySection.setSelections(selections)

    fun getFloatRange(@IdRes rangeId: Int): FloatRange =
        rangeFilterElements.filter { it.id == rangeId }.takeIf { it.size == 1 }?.first()?.let {
            it.getFloatRange()
        } ?: throw IllegalArgumentException("Invalid range element ID.")

    fun setFloatRange(@IdRes rangeId: Int, range: FloatRange) =
        rangeFilterElements.filter { it.id == rangeId }.takeIf {
            it.size == 1
        }?.first()?.setFloatRange(range) ?: throw IllegalArgumentException("Invalid range element ID.")

    fun getIntRange(@IdRes rangeId: Int): IntRange =
        rangeFilterElements.filter { it.id == rangeId }.takeIf { it.size == 1 }?.first()?.let {
            it.getIntRange()
        } ?: throw IllegalArgumentException("Invalid range element ID.")

    fun setIntRange(@IdRes rangeId: Int, range: IntRange) =
        rangeFilterElements.filter { it.id == rangeId }.takeIf {
            it.size == 1
        }?.first()?.setIntRange(range) ?: throw IllegalArgumentException("Invalid range element ID.")

    fun getCheckboxIsChecked(@IdRes elementId: Int): Boolean =
        checkboxFilterElements.filter { it.id == elementId }.takeIf { it.size == 1 }
            ?.first()?.isChecked ?: throw IllegalArgumentException("Invalid checkbox element ID.")

    fun setCheckboxIsChecked(@IdRes elementId: Int, isChecked: Boolean) =
        checkboxFilterElements.filter { it.id == elementId }.takeIf { it.size == 1 }?.first()?.apply {
            this.isChecked = isChecked
        } ?: throw IllegalArgumentException("Invalid checkbox element ID.")

    fun addOrderByElementFromTypedArray(array: TypedArray) {
        addOrderByElement(array.toFiltersOrderByElement(context))
    }

    fun addOrderByElement(elementFilter: OrderByElementFilter) {
        if (showOrderBySection.not()) {
            throw IllegalArgumentException("Attribute app:showOrderBySection should be true if you want to add order by types")
        }
        filtersOrderBySection.addElement(elementFilter)
    }

    fun addRangeElementFromTypedArray(array: TypedArray) {
        addRangeElement(array.toFiltersRange(context))
    }

    fun addRangeElement(element: RangeFilter) {
        filtersSection.addView(element)
        rangeFilterElements.add(element)
    }

    fun addCheckboxElementFromTypedArray(array: TypedArray) {
        addCheckboxElement(array.toFiltersCheckbox(context))
    }

    fun addCheckboxElement(element: CheckboxFilter) {
        filtersSection.addView(element)
        checkboxFilterElements.add(element)
    }

    fun addElementsFromArray(@IdRes arrayResId: Int) {
        processElementsTypedArray(getFiltersArray(arrayResId))
    }

    private fun processElementsTypedArray(array: TypedArray) {
        if (array.length() < 1) {
            throw IllegalArgumentException("Attribute app:filters should reference an array with at least one array as item")
        }
        for (i in 0..(array.length() - 1)) {
            val filterArray = getFiltersArray(array.getResourceIdOrThrow(i))
            when (filterArray.getResourceIdOrThrow(0)) {
                FilterType.ORDER_BY_TYPE.id -> addOrderByElementFromTypedArray(filterArray)
                FilterType.RANGE_TYPE.id -> addRangeElementFromTypedArray(filterArray)
                FilterType.CHECKBOX_TYPE.id -> addCheckboxElementFromTypedArray(filterArray)
                else -> throw IllegalArgumentException("Invalid filter type")
            }
        }
    }

    private fun getFiltersArray(@IdRes resId: Int): TypedArray =
        resources.obtainTypedArray(resId)

    private fun getElementIdName(@IdRes id: Int): String =
        resources.getResourceEntryName(id)
}
