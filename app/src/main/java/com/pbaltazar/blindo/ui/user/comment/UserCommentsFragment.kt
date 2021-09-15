package com.pbaltazar.blindo.ui.user.comment

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
import com.pbaltazar.blindo.entities.enums.CommentSort
import com.pbaltazar.blindo.entities.inputs.CommentInput
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticationViewModel
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG
import com.wizeline.viewstate.State
import com.wizeline.viewstate.ViewState
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserCommentsFragment : AuthenticableFragment() {

    private val userCommentsViewModel: UserCommentsViewModel by viewModel()
    private var binding: FragmentUserCommentsBinding? = null

    private lateinit var userCommentsViewState: ViewState
    private lateinit var userCommentsRecycler: RecyclerView

    private val userCommentsAdapter: UserCommentsAdapter =
        UserCommentsAdapter({ rating ->
            onCommentClickListener(rating)
        })

    private var sort: List<CommentSort> = listOf(
        CommentSort.UPDATED_AT_DESC
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
        if (userCommentsAdapter.itemCount == 0 && isLoading.not()) {
            loadComments()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loginScreen.unregister()
        binding = null
    }

    override fun onSubscribeUser() {
        // Not required
    }

    override fun onSubscribeAuthentication(userAuthentication: AuthenticationViewModel.UserAuthentication) {
        // Not required
    }

    override fun onSubscribeUserUpdate(userUpdate: AuthenticationViewModel.UserUpdate) {
        // Not required
    }

    private fun subscribeAuth() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
        AUTH_CANCELED_ON_DIALOG)?.observe(this, Observer {
        if (it.not()) {
            loginScreen.launch(Unit)
        } else {
            findNavController().popBackStack()
        }
    })

    private fun subscribeComments() = userCommentsViewModel.comments.observe(this, Observer {
        isLoading = false
        when (val response = it) {
            is UserCommentsViewModel.UserComments.Success -> {
                hasNextPage = response.hasNextPage
                nextPageToken = response.nextPageToken
                if (userCommentsAdapter.itemCount == 0) {
                    userCommentsViewState.setState(State.CONTENT)
                    userCommentsAdapter.items = response.comments.toMutableList()
                } else {
                    userCommentsAdapter.appendItems(response.comments)
                }
            }
            is UserCommentsViewModel.UserComments.Empty -> {
                if (userCommentsAdapter.itemCount == 0) {
                    userCommentsViewState.setState(State.EMPTY)
                }
            }
            is UserCommentsViewModel.UserComments.Error -> {
                if (userCommentsAdapter.itemCount == 0) {
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
            adapter = userCommentsAdapter
            addOnScrollListener(
                object: PaginationScrollListener(
                    if (layoutManager != null)
                        layoutManager as LinearLayoutManager
                else
                        LinearLayoutManager(this@UserCommentsFragment.requireContext())
                ) {
                    override fun hasNextPage(): Boolean = hasNextPage

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
        if (userCommentsAdapter.itemCount == 0) {
            userCommentsViewState.setState(State.LOADING)
            hasNextPage = false
            nextPageToken = null
        }
        getUser()?.also { user ->
            isLoading = true
            userCommentsViewModel.getUserComments(
                CommentInput(
                    userId = user.id,
                    sort = sort,
                    pageSize = pageSize,
                    nextPageToken = nextPageToken
                )
            )
        } ?: findNavController().navigate(
            UserCommentsFragmentDirections.actionFromUserCommentsToRequiresAuth()
        )
    }

    private fun onCommentClickListener(rating: Rating) {
        findNavController().navigate(
            UserCommentsFragmentDirections.actionFromUserCommentsToCommentCreator(
                rating.app ?: App(),
                rating
            )
        )
    }
}
