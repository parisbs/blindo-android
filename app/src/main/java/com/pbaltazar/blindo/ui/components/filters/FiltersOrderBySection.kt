package com.pbaltazar.blindo.ui.components.filters

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.view.ViewCompat
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ComponentFiltersOrderBySectionBinding
import com.pbaltazar.blindo.ui.components.filters.elements.OrderByElementFilter
import com.pbaltazar.blindo.ui.components.filters.entities.orderby.OrderBySelection
import com.pbaltazar.blindo.ui.components.filters.extensions.toFiltersOrderByElement

class FiltersOrderBySection @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr),
    OrderByElementFilter.OnCheckedChangeListener {

    companion object {
        const val PREFERENCES_KEY: String = "orderBy"
    }

    private lateinit var binding: ComponentFiltersOrderBySectionBinding

    private lateinit var container: LinearLayout

    private val commonElementFilters: List<OrderByElementFilter> = listOf(
        OrderByElementFilter(context).apply {
            id = R.id.filters_order_by_updated_at
            text = resources.getString(R.string.filter__updated_at)
            isChecked = false
        }
    )

    var autoAddCommonElements: Boolean = false
    set(value) {
        field = value
        var requiresRefresh: Boolean = false
        for (commonElement in commonElementFilters.reversed()) {
            if (field) {
                if (elementFilters.contains(commonElement).not()) {
                    addElement(0, commonElement, false)
                    requiresRefresh = true
                }
            } else {
                if (elementFilters.size >= commonElementFilters.size) {
                    container.removeView(commonElement)
                    elementFilters.remove(commonElement)
                    requiresRefresh = true
                }
            }
        }
        if (requiresRefresh) {
            refreshElements()
        }
    }

    private val elementFilters: MutableList<OrderByElementFilter> = mutableListOf()

    private val selections: MutableList<OrderByElementFilter> = mutableListOf()

    init {
        binding = ComponentFiltersOrderBySectionBinding.inflate(LayoutInflater.from(context), this)

        container = binding.elementsOrderByContainer
        ViewCompat.setAccessibilityHeading(binding.orderByHeader, true)

        var elementsArray: TypedArray? = null

        attrs?.also {
            val a = context.obtainStyledAttributes(it, R.styleable.FiltersOrderBySection)

            autoAddCommonElements = a.getBoolean(R.styleable.FiltersOrderBySection_autoAddCommonElements, false)
            elementsArray = a.getResourceId(R.styleable.FiltersOrderBySection_elements, View.NO_ID).let {
                if (it == View.NO_ID) {
                    null
                } else {
                    getElementsArray(it)
                }
            }

            a.recycle()
        }

        elementsArray?.also {
            for (i in 0..(it.length() - 1)) {
                addElement(
                    container.childCount,
                    getElementsArray(it.getResourceIdOrThrow(i)).toFiltersOrderByElement(context),
                    false
                )
            }
        }
        refreshElements()
    }

    override fun onCheckedChanged(selection: OrderBySelection) {
        elementFilters.filter { it.id == selection.elementId }.takeIf { it.size == 1 }?.first()?.also {
            val selectedIds: List<Int> = selections.mapNotNull { it.id }
            if (selectedIds.contains(it.id)) {
                if (it.isChecked.not()) {
                    selections.removeAt(selectedIds.indexOf(it.id))
                    it.removeBadgePosition()
                }
            } else {
                if (it.isChecked) {
                    selections.add(it)
                    it.setPositionBadge(selections.size)
                }
            }
        }
        refreshElements()
    }

    fun addElementFromTypedArray(array: TypedArray) {
        addElementFromTypedArray(container.childCount, array)
    }

    fun addElementFromTypedArray(position: Int, array: TypedArray) {
        addElement(position, array.toFiltersOrderByElement(context))
    }

    fun addElement(elementFilter: OrderByElementFilter) {
        addElement(container.childCount, elementFilter)
    }

    fun addElement(position: Int, elementFilter: OrderByElementFilter) {
        addElement(position, elementFilter, true)
    }

    private fun addElement(
        position: Int,
        elementFilter: OrderByElementFilter,
        refreshSelections: Boolean = true
    ) {
        elementFilter.setCustomCheckedListener(this)
        container.addView(elementFilter, position)
        elementFilters.add(position, elementFilter)
        if (refreshSelections) {
            refreshElements()
        }
    }

    fun getElements(): List<OrderByElementFilter> =
        elementFilters.toList()

    fun clearElements() {
        container.removeAllViews()
        elementFilters.clear()
        selections.clear()
    }

    fun getSelection(@IdRes elementId: Int): OrderBySelection =
        elementFilters.filter { it.id == elementId }.takeIf { it.size == 1 }
            ?.first()?.selection ?: throw NullPointerException("The elementId no exists.")

    fun setSelection(selection: OrderBySelection) {
        setSelection(selection, true)
    }

    private fun setSelection(selection: OrderBySelection, refreshSelections: Boolean) =
        elementFilters.filter { it.id == selection.elementId }.takeIf { it.size == 1 }?.first()?.apply {
            this.selection = selection
            if (refreshSelections) {
                refreshElements()
            }
        } ?: throw NullPointerException("The elementId no exists.")

    fun getSelections(): List<OrderBySelection> = selections.mapNotNull { it.selection }

    fun setSelections(selections: List<OrderBySelection>) {
        this.selections.clear()
        selections.forEach { selection ->
            setSelection(selection, false)
        }
        refreshElements()
    }

    private fun refreshElements() = elementFilters.forEach { element ->
            val selectionsIds: List<Int> = selections.mapNotNull { it.id }
            var currentPosition: Int = selections.size
            val currentChecked: Boolean = element.isChecked
            if (selectionsIds.contains(element.id)) {
                currentPosition = selectionsIds.indexOf(element.id)
                if (currentChecked.not()) {
                    selections.removeAt(currentPosition)
                    element.removeBadgePosition()
                } else {
                    if ((currentPosition + 1) != element.getPositionBadge()) {
                        element.setPositionBadge(currentPosition + 1)
                    }
                }
            } else {
                if (currentChecked) {
                    selections.add(currentPosition, element)
                    element.setPositionBadge(currentPosition + 1)
                } else {
                    element.removeBadgePosition()
                }
            }
        }

    private fun getElementFromTypedArray(array: TypedArray): OrderByElementFilter =
        array.toFiltersOrderByElement(context)

    private fun getElementsArray(@IdRes resId: Int): TypedArray =
        resources.obtainTypedArray(resId)
}
