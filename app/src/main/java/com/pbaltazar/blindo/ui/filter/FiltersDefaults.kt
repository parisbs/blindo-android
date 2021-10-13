package com.pbaltazar.blindo.ui.filter

import android.content.Context
import androidx.annotation.IdRes
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange

interface FiltersDefaults {

    fun getPageSizeDefault(): Int = 50

    fun getOrderByDefault(): String = ""

    fun isRangeCheckedDefault(
        context: Context,
        @IdRes id: Int
    ): Boolean

    fun getFloatRangeDefault(
        context: Context,
        @IdRes id: Int
    ): FloatRange

    fun getIntRangeDefault(
        context: Context,
        @IdRes id: Int
    ): IntRange

    fun getCheckboxDefault(
        context: Context,
        @IdRes id: Int
    ): Boolean
}
