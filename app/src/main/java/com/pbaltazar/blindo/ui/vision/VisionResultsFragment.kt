package com.pbaltazar.blindo.ui.vision

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentVisionResultsBinding
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.pbaltazar.blindo.utils.vision.BlindoVisionBridge
import com.wizeline.viewstate.State
import com.wizeline.viewstate.ViewState
import org.koin.androidx.viewmodel.ext.android.viewModel

class VisionResultsFragment : BlindoFragment<FragmentVisionResultsBinding>() {

    private val visionResultsViewModel: VisionResultsViewModel by viewModel()

    private lateinit var resultsState: ViewState
    private lateinit var imagePreview: ImageView
    private lateinit var description: TextView
    private lateinit var confidence: TextView
    private lateinit var text: TextView
    private lateinit var tags: TextView
    private lateinit var coinsLeft: TextView

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BlindoVisionBridge.listener.stopScreenshotWatcher()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVisionResultsBinding.inflate(inflater, container, false)
        resultsState = binding!!.resultsState
        imagePreview = binding!!.imagePreview
        description = binding!!.imageDescription
        confidence = binding!!.imageDescriptionConfidence
        text = binding!!.imageText
        tags = binding!!.imageTags
        coinsLeft = binding!!.coinsLeft
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
        resultsState.setState(State.LOADING)
        subscribeImageAnalisis()
        visionResultsViewModel.analizeImage()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            requireActivity().finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun subscribeImageAnalisis() = visionResultsViewModel.imageAnalisis.observe(viewLifecycleOwner) {
        when (val response = it) {
            is VisionResultsViewModel.ImageAnalisis.Success -> {
                imagePreview.setImageBitmap(response.image)
                description.text = getString(
                    R.string.vision__description,
                    response.imageDescription.description
                )
                confidence.text = getString(
                    R.string.vision__confidence,
                    response.imageDescription.percentageOfConfidence
                )
                text.text = getString(
                    R.string.vision__text,
                    response.imageDescription.imageText
                )
                tags.text = getString(
                    R.string.vision__tags,
                    response.imageDescription.descriptionTags.joinToString(", ")
                )
                coinsLeft.text = resources.getQuantityString(
                    R.plurals.coins__current_coins,
                    response.imageDescription.left,
                    response.imageDescription.left
                )
                resultsState.setState(State.CONTENT)
                description.apply {
                    requestFocus()
                    sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }
            }
            is VisionResultsViewModel.ImageAnalisis.Empty -> {
                resultsState.setState(State.EMPTY)
            }
            is VisionResultsViewModel.ImageAnalisis.Error -> {
                resultsState.setErrorDescriptionText(
                    response.reason
                )
                resultsState.setState(State.ERROR)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        BlindoVisionBridge.listener.startScreenshotWatcher()
    }
}
