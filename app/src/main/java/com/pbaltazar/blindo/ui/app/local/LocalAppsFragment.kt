package com.pbaltazar.blindo.ui.app.local

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.pbaltazar.blindo.databinding.FragmentLocalAppsBinding
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.wizeline.viewstate.State
import com.wizeline.viewstate.ViewState
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocalAppsFragment : BlindoFragment<FragmentLocalAppsBinding>() {

    private val localAppsViewModel: LocalAppsViewModel by viewModel()

    private lateinit var localAppsViewState: ViewState
    private lateinit var localAppsRecycler: RecyclerView

    private val localAppsAdapter: LocalAppsAdapter =
        LocalAppsAdapter({ app ->
            onAppClickListener(app)
        })

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeApps()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLocalAppsBinding.inflate(inflater, container, false)
        localAppsViewState = binding!!.localAppsViewState
        localAppsRecycler = binding!!.localAppsRecycler
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    override fun onResume() {
        super.onResume()
        if (localAppsAdapter.itemCount == 0) {
            loadApps()
        }
    }

    private fun loadApps() {
        if (localAppsAdapter.itemCount == 0) {
            localAppsViewState.setState(State.LOADING)
        }
        localAppsViewModel.loadLocalApps()
    }

    private fun subscribeApps() = localAppsViewModel.apps.observe(this, Observer {
        when (val response = it) {
            is LocalAppsViewModel.AppsViewState.Success -> {
                if (localAppsAdapter.itemCount == 0) {
                    localAppsAdapter.items = response.apps.toMutableList()
                    localAppsViewState.setState(State.CONTENT)
                } else {
                    localAppsAdapter.appendItems(response.apps)
                }
            }
            is LocalAppsViewModel.AppsViewState.Empty -> {
                if (localAppsAdapter.itemCount == 0) {
                    localAppsViewState.setState(State.EMPTY)
                }
            }
            is LocalAppsViewModel.AppsViewState.Error -> {
                if (localAppsAdapter.itemCount == 0) {
                    localAppsViewState.setState(State.ERROR)
                }
            }
        }
    })

    private fun setupUi() {
        localAppsRecycler.apply {
            adapter = localAppsAdapter
        }
        localAppsViewState.setOnRetryClickListener {
            loadApps()
        }
    }

    private fun onAppClickListener(app: App) {
        findNavController().navigate(
            LocalAppsFragmentDirections.actionFromLocalAppsToAppDetails(
                packageName = app.packageName,
                app = app
            )
        )
    }
}
