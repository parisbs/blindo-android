package com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.packs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.flatMap
import androidx.recyclerview.widget.RecyclerView
import com.pbaltazar.blindo.databinding.FragmentUserPacksBinding
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.ui.user.profile.publicprofile.UserProfileFragmentDirections
import com.pbaltazar.blindo.ui.user.profile.publicprofile.UserProfileViewModel
import com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.UserProfilePagerHelper
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.pbaltazar.blindo.utils.pagination.ui.PaginationStateAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserPacksFragment : BlindoFragment<FragmentUserPacksBinding>() {

    private var userProfileViewModel: UserProfileViewModel? = null

    private lateinit var userPacksRecycler: RecyclerView

    private val userPacksAdapter: UserPacksAdapter = UserPacksAdapter(
        UserPacksComparator,
        { pack ->
            onPackClickListener(pack)
        }
    )

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userProfileViewModel = UserProfilePagerHelper.userProfileViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUserPacksBinding.inflate(inflater, container, false)
        userPacksRecycler = binding!!.userPacksRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        userPacksRecycler.adapter = userPacksAdapter.withLoadStateFooter(
            footer = PaginationStateAdapter({ userPacksAdapter.retry() })
        )

        viewLifecycleOwner.lifecycleScope.launch {
            userProfileViewModel!!.userPacks.collectLatest { value: PagingData<User> ->
                value.flatMap { it.packs?.packs ?: emptyList() }.also { pagingData ->
                    userPacksAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
                }
            }
        }
    }

    private fun onPackClickListener(pack: Pack) {
        findNavController().navigate(
            UserProfileFragmentDirections.actionFromPublicUserProfileToPackDetails(pack)
        )
    }
}
