package com.opentouchgaming.androidcore.ui.widgets

import android.R
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.opentouchgaming.androidcore.AppSettings
import com.opentouchgaming.androidcore.databinding.ViewSpinnerOptionBinding

class SpinnerWidget internal constructor(
    val context: Context,
    view: View,
    val settingPrefix: String
) {
    private var binding = ViewSpinnerOptionBinding.bind(view)

    fun setup(title: String, items: Array<String>, default: Int) {

        binding.title.text = title

        val adapter = ArrayAdapter(context, R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

        // Save setting on change
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                AppSettings.setIntOption(context, settingPrefix + "_spinner", position)
            }
        }

        // Set to existing value if set
        binding.spinner.setSelection(
            fetchValue(context, settingPrefix, default)
        )
    }

    companion object {
        //This annotation tells Java classes to treat this method as if it was a static to [KotlinClass]
        @JvmStatic
        fun fetchValue(context: Context, settingPrefix: String, default: Int): Int {
            return AppSettings.getIntOption(context, settingPrefix + "_spinner", default)
        }
    }
}