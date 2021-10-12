package com.pbaltazar.blindo.ui.app.details

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.filters.RatingFilters
import com.pbaltazar.blindo.entities.filters.sorts.PackSort
import com.pbaltazar.blindo.entities.filters.sorts.RatingSort
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.inputs.PackInput
import com.pbaltazar.blindo.entities.inputs.RatingInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.ui.components.filters.FiltersScreen
import com.pbaltazar.blindo.ui.filter.FiltersSet
import com.pbaltazar.blindo.usecases.*
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class AppViewModel(
    private val backgroundContext: CoroutineContext,
    private val context: Context,
    private val userPreferences: UserPreferences,
    private val queryGetApp: QueryGetApp,
    private val queryGetAppByPackageName: QueryGetAppByPackageName,
    private val queryGetAppOnly: QueryGetAppOnly,
    private val queryGetAppPacks: QueryGetAppPacks,
    private val queryGetAppPacksByPackageName: QueryGetAppPacksByPackageName,
    private val queryGetAppRatings: QueryGetAppRatings,
    private val queryGetAppRatingsByPackageName: QueryGetAppRatingsByPackageName
) : ViewModel() {

    private var isQueryById: Boolean = false

    private val appDetailsResponse = MutableLiveData<AppDetails>()
    val appDetails: LiveData<AppDetails> get() = appDetailsResponse

    private val appStatisticsResponse = MutableLiveData<AppStatistics>()
    val statistics: LiveData<AppStatistics> get() = appStatisticsResponse

    private val packsList = MutableLiveData<PacksList>()
    val packs: LiveData<PacksList> get() = packsList

    private val ratingsList = MutableLiveData<RatingsList>()
    val ratings: LiveData<RatingsList> get() = ratingsList

    fun getIsQueryById(): Boolean = isQueryById

    fun setIsQueryById(isQueryById: Boolean) {
        this.isQueryById = isQueryById
    }

    fun getPacksPageSize(): Int =
        userPreferences.getInt(FiltersSet.APP_PACKS.getPreferencesKeyForPageSize(), 15)

    fun getPackSort(): List<PackSort> =
        userPreferences.getString(
            FiltersSet.APP_PACKS.getPreferencesKeyForTypeAndId(context, FiltersScreen.Companion.FilterType.ORDER_BY_TYPE, R.id.filters_screen_order_by_type),
            FiltersSet.APP_PACKS.getOrderByDefault() ?: ""
        ).split(",").mapNotNull { PackSort.valueOf(it) }

    fun getRatingsPageSize(): Int =
        userPreferences.getInt(FiltersSet.APP_RATINGS.getPreferencesKeyForPageSize(), 15)

    fun getRatingsSort(): List<RatingSort> =
        userPreferences.getString(
            FiltersSet.APP_RATINGS.getPreferencesKeyForTypeAndId(context, FiltersScreen.Companion.FilterType.ORDER_BY_TYPE, R.id.filters_screen_order_by_type),
            FiltersSet.APP_RATINGS.getOrderByDefault()
        ).split(",").mapNotNull { RatingSort.valueOf(it) }

    fun getAppRatingsWithComment(): Boolean =
        userPreferences.getBoolean(
            FiltersSet.APP_RATINGS.getPreferencesKeyForTypeAndId(
                context,
                FiltersScreen.Companion.FilterType.CHECKBOX_TYPE,
                R.id.appRatingsFiltersWithComment
            ),
            false
        )

    fun getAppRatingsInLanguages(): List<String>? =
        userPreferences.getBoolean(
            FiltersSet.APP_RATINGS.getPreferencesKeyForTypeAndId(
                context,
                FiltersScreen.Companion.FilterType.CHECKBOX_TYPE,
                R.id.appRatingsFiltersOnlyMyLanguage
            ),
            false
        ).let { inMyLanguage ->
            if (inMyLanguage) {
                listOf(
                    Locale.getDefault().language
                )
            } else null
        }

    fun getAppRatingsFilters(): RatingFilters = RatingFilters(
        commentIsNull = getAppRatingsWithComment().not(),
        commentLanguageIn = getAppRatingsInLanguages()
    )

    fun getRatingInput(): RatingInput = RatingInput(
        filters = getAppRatingsFilters(),
        sort = getRatingsSort(),
        pageSize = getRatingsPageSize()
    )

    fun getApp(id: String? = null, packageName: String? = null) = viewModelScope.launch(backgroundContext) {
        val appInput = AppInput(
            id = id ?: "",
            packageName = packageName ?: "",
            packInput = PackInput(
                sort = getPackSort(),
                pageSize = getPacksPageSize()
            ),
            ratingInput = getRatingInput()
        )
        when (
            val apiResponse = if (isQueryById)
                queryGetApp(appInput)
            else
                queryGetAppByPackageName(appInput)
        ) {
            is ApiResponse.Success -> apiResponse.data.also { app ->
                appDetailsResponse.postValue(AppDetails.Success(app))
            }
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> appDetailsResponse.postValue(AppDetails.Empty)
                is ApiException.WithErrors -> appDetailsResponse.postValue(AppDetails.Error(apiException.errorsList.toString()))
                is ApiException.CallFailure -> appDetailsResponse.postValue(AppDetails.Error(apiException.error.localizedMessage ?: "Unknown call failure"))
            }
        }
    }

    fun getAppStatistics(id: String) = viewModelScope.launch(backgroundContext) {
        when (val apiResponse = queryGetAppOnly(
            AppInput(id = id)
        )) {
            is ApiResponse.Success -> appStatisticsResponse.postValue(
                AppStatistics.Success(apiResponse.data)
            )
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> appStatisticsResponse.postValue(AppStatistics.Empty)
                is ApiException.WithErrors -> appStatisticsResponse.postValue(AppStatistics.Error(apiException.errorsList.joinToString(", ")))
                is ApiException.CallFailure -> appStatisticsResponse.postValue(AppStatistics.Error(apiException.error.localizedMessage ?: apiException.error.toString()))
            }
        }
    }

    fun loadPacks(appInput: AppInput) = viewModelScope.launch(backgroundContext) {
        when (val apiResponse = if (appInput.id.isNotEmpty())
            queryGetAppPacks(appInput)
        else
            queryGetAppPacksByPackageName(appInput)
        ) {
            is ApiResponse.Success -> apiResponse.data.also { packsList ->
                this@AppViewModel.packsList.postValue(
                    PacksList.Success(
                        packs = packsList,
                        hasNextPage = apiResponse.hasNextPage,
                        nextPageToken = apiResponse.nextPageToken
                    )
                )
            }
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> packsList.postValue(PacksList.Empty)
                is ApiException.WithErrors -> packsList.postValue(PacksList.Error(apiException.errorsList.joinToString(", ")))
                is ApiException.CallFailure -> packsList.postValue(PacksList.Error(apiException.error.localizedMessage ?: apiException.error.toString()))
            }
        }
    }

    fun loadRatings(appInput: AppInput) = viewModelScope.launch(backgroundContext) {
        when (val apiResponse = if (appInput.id.isNotEmpty())
            queryGetAppRatings(appInput)
        else
            queryGetAppRatingsByPackageName(appInput)
        ) {
            is ApiResponse.Success -> ratingsList.postValue(
                RatingsList.Success(
                    ratings = apiResponse.data,
                    hasNextPage = apiResponse.hasNextPage,
                    nextPageToken = apiResponse.nextPageToken
                )
            )
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is  ApiException.EmptyResponse -> ratingsList.postValue(RatingsList.Empty)
                is ApiException.WithErrors -> ratingsList.postValue(RatingsList.Error(apiException.errorsList.joinToString(", ")))
                is ApiException.CallFailure -> ratingsList.postValue(RatingsList.Error(apiException.error.localizedMessage ?: apiException.error.toString()))
            }
        }
    }

    sealed class AppDetails {
        class Success(val app: App): AppDetails()
        object Empty: AppDetails()
        class Error(val errorMessage: String): AppDetails()
    }

    sealed class AppStatistics {
        class Success(val statistics: App) : AppStatistics()
        object Empty : AppStatistics()
        class Error(val reason: String) : AppStatistics()
    }

    sealed class PacksList {
        class Success(val packs: List<Pack>, val hasNextPage: Boolean, val nextPageToken: String? = null) : PacksList()
        object Empty : PacksList()
        class Error(val reason: String) : PacksList()
    }

    sealed class RatingsList {
        class Success(val ratings: List<Rating>, val hasNextPage: Boolean, val nextPageToken: String? = null) : RatingsList()
        object Empty : RatingsList()
        class Error(val reason: String) : RatingsList()
    }
}
