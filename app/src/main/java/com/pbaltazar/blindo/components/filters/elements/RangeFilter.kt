package com.pbaltazar.blindo.components.filters.elements

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.components.selectors.AccessibleRangeSelector
import com.pbaltazar.blindo.databinding.ComponentRangeFilterBinding
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange
import com.pbaltazar.blindo.utils.extensions.toFloatRange
import com.pbaltazar.blindo.utils.extensions.toIntRange

class RangeFilter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Filter {

    companion object {
        const val FILTER_TYPE: Int = R.id.filters_screen_range_type
    }

    private val binding: ComponentRangeFilterBinding

    private val header: TextView
    private val expandableContainer: LinearLayout
    private val rangeSelector: AccessibleRangeSelector

    var text: String = ""
    set(value) {
        field = value
        header.text = resources.getString(R.string.filters__range_title, field).trim()
        rangeSelector.description = field
    }

    private val expandAction: AccessibilityNodeInfoCompat.AccessibilityActionCompat =
        AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND
    private val collapseAction: AccessibilityNodeInfoCompat.AccessibilityActionCompat =
        AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE
    var isExpanded: Boolean = false
    set(value) {
        field = value
        expandableListener()
    }

    var valueFrom: Float = 0F
    set(value) {
        field = value
        rangeSelector.valueFrom = field
    }
    var valueTo: Float = 0F
    set(value) {
        field = value
        rangeSelector.valueTo = field
    }
    var stepSize: Float = 0F
    set(value) {
        field = value
        rangeSelector.stepSize = field
    }
    private val range: FloatRange get() = FloatRange(valueFrom, valueTo)

    init {
        binding = ComponentRangeFilterBinding.inflate(LayoutInflater.from(context), this)

        header = binding.rangeHeader
        expandableContainer = binding.rangeExpandableContainer
        rangeSelector = binding.rangeSelector

        attrs?.also {
            val  a = context.obtainStyledAttributes(it, R.styleable.FiltersRange)

            text = a.getString(R.styleable.FiltersRange_android_text) ?: ""
            valueFrom = a.getFloat(R.styleable.FiltersRange_android_valueFrom, 0F)
            valueTo = a.getFloat(R.styleable.FiltersRange_android_valueTo, 0F)
            stepSize = a.getFloat(R.styleable.FiltersRange_android_stepSize, 0F)
            isExpanded = a.getBoolean(R.styleable.FiltersRange_expanded, false)

            a.recycle()
        }

        setFloatRange(range)
        setupSelector()
    }

    fun getFloatRange(): FloatRange = rangeSelector.getRange()

    fun setFloatRange(range: FloatRange) = rangeSelector.setRange(range)

    fun getIntRange(): IntRange = rangeSelector.getRange().toIntRange()

    fun setIntRange(range: IntRange) = rangeSelector.setRange(range.toFloatRange())

    private fun setupSelector() {
        rangeSelector.description = text
        header.setOnClickListener { isExpanded = isExpanded.not() }
        setupAccessibility()
    }

    private fun setupAccessibility() {
        ViewCompat.setAccessibilityHeading(header, true)
        ViewCompat.setAccessibilityDelegate(header, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfoCompat?) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info?.apply {
                    isCheckable = true
                    if (isExpanded) {
                        removeAction(expandAction)
                        addAction(collapseAction)
                        isChecked = true
                    } else {
                        removeAction(collapseAction)
                        addAction(expandAction)
                        isChecked = false
                    }
                }
            }

            override fun performAccessibilityAction(host: View?, action: Int, args: Bundle?): Boolean {
                return when(action) {
                    expandAction.id -> {
                        isExpanded = true
                        true
                    }
                    collapseAction.id -> {
                        isExpanded = false
                        true
                    }
                    else -> super.performAccessibilityAction(host, action, args)
                }
            }
        })
    }

    private fun expandableListener() {
        if (isExpanded) {
            header.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_box_black_24dp, 0, 0, 0)
            expandableContainer.visibility = View.VISIBLE
        } else {
            header.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_box_outline_blank_black_24dp, 0, 0, 0)
            expandableContainer.visibility = View.GONE
        }
    }
}
