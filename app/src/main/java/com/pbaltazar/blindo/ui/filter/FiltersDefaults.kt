package com.pbaltazar.blindo.ui.filter

import android.content.Context
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange

interface FiltersDefaults {

    fun getPageSizeDefault(): Int = 50

    fun getOrderByDefault(): String = ""

    fun isRangeCheckedDefault(
        context: Context,
        id: Int
    ): Boolean

    fun getFloatRangeDefault(
        context: Context,
        id: Int
    ): FloatRange

    fun getIntRangeDefault(
        context: Context,
        id: Int
    ): IntRange

    fun getCheckboxDefault(
        context: Context,
        id: Int
    ): Boolean
}
