package com.pbaltazar.blindo.ui.rating.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blindo.apollito.utils.extensions.toTimeAgo
import com.bumptech.glide.Glide
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentRatingDetailsBinding
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.pbaltazar.blindo.utils.extensions.setExplainingTooltip
import com.pbaltazar.blindo.utils.extensions.setValueWithAccessibilitySupport
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class RatingDetailsFragment : BlindoFragment<FragmentRatingDetailsBinding>() {

    private val ratingDetailsViewModel: RatingDetailsViewModel by viewModel()
    private val ratingDetailsFragmentArgs: RatingDetailsFragmentArgs by navArgs()

    private lateinit var userPhoto: ImageView
    private lateinit var authorInfo: TextView
    private lateinit var commentInfo: TextView
    private lateinit var totalRating: TextView
    private lateinit var totalRatingBar: RatingBar
    private lateinit var uiRating: TextView
    private lateinit var uiRatingBar: RatingBar
    private lateinit var screenreadersRating: TextView
    private lateinit var screenreadersRatingBar: RatingBar
    private lateinit var labelsRating: TextView
    private lateinit var labelsRatingBar: RatingBar
    private lateinit var functionsRating: TextView
    private lateinit var functionsRatingBar: RatingBar
    private lateinit var performanceRating: TextView
    private lateinit var performanceRatingBar: RatingBar
    private lateinit var commentText: TextView

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ratingDetailsViewModel.setTargetRating(ratingDetailsFragmentArgs.rating)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRatingDetailsBinding.inflate(inflater, container, false)
        userPhoto = binding!!.userPhoto
        authorInfo = binding!!.authorInfo
        commentInfo = binding!!.commentInfo
        totalRating = binding!!.commentRatingBars.totalRating
        totalRatingBar = binding!!.commentRatingBars.totalRatingBar
        uiRating = binding!!.commentRatingBars.uiRating
        uiRatingBar = binding!!.commentRatingBars.uiRatingBar
        screenreadersRating = binding!!.commentRatingBars.screenreadersRating
        screenreadersRatingBar = binding!!.commentRatingBars.screenreadersRatingBar
        labelsRating = binding!!.commentRatingBars.labelsRating
        labelsRatingBar = binding!!.commentRatingBars.labelsRatingBar
        functionsRating = binding!!.commentRatingBars.functionsRating
        functionsRatingBar = binding!!.commentRatingBars.functionsRatingBar
        performanceRating = binding!!.commentRatingBars.performanceRating
        performanceRatingBar = binding!!.commentRatingBars.performanceRatingBar
        commentText = binding!!.commentText
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        totalRating.setExplainingTooltip(R.string.ratingbars__total_description)
        uiRating.setExplainingTooltip(R.string.ratingbars__ui_description)
        screenreadersRating.setExplainingTooltip(R.string.ratingbars__screenreaders_description)
        labelsRating.setExplainingTooltip(R.string.ratingbars__labels_description)
        functionsRating.setExplainingTooltip(R.string.ratingbars__functions_description)
        performanceRating.setExplainingTooltip(R.string.ratingbars__performance_description)
        ratingDetailsViewModel.getTargetRating()?.also { rating ->
            Glide.with(requireContext())
                .load(rating.user?.picture)
                .placeholder(R.mipmap.default_user_picture)
                .centerCrop()
                .into(userPhoto)
            authorInfo.apply {
                text = rating.user?.name ?: getString(R.string.appcomment_unknown_author)
                rating.user?.also { user ->
                    setOnClickListener {
                        this@RatingDetailsFragment.findNavController().navigate(
                            RatingDetailsFragmentDirections.actionFromCommentDetailsToPublicUserProfile(user)
                        )
                    }
                }
            }
            commentInfo.text = getString(
        R.string.commentdetails__comment_info,
                rating.commentLanguage?.let { language ->
                    Locale.Builder().setLanguage(language).build().displayLanguage
                } ?: getString(R.string.commentdetails__unknown_language),
                rating.updatedAt?.toTimeAgo() ?: rating.createdAt.toTimeAgo()
            )
            totalRatingBar.apply {
                setValueWithAccessibilitySupport(rating.total ?: 0F)
                setExplainingTooltip(R.string.ratingbars__total_description)
            }
            uiRatingBar.apply {
                setValueWithAccessibilitySupport(rating.ui.toFloat())
                setExplainingTooltip(R.string.ratingbars__ui_description)
            }
            screenreadersRatingBar.apply {
                setValueWithAccessibilitySupport(rating.screenreaders.toFloat())
                setExplainingTooltip(R.string.ratingbars__screenreaders_description)
            }
            labelsRatingBar.apply {
                setValueWithAccessibilitySupport(rating.labels.toFloat())
                setExplainingTooltip(R.string.ratingbars__labels_description)
            }
            functionsRatingBar.apply {
                setValueWithAccessibilitySupport(rating.functions.toFloat())
                setExplainingTooltip(R.string.ratingbars__functions_description)
            }
            performanceRatingBar.apply {
                setValueWithAccessibilitySupport(rating.performance.toFloat())
                setExplainingTooltip(R.string.ratingbars__performance_description)
            }
            commentText.text = rating.comment ?: getString(R.string.appcomment__no_comment)
        }
    }
}
