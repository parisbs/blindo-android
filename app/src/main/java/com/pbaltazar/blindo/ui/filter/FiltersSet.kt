package com.pbaltazar.blindo.ui.filter

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ArrayRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.content.res.getResourceIdOrThrow
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange
import com.pbaltazar.blindo.entities.filters.sorts.AppSort
import com.pbaltazar.blindo.entities.filters.sorts.PackSort
import com.pbaltazar.blindo.entities.filters.sorts.RatingSort
import com.pbaltazar.blindo.components.filters.FiltersScreen
import com.pbaltazar.blindo.components.filters.extensions.toFiltersCheckbox
import com.pbaltazar.blindo.components.filters.extensions.toFiltersRange
import com.pbaltazar.blindo.utils.extensions.toIntRange
import kotlin.reflect.KClass

enum class FiltersSet(
    @StringRes
    val title: Int,
    @ArrayRes
    val arrayResId: Int,
    val showOrderBySection: Boolean,
    val autoAddCommonElements: Boolean,
    val orderByEnum: KClass<out Enum<*>>
) : FiltersDefaults {

    APP(
        R.string.apps_filter__title,
        R.array.app_filters,
        true,
        true,
        AppSort::class
    ) {
        override fun getOrderByDefault(): String = AppSort.UPDATED_AT_DESC.name
    },
    APP_PACKS(
        R.string.packs_filters__title,
        R.array.app_packs_filters,
        true,
        true,
        PackSort::class
    ) {
        override fun getPageSizeDefault(): Int = 15

        override fun getOrderByDefault(): String = PackSort.UPDATED_AT_DESC.name
    },
    APP_RATINGS(
        R.string.ratings_filters__title,
        R.array.app_ratings_filters,
        true,
        true,
        RatingSort::class
    ) {
        override fun getPageSizeDefault(): Int = 15

        override fun getOrderByDefault(): String = RatingSort.UPDATED_AT_DESC.name
    };

    override fun isRangeCheckedDefault(context: Context, id: Int): Boolean = getFilterTypedArray(context, id)
        ?.toFiltersRange(context)?.isExpanded ?: throw NullPointerException("The filter ${getResourceName(context, id)} no exists in this filters screen.")

    override fun getFloatRangeDefault(context: Context, id: Int): FloatRange = getFilterTypedArray(context, id)
        ?.toFiltersRange(context)?.let { rangeFilter ->
            FloatRange(
                begin = rangeFilter.valueFrom,
                end = rangeFilter.valueTo
            )
        } ?: throw NullPointerException("The filter ${getResourceName(context, id)} no exists in this filters screen.")

    override fun getIntRangeDefault(context: Context, id: Int): IntRange =
        getFloatRangeDefault(context, id).toIntRange()

    override fun getCheckboxDefault(context: Context, id: Int): Boolean = getFilterTypedArray(context, id)
        ?.toFiltersCheckbox(context)?.isChecked ?: throw NullPointerException("The filter ${getResourceName(context, id)} no exists in this filters screen.")

    fun getTitle(context: Context): String =
        context.getString(title)

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

    private fun getFilterTypedArray(context: Context, @ArrayRes id: Int): TypedArray? =
        getTypedArrayResource(context, arrayResId).let { typedArray ->
            var filter: TypedArray? = null
            for (i in 0 until typedArray.length()) {
                val filterCandidate: TypedArray = getTypedArrayResource(context, typedArray.getResourceIdOrThrow(i))
                if (filterCandidate.getResourceIdOrThrow(1) == id) {
                    filter = filterCandidate
                    break
                }
            }
            filter
        }

    private fun getTypedArrayResource(context: Context, @ArrayRes id: Int): TypedArray =
        context.resources.obtainTypedArray(id)

    private fun getResourceName(context: Context, @IdRes id: Int): String =
        context.resources.getResourceEntryName(id)
}
