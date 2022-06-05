package com.opentouchgaming.androidcore.ui.widgets

import android.R
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import com.opentouchgaming.androidcore.AppSettings
import com.opentouchgaming.androidcore.databinding.ViewSpinnerOptionBinding
import com.opentouchgaming.androidcore.databinding.ViewSwitchOptionBinding

class SwitchWidget internal constructor(
    val context: Context,
    view: View,
    private val title: String,
    private val settingPrefix: String,
    val default: Boolean
) {
    private var binding = ViewSwitchOptionBinding.bind(view)

    init {
        binding.title.text = title

        binding.switch1.isChecked = fetchValue(context, settingPrefix, default);

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            AppSettings.setBoolOption(context, settingPrefix, isChecked)
        }
    }

    fun setEnabled(enabled: Boolean) {
        binding.switch1.isEnabled = enabled
        binding.title.isEnabled = enabled
        binding.imageView.isEnabled = enabled
    }

    companion object {
        //This annotation tells Java classes to treat this method as if it was a static to [KotlinClass]
        @JvmStatic
        fun fetchValue(context: Context, settingPrefix: String, default: Boolean): Boolean {
            return AppSettings.getBoolOption(
                context,
                settingPrefix,
                default
            )
        }
    }
}