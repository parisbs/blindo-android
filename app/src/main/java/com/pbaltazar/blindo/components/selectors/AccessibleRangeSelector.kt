package com.pbaltazar.blindo.components.selectors

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ComponentAccessibleRangeSelectorBinding
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.utils.extensions.toRatingString
import java.util.*

class AccessibleRangeSelector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ComponentAccessibleRangeSelectorBinding

    private val beginLabel: TextView
    private lateinit var beginMinusButton: ImageButton
    private val beginValue: TextView
    private lateinit var beginPlusButton: ImageButton
    private val endLabel: TextView
    private lateinit var endMinusButton: ImageButton
    private val endValue: TextView
    private lateinit var endPlusButton: ImageButton

    var description: String = ""
    set(value) {
        field = value
        setLabelsAndContentDescriptions()
    }
    var valueFrom: Float = 0F
    var valueTo: Float = 0F
    var stepSize: Float = 0F

    private val buttonsListener: OnClickListener = object : OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.beginMinusButton -> {
                    val newValue = currentBeginValue.minus(stepSize).toOneDecimalFloat()
                    if (newValue < valueFrom) {
                        beginMinusButton.isEnabled = false
                    } else {
                        if (newValue == valueFrom) {
                            beginMinusButton.isEnabled = false
                        }
                        currentBeginValue = newValue
                        regreshBeginValue()
                    }
                }
                R.id.beginPlusButton -> {
                    val newValue = currentBeginValue.plus(stepSize).toOneDecimalFloat()
                    if (newValue >= valueTo) {
                        beginPlusButton.isEnabled = false
                    } else if (newValue >= currentEndValue) {
                        beginPlusButton.isEnabled = false
                    } else {
                        if (newValue == currentEndValue.minus(stepSize).toOneDecimalFloat()) {
                            beginPlusButton.isEnabled = false
                            endMinusButton.isEnabled = false
                        }
                        currentBeginValue = newValue
                        regreshBeginValue()
                    }
                }
                R.id.endMinusButton -> {
                    val newValue = currentEndValue.minus(stepSize).toOneDecimalFloat()
                    if (newValue <= valueFrom) {
                        endMinusButton.isEnabled = false
                    } else if (newValue <= currentBeginValue) {
                        endMinusButton.isEnabled = false
                    } else {
                        if (newValue == currentBeginValue.plus(stepSize).toOneDecimalFloat()) {
                            endMinusButton.isEnabled = false
                            beginPlusButton.isEnabled = false
                        }
                        currentEndValue = newValue
                        refreshEndValue()
                    }
                }
                R.id.endPlusButton -> {
                    val newValue = currentEndValue.plus(stepSize).toOneDecimalFloat()
                    if (newValue > valueTo) {
                        endPlusButton.isEnabled = false
                    } else {
                        if (newValue == valueTo) {
                            endPlusButton.isEnabled = false
                        }
                        currentEndValue = newValue
                        refreshEndValue()
                    }
                }
            }
            refreshButtonsState()
        }
    }

    private var currentBeginValue: Float = 0F
    private var currentEndValue: Float = 0F

    init {
        binding = ComponentAccessibleRangeSelectorBinding.inflate(LayoutInflater.from(context), this)

        beginLabel = binding.beginLabel
        beginMinusButton = binding.beginMinusButton
        beginValue = binding.beginValue
        beginPlusButton = binding.beginPlusButton
        endLabel = binding.endLabel
        endMinusButton = binding.endMinusButton
        endValue = binding.endValue
        endPlusButton = binding.endPlusButton

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.AccessibleRangeSelector)

            description = a.getString(R.styleable.AccessibleRangeSelector_selectorDescription) ?: ""
            valueTo = a.getFloat(R.styleable.AccessibleRangeSelector_android_valueTo, 0F)
            valueFrom = a.getFloat(R.styleable.AccessibleRangeSelector_android_valueFrom, 0F).let { vf ->
                if (vf > valueTo) {
                    throw IllegalArgumentException("Attribute android:valueFrom cannot be higher than android:valueTo")
                }
                vf
            }
            stepSize = a.getFloat(R.styleable.AccessibleRangeSelector_android_stepSize, 1F).let { ss ->
                if (ss <= 0) {
                    throw IllegalArgumentException("Attribute android:stepSize cannot be lower than or equals to 0")
                }
                ss
            }

            a.recycle()
        }

        setLabelsAndContentDescriptions()
        setClickListeners()
        setInitialValues()
        setupAccessibility()
    }

    private fun setupAccessibility() {
        ViewCompat.setAccessibilityLiveRegion(beginValue, ViewCompat.ACCESSIBILITY_LIVE_REGION_ASSERTIVE)
        ViewCompat.setAccessibilityLiveRegion(endValue, ViewCompat.ACCESSIBILITY_LIVE_REGION_ASSERTIVE)
    }

    private fun setLabelsAndContentDescriptions() {
        context.apply {
            beginLabel.text = getString(R.string.components__accessible_range_selector__begin_label, description).trim()
            beginMinusButton.contentDescription = getString(
                R.string.components__accessible_range_selector__minus_buttons,
                description,
                getString(R.string.components__accessible_range_selector__begin)
            ).trim()
            beginPlusButton.contentDescription = getString(
                R.string.components__accessible_range_selector__plus_buttons,
                description,
                getString(R.string.components__accessible_range_selector__begin)
            ).trim()
            endLabel.text = getString(R.string.components__accessible_range_selector__end_label, description).trim()
            endMinusButton.contentDescription = getString(
                R.string.components__accessible_range_selector__minus_buttons,
                description,
                getString(R.string.components__accessible_range_selector__end)
            ).trim()
            endPlusButton.contentDescription = getString(
                R.string.components__accessible_range_selector__plus_buttons,
                description,
                getString(R.string.components__accessible_range_selector__end)
            ).trim()
        }
    }

    private fun setInitialValues() {
        currentBeginValue = valueFrom
        currentEndValue = valueTo
        beginMinusButton.isEnabled = false
        endPlusButton.isEnabled = false
        regreshBeginValue()
        refreshEndValue()
    }

    private fun setClickListeners() {
        beginMinusButton.setOnClickListener(buttonsListener)
        beginPlusButton.setOnClickListener(buttonsListener)
        endMinusButton.setOnClickListener(buttonsListener)
        endPlusButton.setOnClickListener(buttonsListener)
    }

    private fun regreshBeginValue() {
        beginValue.apply {
            text = currentBeginValue.toRatingString()
            contentDescription = "${beginLabel.text}: ${currentBeginValue.toRatingString()}"
        }
    }

    private fun refreshEndValue() {
        endValue.apply {
            text = currentEndValue.toRatingString()
            contentDescription = "${endLabel.text}: ${currentEndValue.toRatingString()}"
        }
    }

    fun setBeginValue(value: Float) {
        if (value < valueFrom) {
            throw IllegalArgumentException("Begin value cannot be lower than ${valueFrom.toString()}")
        } else if (value > valueTo) {
            throw IllegalArgumentException("Begin value cannot be higher than ${valueTo.toString()}")
        } else if (value > currentEndValue) {
            throw IllegalArgumentException("Begin value cannot be higher than the current end value: ${currentEndValue.toString()}")
        } else {
            currentBeginValue = value.toOneDecimalFloat()
            regreshBeginValue()
        }
    }

    fun getBeginValue(): Float = currentBeginValue.toOneDecimalFloat()

    fun setEndValue(value: Float) {
        if (value < valueFrom) {
            throw IllegalArgumentException("End value cannot be lower than ${valueFrom.toString()}")
        } else if (value > valueTo) {
            throw IllegalArgumentException("End value cannot be higher than ${valueTo.toString()}")
        } else if (value < currentBeginValue) {
            throw IllegalArgumentException("End value cannot be lower than the current begin value: ${currentBeginValue.toString()}")
        } else {
            currentEndValue = value.toOneDecimalFloat()
            refreshEndValue()
        }
    }

    fun getEndValue(): Float = currentEndValue.toOneDecimalFloat()

    fun setRange(floatRange: FloatRange) {
        currentBeginValue = valueFrom
        currentEndValue = valueTo
        setBeginValue(floatRange.begin)
        setEndValue(floatRange.end)
        refreshButtonsState()
    }

    fun getRange(): FloatRange = FloatRange(
        begin = getBeginValue(),
    end = getEndValue()
    )

    private fun refreshButtonsState() {
        if (currentBeginValue > valueFrom) {
            beginMinusButton.isEnabled = true
        }
        if (currentBeginValue < valueTo && currentBeginValue < currentEndValue.minus(stepSize).toOneDecimalFloat()) {
            beginPlusButton.isEnabled = true
        }
        if (currentEndValue > valueFrom && currentEndValue > currentBeginValue.plus(stepSize).toOneDecimalFloat()) {
            endMinusButton.isEnabled = true
        }
        if (currentEndValue < valueTo) {
            endPlusButton.isEnabled = true
        }
    }

    private fun Float.toOneDecimalFloat(): Float = toRatingString(Locale.ENGLISH        ).toFloat()
}
