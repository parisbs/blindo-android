package com.pbaltazar.blindo.ui.components.filters.extensions

import android.content.Context
import android.content.res.TypedArray
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.getStringOrThrow
import com.pbaltazar.blindo.ui.components.filters.elements.CheckboxFilter
import com.pbaltazar.blindo.ui.components.filters.elements.OrderByElementFilter
import com.pbaltazar.blindo.ui.components.filters.elements.RangeFilter

fun TypedArray.toFiltersOrderByElement(context: Context): OrderByElementFilter {
    if (length() < 3) {
        throw IllegalArgumentException("At least 3 values should be setted: filter type, ID and text.")
    }
    if (getResourceIdOrThrow(0) != OrderByElementFilter.FILTER_TYPE) {
        throw IllegalArgumentException("Invalid filter type, should be order by")
    }
    return OrderByElementFilter(context).apply {
        id = this@toFiltersOrderByElement.getResourceIdOrThrow(1)
        text = resources.getString(this@toFiltersOrderByElement.getResourceIdOrThrow(2))
        isChecked = if (this@toFiltersOrderByElement.length() >= 4)
            resources.getBoolean(this@toFiltersOrderByElement.getResourceIdOrThrow(3))
        else false
    }
}

fun TypedArray.toFiltersRange(context: Context): RangeFilter {
    if (length() < 6) {
        throw IllegalArgumentException("At least 6 values should be setted: filter type, ID, text, value from, value to and step size.")
    }
    if (getResourceIdOrThrow(0) != RangeFilter.FILTER_TYPE) {
        throw IllegalArgumentException("Invalid filter type, should be range")
    }
    return RangeFilter(context).apply {
        id = getResourceIdOrThrow(1)
        text = resources.getString(this@toFiltersRange.getResourceIdOrThrow(2))
        valueFrom = resources.getFloat(this@toFiltersRange.getResourceIdOrThrow(3))
        valueTo = resources.getFloat(this@toFiltersRange.getResourceIdOrThrow(4))
        stepSize = resources.getFloat(this@toFiltersRange.getResourceIdOrThrow(5))
        isExpanded = if (this@toFiltersRange.length() >= 7)
            resources.getBoolean(this@toFiltersRange.getResourceIdOrThrow(6))
        else false
    }
}

fun TypedArray.toFiltersCheckbox(context: Context): CheckboxFilter {
    if (length() < 3) {
        throw IllegalArgumentException("At least 3 values should be setted: filter type, ID and text.")
    }
    if (getResourceIdOrThrow(0) != CheckboxFilter.FILTER_TYPE) {
        throw IllegalArgumentException("Invalid filter type, should be checkbox")
    }
    return CheckboxFilter(context).apply {
        id = getResourceIdOrThrow(1)
        text = resources.getString(this@toFiltersCheckbox.getResourceIdOrThrow(2))
        isChecked = if (this@toFiltersCheckbox.length() >= 4)
            resources.getBoolean(this@toFiltersCheckbox.getResourceIdOrThrow(3))
        else false
    }
}
