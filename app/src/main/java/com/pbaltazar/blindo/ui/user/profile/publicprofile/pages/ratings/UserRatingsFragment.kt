package com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.ratings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.flatMap
import androidx.recyclerview.widget.RecyclerView
import com.pbaltazar.blindo.databinding.FragmentUserRatingsBinding
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.ui.user.profile.publicprofile.UserProfileFragmentDirections
import com.pbaltazar.blindo.ui.user.profile.publicprofile.UserProfileViewModel
import com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.UserProfilePagerHelper
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.pbaltazar.blindo.utils.pagination.ui.PaginationStateAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserRatingsFragment : BlindoFragment<FragmentUserRatingsBinding>() {

    private var userProfileViewModel: UserProfileViewModel? = null

    private lateinit var userRatingsRecycler: RecyclerView

    private val userRatingsAdapter: UserRatingsAdapter = UserRatingsAdapter(
        UserRatingsComparator,
        { rating ->
            onRatingClickListener(rating)
        }
    )

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userProfileViewModel = UserProfilePagerHelper.userProfileViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUserRatingsBinding.inflate(inflater, container, false)
        userRatingsRecycler = binding!!.userRatingsRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        userRatingsRecycler.adapter = userRatingsAdapter.withLoadStateFooter(
            footer = PaginationStateAdapter({ userRatingsAdapter.retry() })
        )

        viewLifecycleOwner.lifecycleScope.launch {
            userProfileViewModel!!.userRatings.collectLatest { value: PagingData<User> ->
                value.flatMap { it.ratings?.ratings ?: emptyList() }.also { pagingData ->
                    userRatingsAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
                }
            }
        }
    }

    private fun onRatingClickListener(rating: Rating) {
        findNavController().navigate(
            UserProfileFragmentDirections.actionFromPublicUserProfileToRatingDetails(rating)
        )
    }
}
