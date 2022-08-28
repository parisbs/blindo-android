package com.pbaltazar.blindo.ui.user.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blindoapp.uitools.recyclerview.PaginationScrollListener
import com.google.android.material.snackbar.Snackbar
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentBackupBinding
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.filters.sorts.PackSort
import com.pbaltazar.blindo.entities.inputs.PackInput
import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG
import com.pbaltazar.blindo.utils.extensions.installTalkbackPack
import com.pbaltazar.blindo.utils.extensions.saveTalkbackInstallableFile
import com.wizeline.viewstate.State
import com.wizeline.viewstate.ViewState
import org.koin.androidx.viewmodel.ext.android.viewModel

class BackupFragment : AuthenticableFragment<FragmentBackupBinding>() {

    private val backupViewModel: BackupViewModel by viewModel()

    private lateinit var backupViewState: ViewState
    private lateinit var backupRecycler: RecyclerView
    private var downloadBackupMenuItem: MenuItem? = null

    private val backupAdapter: BackupAdapter =
        BackupAdapter { pack ->
            onPackClickListener(pack)
        }

    private var sort: List<PackSort> = listOf(
        PackSort.UPDATED_AT_DESC
    )
    private var pageSize: Int = 30
    private var nextPageToken: String? = null

    private var isLoading: Boolean = false
    private var hasNextPage: Boolean = false

    override val isSearchable: Boolean
        get() = false

    override fun getMenuResId(): Int = R.menu.backup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeUser()
        subscribePacks()
        subscribeBackup()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBackupBinding.inflate(inflater, container, false)
        backupViewState = binding!!.backupViewState
        backupRecycler = binding!!.backupRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeAuth()
        setupUi()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuDownloadBackup -> {
                getUser()?.also { user ->
                    if (user.isPremium.not()) {
                        findNavController().navigate(
                            BackupFragmentDirections.actionFromBackupToRequiresPremium()
                        )
                    } else {
                        if (backupAdapter.itemCount == 0) {
                        showMessage(getString(R.string.backup__empty))
                    } else {
                        downloadBackupMenuItem = item
                            item.isEnabled = false
                        showMessage(getString(R.string.backup__downloading_message))
                        backupViewModel.downloadBackup(SupportedScreenreadersEnum.TALKBACK)
                    }
                    }
                } ?: findNavController().navigate(
                    BackupFragmentDirections.actionFromBackupToRequiresAuth()
                )
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (backupAdapter.itemCount == 0 && isLoading.not()) {
            loadPacks()
        }
    }

    private fun subscribeAuth() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
    AUTH_CANCELED_ON_DIALOG)?.observe(viewLifecycleOwner) {
        if (it.not()) {
            launchLoginScreen()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun subscribePacks() = backupViewModel.packs.observe(this) {
        isLoading = false
        when (val response = it) {
            is BackupViewModel.UserPacks.Success -> {
                hasNextPage = response.hasNextPage
                nextPageToken = response.nextPageToken
                if (backupAdapter.itemCount == 0) {
                    backupViewState.setState(State.CONTENT)
                    backupAdapter.items = response.packs.toMutableList()
                } else {
                    backupAdapter.appendItems(response.packs)
                }
            }
            is BackupViewModel.UserPacks.Empty -> {
                if (backupAdapter.itemCount == 0) {
                    backupViewState.setState(State.EMPTY)
                }
            }
            is BackupViewModel.UserPacks.Error -> {
                if (backupAdapter.itemCount == 0) {
                    backupViewState.apply {
                        setState(State.ERROR)
                        setErrorDescriptionText(response.errorMessage)
                    }
                }
            }
        }
    }

    private fun subscribeBackup() = backupViewModel.backup.observe(this) {
        when (val response = it) {
            is BackupViewModel.InstallableBackup.Success -> {
                downloadBackupMenuItem?.isEnabled = true
                response.installablePack.copy(
                    pack = Pack(
                        hash = response.installablePack.installable.hashCode().toString()
                    )
                ).saveTalkbackInstallableFile(requireContext())?.also { installable ->
                    installable.installTalkbackPack(backupViewState)
                }
            }
            is BackupViewModel.InstallableBackup.Empty -> showMessage(getString(R.string.backup__empty))
            is BackupViewModel.InstallableBackup.Error -> showMessage(response.errorMessage)
        }
    }

    private fun setupUi() {
        backupRecycler.apply {
            adapter = backupAdapter
            addOnScrollListener(
                object: PaginationScrollListener(
                if (layoutManager != null)
                    layoutManager as LinearLayoutManager
                else
                    LinearLayoutManager(this@BackupFragment.requireContext())
            ) {
                    override fun hasNextPage(): Boolean = hasNextPage

                    override fun prefetchDistance(): Int = 10

                    override fun isLoading(): Boolean = isLoading

                    override fun loadMoreItems() = loadPacks()
                }
            )
        }
        backupViewState.setOnRetryClickListener {
            loadPacks()
        }
    }

    private fun loadPacks() {
        if (backupAdapter.itemCount == 0) {
            backupViewState.setState(State.LOADING)
            hasNextPage = false
            nextPageToken = null
        }
        getUser()?.also { user ->
            isLoading = true
            backupViewModel.getUserPacks(
                PackInput(
                    userId = user.id,
                    sort = sort,
                    pageSize = pageSize,
                    nextPageToken = nextPageToken
                )
            )
        } ?: findNavController().navigate(
            BackupFragmentDirections.actionFromBackupToRequiresAuth()
        )
    }

    private fun showMessage(message: String) = Snackbar.make(
        backupViewState,
        message,
        Snackbar.LENGTH_LONG
    ).show()

    private fun onPackClickListener(pack: Pack) {
        findNavController().navigate(
            BackupFragmentDirections.actionFromBackupToPackDetails(
                pack
            )
        )
    }
}
