package com.pbaltazar.blindo.ui.components.filters.elements

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ComponentCheckboxFilterBinding

class CheckboxFilter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Filter {

    companion object {
        @IdRes
        const val FILTER_TYPE: Int = R.id.filters_screen_checkbox_type
    }

    private lateinit var binding: ComponentCheckboxFilterBinding

    private lateinit var checkBox: CheckBox

    var text: String?
    get() = checkBox.text?.toString()
    set(value) {
        checkBox.text = value
    }
    var isChecked: Boolean
    get() = checkBox.isChecked
    set(value) {
        checkBox.isChecked = value
    }

    init {
        binding = ComponentCheckboxFilterBinding.inflate(LayoutInflater.from(context), this)

        checkBox = binding.checkbox

        attrs?.also {
            val a = context.obtainStyledAttributes(it, R.styleable.FiltersCheckbox)

            text = a.getString(R.styleable.FiltersCheckbox_android_text)
            isChecked = a.getBoolean(R.styleable.FiltersCheckbox_android_checked, false)

            a.recycle()
        }
    }
}
