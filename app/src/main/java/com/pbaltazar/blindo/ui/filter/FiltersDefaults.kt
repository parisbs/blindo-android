package com.pbaltazar.blindo.ui.filter

import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange
import com.pbaltazar.blindo.ui.components.filters.elements.CheckboxFilter
import com.pbaltazar.blindo.ui.components.filters.elements.RangeFilter
import com.pbaltazar.blindo.utils.extensions.toIntRange

interface FiltersDefaults {
    fun getOrderByDefault(): String? = null
    fun getFloatRangeDefault(range: RangeFilter): FloatRange = FloatRange(
        begin = range.valueFrom,
        end = range.valueTo
    )
    fun getIntRangeDefault(range: RangeFilter): IntRange = FloatRange(
        begin = range.valueFrom,
        end = range.valueTo
    ).toIntRange()
    fun getCheckboxDefault(checkbox: CheckboxFilter): Boolean = false
}
