package com.pbaltazar.blindo.ui.user.profile.publicprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.filters.sorts.PackSort
import com.pbaltazar.blindo.entities.filters.sorts.RatingSort
import com.pbaltazar.blindo.entities.inputs.PackInput
import com.pbaltazar.blindo.entities.inputs.RatingInput
import com.pbaltazar.blindo.entities.inputs.UserInput
import com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.packs.UserPacksPagination
import com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.ratings.UserRatingsPagination
import com.pbaltazar.blindo.usecases.QueryGetPublicUserPacks
import com.pbaltazar.blindo.usecases.QueryGetPublicUserRatings
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

class UserProfileViewModel(
    private val userPreferences: UserPreferences,
    private val queryGetPublicUserPacks: QueryGetPublicUserPacks,
    private val queryGetPublicUserRatings: QueryGetPublicUserRatings
) : ViewModel() {

    private var user: User? = null

    fun setUser(user: User) {
        this.user = user
    }

    val userPacks: Flow<PagingData<User>> = Pager(
        PagingConfig(
            pageSize = getUserPacksPageSize(),
            prefetchDistance = 10
        )
    ) {
        UserPacksPagination(queryGetPublicUserPacks, getUserPacksInput())
    }.flow
        .cachedIn(viewModelScope)

    val userRatings: Flow<PagingData<User>> = Pager(
        PagingConfig(
            pageSize = getUserRatingsPageSize(),
            prefetchDistance = 10
        )
    ) {
        UserRatingsPagination(queryGetPublicUserRatings, getUserRatingInput())
    }.flow
        .cachedIn(viewModelScope)

    private fun getUserPacksInput(): UserInput = UserInput(
        id = user!!.id,
        packInput = getPackInput()
    )

    private fun getUserRatingInput(): UserInput = UserInput(
        id = user!!.id,
        ratingInput = getRatingInput()
    )

    private fun getPackInput(): PackInput = PackInput(
        sort = getUserPacksSort(),
        pageSize = 25
    )

    private fun getRatingInput(): RatingInput = RatingInput(
        sort = getUserRatingsSort(),
        pageSize = getUserRatingsPageSize()
    )

    fun getUserPacksPageSize(): Int = 25

    fun getUserPacksSort(): List<PackSort> = listOf(
        PackSort.UPDATED_AT_DESC
    )

    fun getUserRatingsPageSize(): Int = 25

    fun getUserRatingsSort(): List<RatingSort> = listOf(
        RatingSort.UPDATED_AT_DESC
    )
}
