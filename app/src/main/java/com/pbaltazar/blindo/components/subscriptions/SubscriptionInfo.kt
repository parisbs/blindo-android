package com.pbaltazar.blindo.components.subscriptions

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ComponentSubscriptionInfoBinding
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.purchases.subscriptions.Subscription
import com.pbaltazar.blindo.entities.purchases.subscriptions.SubscriptionOffer
import com.pbaltazar.blindo.entities.purchases.subscriptions.SubscriptionPricingPhase
import java.time.Period

class SubscriptionInfo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ComponentSubscriptionInfoBinding

    private val title: TextView
    private val benefits: ListView
    private val offersContainer: ChipGroup

    private var subscription: Subscription? = null
    private var onOfferSelectedListener: OnOfferSelectedListener? = null
    private val offers: MutableList<SubscriptionOffer> = mutableListOf()
    private val chips: MutableList<Chip> = mutableListOf()

    init {
        binding = ComponentSubscriptionInfoBinding.inflate(LayoutInflater.from(context), this)
        title = binding.title
        benefits = binding.benefits
        offersContainer = binding.offersContainer

        setTitleFunctions()
        offersContainer.setOnCheckedChangeListener { _, checkedId ->
            onOfferSelectedListener?.onOfferSelected(this, getSelectedOffer())
        }
    }

    fun setOnOfferSelectedListener(onOfferSelectedListener: OnOfferSelectedListener?) {
        this.onOfferSelectedListener = onOfferSelectedListener
    }

    fun getSelectedOffer(): SubscriptionOffer? = offers.getOrNull(offersContainer.checkedChipId - 1)

    fun clearSelection() = offersContainer.clearCheck()

    fun getSelectedOfferFormatedPhases(): String =
        chips.getOrNull(offersContainer.checkedChipId - 1)?.text?.toString() ?: ""

    fun getSubscription(): Subscription? = subscription

    fun setSubscription(subscription: Subscription) {
        this.subscription = subscription
        if (offersContainer.childCount > 0) {
            offersContainer.removeAllViews()
            offers.clear()
            chips.clear()
        }
        title.text = subscription.name
        benefits.contentDescription = subscription.name
        resources.getStringArray(subscription.benefitsResourceId)?.also { subscriptionBenefits ->
            ArrayAdapter<String>(context, R.layout.item_text, subscriptionBenefits.toList()).apply {
                benefits.adapter = this
            }
        }
        offersContainer.contentDescription = subscription.name
        subscription.getSubscriptionOffers()?.forEach { subscriptionOffer ->
            Chip(context).apply {
                id = ViewCompat.generateViewId()
                layoutParams = ChipGroup.LayoutParams(ChipGroup.LayoutParams.MATCH_PARENT, ChipGroup.LayoutParams.WRAP_CONTENT)
                isCheckable = true
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                text = subscriptionOffer.getSubscriptionPricingPhases()?.toFormatedPhases()
                offersContainer.addView(this)
                offers.add(subscriptionOffer)
                chips.add(this)
            }
        }
    }

    fun List<SubscriptionPricingPhase>.toFormatedPhases(): String {
        if (size < 1) {
            return ""
        }
        val formatedPhases = StringBuilder()
        for (i in 0..(size - 1)) {
            val phase = get(i)
            val period = Period.parse(phase.billingPeriod)
            if (size == 1) {
                formatedPhases.append(
                    context.getString(
                        R.string.membership__unique_phase,
                        phase.getFormatedPriceWithCurrencyCode(),
                        period.getFormatedPeriod()
                    )
                )
                break
            } else {
                when (i) {
                    1 -> formatedPhases.append(
                        context.getString(
                            R.string.membership__first_phase,
                            period.getFormatedPeriod(),
                            phase.getFormatedPriceWithCurrencyCode(),
                            period.getPluralsThe(period.getMaxExactAmmount()),
                            period.getPluralsFollowing(period.getMaxExactAmmount())
                        )
                    )
                    2 -> formatedPhases.append(
                        context.getString(
                            R.string.membership__second_phase,
                            period.getFormatedPeriod(),
                            phase.getFormatedPriceWithCurrencyCode(),
                            period.getPluralsThe(period.getMaxExactAmmount()),
                            period.getPluralsFollowing(period.getMaxExactAmmount())
                        )
                    )
                    3 -> formatedPhases.append(
                        context.getString(
                            R.string.membership__third_phase,
                            period.getFormatedPeriod(),
                            phase.getFormatedPriceWithCurrencyCode(),
                            period.getPluralsThe(period.getMaxExactAmmount()),
                            period.getPluralsFollowing(period.getMaxExactAmmount())
                        )
                    )
                }
                formatedPhases.append(", ")
            }
        }
        return formatedPhases.toString()
    }

    fun Period.weeks(): Int = ((days / 7) as Float).toInt()

    fun Period.hasExactWeeksButNotMonths(): Boolean =
        if ((days % 7) == 0 && months < 1) true else false

    fun Period.getMaxExactAmmount(): Int = when {
        years > 0 -> years
        months > 0 -> months
        weeks() > 0 -> weeks()
        else -> days
    }

    fun Period.getPluralsThe(quantity: Int): String =
        if (hasExactWeeksButNotMonths()) {
            resources.getQuantityString(R.plurals.membership__female_the, quantity)
        } else {
            resources.getQuantityString(R.plurals.membership__male_the, quantity)
        }

    fun Period.getPluralsFollowing(quantity: Int): String =
        resources.getQuantityString(R.plurals.membership__following, quantity)

    fun Period.getFormatedPeriod(): String {
        val formatedPeriod = StringBuilder()
        if (hasExactWeeksButNotMonths()) {
            if (weeks() > 1) {
                formatedPeriod.append("${weeks()} ")
            }
            formatedPeriod.append(
                resources.getQuantityString(R.plurals.membership__period_week, weeks())
            )
            return formatedPeriod.toString()
        } else if (days > 0) {
            if (days > 1) {
                formatedPeriod.append("$days ")
            }
            formatedPeriod.append(
                resources.getQuantityString(R.plurals.membership__period_day, days)
            )
            return formatedPeriod.toString()
        } else if (months > 0) {
            if (months > 1) {
                formatedPeriod.append("$months ")
            }
            formatedPeriod.append(
                resources.getQuantityString(R.plurals.membership__period_month, months)
            )
            return formatedPeriod.toString()
        } else if (years > 0) {
            if (years > 1) {
                formatedPeriod.append("$years ")
            }
            formatedPeriod.append(
                resources.getQuantityString(R.plurals.membership__period_year, years)
            )
            return formatedPeriod.toString()
        } else {
            return this.toString()
        }
    }

    private fun setTitleFunctions() {
        ViewCompat.setAccessibilityHeading(title, true)
    }

    interface OnOfferSelectedListener {
        fun onOfferSelected(subscriptionInfo: SubscriptionInfo, selectedSubscriptionOffer: SubscriptionOffer?)
    }
}
