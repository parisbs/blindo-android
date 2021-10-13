package com.pbaltazar.blindo.ui.filter

import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.entities.filters.sorts.PackSort
import com.pbaltazar.blindo.entities.filters.sorts.RatingSort
import com.pbaltazar.blindo.ui.components.filters.FiltersScreen
import kotlin.reflect.KClass

enum class FiltersSet(
    @StringRes
    val title: Int,
    @IdRes
    val arrayResId: Int,
    val showOrderBySection: Boolean,
    val autoAddCommonElements: Boolean,
    val orderByEnum: KClass<out Enum<*>>
) : FiltersDefaults {
    APP_PACKS(
        R.string.packs_filters__title,
        R.array.app_packs_filters,
        true,
        true,
        PackSort::class
    ) {
        override fun getOrderByDefault(): String = PackSort.UPDATED_AT_DESC.name
    },
    APP_RATINGS(
        R.string.ratings_filters__title,
        R.array.app_ratings_filters,
        true,
        true,
        RatingSort::class
    ) {
        override fun getOrderByDefault(): String = RatingSort.UPDATED_AT_DESC.name
    };

    fun getPreferencesKeyForTypeAndId(
        context: Context,
        filterType: FiltersScreen.Companion.FilterType,
        @IdRes id: Int
    ): String =
        when (filterType) {
            FiltersScreen.Companion.FilterType.ORDER_BY_TYPE -> "${this.name}__${filterType.name}"
            else -> "${this.name}__${filterType.name}__${getResourceName(context, id)}"
        }

    fun getPreferencesKeyForPageSize(): String =
        "${this.name}__pageSize"

    private fun getResourceName(context: Context, @IdRes id: Int): String =
        context.resources.getResourceEntryName(id)
}
