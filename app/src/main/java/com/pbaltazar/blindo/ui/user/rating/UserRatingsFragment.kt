package com.pbaltazar.blindo.ui.user.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blindoapp.uitools.recyclerview.PaginationScrollListener
import com.pbaltazar.blindo.databinding.FragmentUserCommentsBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.sorts.RatingSort
import com.pbaltazar.blindo.entities.inputs.RatingInput
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG
import com.wizeline.viewstate.State
import com.wizeline.viewstate.ViewState
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserRatingsFragment : AuthenticableFragment() {

    private val userRatingsViewModel: UserRatingsViewModel by viewModel()
    private var binding: FragmentUserCommentsBinding? = null

    private lateinit var userCommentsViewState: ViewState
    private lateinit var userCommentsRecycler: RecyclerView

    private val userRatingsAdapter: UserRatingsAdapter =
        UserRatingsAdapter({ rating ->
            onCommentClickListener(rating)
        })

    private var sort: List<RatingSort> = listOf(
        RatingSort.UPDATED_AT_DESC
    )
    private var pageSize: Int = 30
    private var nextPageToken: String? = null

    private var isLoading: Boolean = false
    private var hasNextPage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeUser()
        subscribeComments()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserCommentsBinding.inflate(inflater, container, false)
        userCommentsViewState = binding!!.userCommentsViewState
        userCommentsRecycler = binding!!.userCommentsRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeAuth()
        setupUi()
    }

    override fun onResume() {
        super.onResume()
        if (userRatingsAdapter.itemCount == 0 && isLoading.not()) {
            loadComments()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun subscribeAuth() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
        AUTH_CANCELED_ON_DIALOG)?.observe(this, Observer {
        if (it.not()) {
            loginScreen.launch(Unit)
        } else {
            findNavController().popBackStack()
        }
    })

    private fun subscribeComments() = userRatingsViewModel.ratings.observe(this, Observer {
        isLoading = false
        when (val response = it) {
            is UserRatingsViewModel.UserRatings.Success -> {
                hasNextPage = response.hasNextPage
                nextPageToken = response.nextPageToken
                if (userRatingsAdapter.itemCount == 0) {
                    userCommentsViewState.setState(State.CONTENT)
                    userRatingsAdapter.items = response.ratings.toMutableList()
                } else {
                    userRatingsAdapter.appendItems(response.ratings)
                }
            }
            is UserRatingsViewModel.UserRatings.Empty -> {
                if (userRatingsAdapter.itemCount == 0) {
                    userCommentsViewState.setState(State.EMPTY)
                }
            }
            is UserRatingsViewModel.UserRatings.Error -> {
                if (userRatingsAdapter.itemCount == 0) {
                    userCommentsViewState.apply {
                        setState(State.ERROR)
                        setErrorDescriptionText(response.errorMessage)
                    }
                }
            }
        }
    })

    private fun setupUi() {
        userCommentsRecycler.apply {
            adapter = userRatingsAdapter
            addOnScrollListener(
                object: PaginationScrollListener(
                    if (layoutManager != null)
                        layoutManager as LinearLayoutManager
                else
                        LinearLayoutManager(this@UserRatingsFragment.requireContext())
                ) {
                    override fun hasNextPage(): Boolean = hasNextPage

                    override fun prefetchDistance(): Int = 10

                    override fun isLoading(): Boolean = isLoading

                    override fun loadMoreItems() = loadComments()
                }
            )
        }
        userCommentsViewState.setOnRetryClickListener {
            loadComments()
        }
    }

    private fun loadComments() {
        if (userRatingsAdapter.itemCount == 0) {
            userCommentsViewState.setState(State.LOADING)
            hasNextPage = false
            nextPageToken = null
        }
        getUser()?.also { user ->
            isLoading = true
            userRatingsViewModel.getUserRatings(
                RatingInput(
                    userId = user.id,
                    sort = sort,
                    pageSize = pageSize,
                    nextPageToken = nextPageToken
                )
            )
        } ?: findNavController().navigate(
            UserRatingsFragmentDirections.actionFromUserCommentsToRequiresAuth()
        )
    }

    private fun onCommentClickListener(rating: Rating) {
        findNavController().navigate(
            UserRatingsFragmentDirections.actionFromUserCommentsToCommentCreator(
                rating.app ?: App(),
                rating
            )
        )
    }
}
