package com.pbaltazar.blindo.ui.rating.create

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentRatingCreatorBinding
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.inputs.RatingInput
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG
import com.pbaltazar.blindo.utils.extensions.isNullOrEmptyOrBlank
import com.pbaltazar.blindo.utils.extensions.setValueWithAccessibilitySupport
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class RatingCreatorFragment : AuthenticableFragment() {

    private val ratingCreatorViewModel: RatingCreatorViewModel by viewModel()
    private val ratingCreatorFragmentArgs: RatingCreatorFragmentArgs by navArgs()
    private var binding: FragmentRatingCreatorBinding? = null

    private lateinit var totalRating: TextView
    private lateinit var totalRatingBar: RatingBar
    private lateinit var uiRatingBar: RatingBar
    private lateinit var screenreadersRatingBar: RatingBar
    private lateinit var labelsRatingBar: RatingBar
    private lateinit var functionsRatingBar: RatingBar
    private lateinit var performanceRatingBar: RatingBar
    private lateinit var commentText: EditText
    private lateinit var ratingContinue: Button

    private var isUpdate: Boolean = false
    private var currentRatingId: String = ""
    private var isReadyToContinue: Boolean = false
    set(value) {
        field = value
    ratingContinue.isEnabled = value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ratingCreatorViewModel.setTargetApp(ratingCreatorFragmentArgs.app)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRatingCreatorBinding.inflate(inflater, container, false)
        totalRating = binding!!.ratingBars.totalRating
        totalRatingBar = binding!!.ratingBars.totalRatingBar
        uiRatingBar = binding!!.ratingBars.uiRatingBar
        screenreadersRatingBar = binding!!.ratingBars.screenreadersRatingBar
        labelsRatingBar = binding!!.ratingBars.labelsRatingBar
        functionsRatingBar = binding!!.ratingBars.functionsRatingBar
        performanceRatingBar = binding!!.ratingBars.performanceRatingBar
        commentText = binding!!.commentText
        ratingContinue = binding!!.ratingContinue
        setupUi()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeAuth()
        subscribeUser()
        subscribeUserRating()
        subscribeCreation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onSubscribeUser() {
        getUser()?.also { user ->
            if (isUpdate.not()) {
                ratingCreatorFragmentArgs.rating?.also { rating ->
                    rating.user?.id?.takeIf { it == user.id }?.also {
                        ratingCreatorViewModel.setUserRating(rating)
                    } ?: findNavController().popBackStack()
                } ?: ratingCreatorViewModel.getUserRating(
                    RatingInput(
                        appId = ratingCreatorFragmentArgs.app.id,
                        userId = user.id,
                        pageSize = 1
                    )
                )
            }
        } ?: run {
            findNavController().navigate(
                RatingCreatorFragmentDirections.actionFromCommentCreatorToRequiresAuth()
            )
        }
    }

    private fun subscribeAuth() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(AUTH_CANCELED_ON_DIALOG)?.observe(this, Observer {
        if (it.not()) {
            loginScreen.launch(Unit)
        } else {
            findNavController().popBackStack()
        }
    })

    private fun subscribeUserRating() = ratingCreatorViewModel.rating.observe(this, Observer {
        it?.also { rating ->
            isUpdate = true
            currentRatingId = rating.id
            uiRatingBar.setValueWithAccessibilitySupport(rating.ui.toFloat())
            screenreadersRatingBar.setValueWithAccessibilitySupport(rating.screenreaders.toFloat())
            labelsRatingBar.setValueWithAccessibilitySupport(rating.labels.toFloat())
            functionsRatingBar.setValueWithAccessibilitySupport(rating.functions.toFloat())
            performanceRatingBar.setValueWithAccessibilitySupport(rating.performance.toFloat())
            commentText.text = Editable.Factory.getInstance().newEditable(rating.comment ?: "")
        }
    })

    private fun subscribeCreation() = ratingCreatorViewModel.isCreated.observe(this, Observer {
        when (val response = it) {
            is RatingCreatorViewModel.RatingCreatorViewState.Success -> {
                Snackbar.make(
                    ratingContinue,
                    getString(R.string.ratingcreator__success),
                    Snackbar.LENGTH_SHORT
                ).show()
                isReadyToContinue = true
                ratingContinue.apply {
                    text = getString(R.string.ratingcreator__finish)
                    setOnClickListener {
                        findNavController().popBackStack()
                    }
                }
            }
            is RatingCreatorViewModel.RatingCreatorViewState.Error -> {
                uiRatingBar.isEnabled = true
                screenreadersRatingBar.isEnabled = true
                labelsRatingBar.isEnabled = true
                functionsRatingBar.isEnabled = true
                performanceRatingBar.isEnabled = true
                commentText.isEnabled = true
                isReadyToContinue = true
                ratingContinue.text = getString(R.string.ratingcreator__retry)
                Snackbar.make(
                    ratingContinue,
                    getString(R.string.ratingcreator__error, response.errorMessage),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    })

    private fun setupUi(){
        totalRating.visibility = View.GONE
        totalRatingBar.visibility = View.GONE
        ratingContinue.isEnabled = false
        uiRatingBar.apply {
            setRatingBar(this)
        }
        screenreadersRatingBar.apply {
            setRatingBar(this)
        }
        labelsRatingBar.apply {
            setRatingBar(this)
        }
        functionsRatingBar.apply {
            setRatingBar(this)
        }
        performanceRatingBar.apply {
            setRatingBar(this)
        }
        ratingContinue.setOnClickListener {
            createRating()
        }
    }

    private fun setRatingBar(ratingBar: RatingBar) {
        ratingBar.apply {
            setIsIndicator(false)
            setOnRatingBarChangeListener { _, rating, _ ->
                onRatingBarChangeListener(ratingBar, rating)
            }
        }
    }

    private fun onRatingBarChangeListener(ratingBar: RatingBar, stars: Float) {
        ratingBar.setValueWithAccessibilitySupport(stars)
        verifyRequirements()
    }

    private fun verifyRequirements() {
        if (
            uiRatingBar.rating >= 1F &&
                    screenreadersRatingBar.rating >= 1F &&
                    labelsRatingBar.rating >= 1F &&
                    functionsRatingBar.rating >= 1F &&
                    performanceRatingBar.rating >= 1F
                ) {
            isReadyToContinue = true
        } else {
            isReadyToContinue = false
        }
    }

    private fun createRating() {
        uiRatingBar.isEnabled = false
        screenreadersRatingBar.isEnabled = false
        labelsRatingBar.isEnabled = false
        functionsRatingBar.isEnabled = false
        performanceRatingBar.isEnabled = false
        commentText.isEnabled = false
        isReadyToContinue = false
        ratingContinue.text = getString(R.string.viewstate__loading_title)
        ratingCreatorViewModel.getTargetApp()?.also { app ->
            ratingCreatorViewModel.createOrUpdateRating(
                Rating(
                    id = currentRatingId,
                    app = app,
                    ui = uiRatingBar.rating.toInt(),
                    screenreaders = screenreadersRatingBar.rating.toInt(),
                    labels = labelsRatingBar.rating.toInt(),
                    functions = functionsRatingBar.rating.toInt(),
                    performance = performanceRatingBar.rating.toInt(),
                    comment = commentText.text?.toString().takeUnless { it?.isNullOrEmptyOrBlank() ?: true },
                    commentLanguage = Locale.getDefault().language
                ),
                isUpdate
            )
        }
    }
}
