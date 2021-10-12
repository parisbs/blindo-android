package com.pbaltazar.blindo.ui.components.filters.elements

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ComponentOrderByElementFilterBinding
import com.pbaltazar.blindo.ui.components.filters.entities.orderby.OrderByDirection
import com.pbaltazar.blindo.ui.components.filters.entities.orderby.OrderBySelection

class OrderByElementFilter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Filter {

    companion object {
        const val FILTER_TYPE: Int = R.id.filters_screen_order_by_type
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(selection: OrderBySelection)
    }

    interface OnItemSelectedListener {
        fun onItemSelected(selection: OrderBySelection)
        fun onNothingSelected(elementFilter: OrderByElementFilter): Unit = Unit
    }

    private lateinit var binding: ComponentOrderByElementFilterBinding

    private lateinit var checkbox: CheckBox
    private lateinit var spinner: Spinner

    private val checkboxListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        spinner.isEnabled = isChecked
            customCheckedListener?.onCheckedChanged(
                OrderBySelection(
                    id,
                    isChecked,
                    OrderByDirection.fromSpinnerPosition(spinner.selectedItemPosition)
                )
            )
    }
    private val spinnerListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                customSelectionListener?.onItemSelected(
                    OrderBySelection(
                        this@OrderByElementFilter.id,
                        isChecked,
                        OrderByDirection.fromSpinnerPosition(position)
                    )
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                customSelectionListener?.onNothingSelected(this@OrderByElementFilter)
            }
        }

    private var customCheckedListener: OnCheckedChangeListener? = null
    private var customSelectionListener: OnItemSelectedListener? = null

    var text: String? = null
    set(value) {
        field = value
        if (positionBadge < 1) {
            checkbox.text = field
        } else {
            setPositionBadge(positionBadge)
        }
    }
    var isChecked: Boolean
    get() = checkbox.isChecked
    set(value) {
        checkbox.isChecked = value
        spinner.isEnabled = value
    }
    var direction: OrderByDirection
    get() = OrderByDirection.fromSpinnerPosition(spinner.selectedItemPosition)
    set(value) = spinner.setSelection(value.spinnerPosition)
    var selection: OrderBySelection
    get() = OrderBySelection(id, isChecked, direction)
    set(value) {
        if (id != value.elementId) {
            throw IllegalArgumentException("The elementId must be same.")
        }
        isChecked = value.isChecked
        direction = value.direction
    }

    private var positionBadge: Int = -1

    init {
        binding = ComponentOrderByElementFilterBinding.inflate(LayoutInflater.from(context), this)

        checkbox = binding.checkbox
        spinner = binding.spinner

        attrs?.also {
            val  a = context.obtainStyledAttributes(it, R.styleable.FiltersOrderByElement)

            text = a.getString(R.styleable.FiltersOrderByElement_android_text)
            isChecked = a.getBoolean(R.styleable.FiltersOrderByElement_android_checked, false)

            a.recycle()
        }

        setupAccessibility()
        checkbox.setOnCheckedChangeListener(checkboxListener)
        spinner.onItemSelectedListener = spinnerListener
    }

    fun setPositionBadge(position: Int) {
        if (positionBadge != position) {
            positionBadge = position
            val badgedText = "${text}, $positionBadge"
            checkbox.apply {
                text = badgedText
                contentDescription = badgedText
            }
        }
    }

    fun removeBadgePosition() {
        if (positionBadge >= 1) {
            positionBadge = -1
            checkbox.apply {
                this.text = this@OrderByElementFilter.text
                contentDescription = null
            }
        }
    }

    fun getPositionBadge(): Int = positionBadge

    fun setCustomCheckedListener(listener: OnCheckedChangeListener) {
        customCheckedListener = listener
    }

    fun setCustomSelectionListener(listener: OnItemSelectedListener) {
        customSelectionListener = listener
    }

    private fun setupAccessibility() {
        ViewCompat.setAccessibilityLiveRegion(checkbox, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE)
    }
}
