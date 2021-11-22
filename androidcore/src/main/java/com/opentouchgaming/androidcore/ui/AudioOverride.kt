package com.opentouchgaming.androidcore.ui

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.view.View
import android.widget.*

import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.AppSettings
import com.opentouchgaming.androidcore.R

class AudioOverride internal constructor(internal var settingPrefix: String) {

    internal val freqList = arrayOf(48000, 44100, 22050, 11025)

    internal val samplesList = arrayOf(512, 1024, 1536, 2048, 2560, 3072, 3584, 4096, 5120, 6144, 7168, 8192)


    internal var override: Boolean = false
    internal var backend: Int = -1
    internal var freq: Int = 0
    internal var samples: Int = 0

    fun linkUI(activity: Activity, dialog: Dialog) {

        loadSettings()

        val overrideCheckbox = dialog.findViewById<CheckBox>(R.id.override_checkBox)
        val overrideLayout =  dialog.findViewById<LinearLayout>(R.id.override_layout)

        if(override)  overrideLayout.visibility = View.VISIBLE else overrideLayout.visibility = View.GONE

        val paths: Array<String>
        paths = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arrayOf("Default","OpenSL", "Audio Tack (Old)", "AAudio (low latency)")
        } else {
            arrayOf("Default","OpenSL", "Audio Tack (Old)")
        }
        val spinner = dialog.findViewById<Spinner>(com.opentouchgaming.androidcore.R.id.audio_spinner)
        val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, paths)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(adapter)
        spinner.setSelection(backend)

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                backend = position
                saveSettings()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        val freqSpinner = dialog.findViewById<Spinner>(R.id.freq_spinner)
        val adapterFreq = ArrayAdapter(activity, android.R.layout.simple_spinner_item, freqList)
        freqSpinner.adapter = adapterFreq
        freqSpinner.setSelection(freq)
        freqSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                freq = position
                saveSettings();
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val samplesSpinner = dialog.findViewById<Spinner>(R.id.samples_spinner)
        val adapterSamples = ArrayAdapter(activity, android.R.layout.simple_spinner_item, samplesList)
        samplesSpinner.adapter = adapterSamples;
        samplesSpinner.setSelection(samples)

        samplesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                samples = position;
                saveSettings();
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        overrideCheckbox.isChecked = override

        overrideCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            override = isChecked
            if(override) overrideLayout.visibility = View.VISIBLE else overrideLayout.visibility = View.GONE
            saveSettings()
        }
    }

    private fun loadSettings() {
        override = AppSettings.getBoolOption(AppInfo.getContext(), settingPrefix + "audio_override", false)
        freq = AppSettings.getIntOption(AppInfo.getContext(), settingPrefix + "audio_freq", 0) // default 48000
        samples = AppSettings.getIntOption(AppInfo.getContext(), settingPrefix + "audio_samples", 3) // default 2048
        backend = AppSettings.getIntOption(AppInfo.getContext(), settingPrefix + "audio_backend", 0)
    }

    private fun saveSettings() {
        AppSettings.setBoolOption(AppInfo.getContext(), settingPrefix + "audio_override", override)
        AppSettings.setIntOption(AppInfo.getContext(), settingPrefix + "audio_freq", freq)
        AppSettings.setIntOption(AppInfo.getContext(), settingPrefix + "audio_samples", samples)
        AppSettings.setIntOption(AppInfo.getContext(), settingPrefix + "audio_backend", backend)
    }

    fun getFreq(): Int {
        loadSettings()
        if( override )
            return freqList[freq]
        else
            return 0;
    }

    fun getSamples(): Int {
        loadSettings()
        if( override )
            return samplesList[samples]
        else
            return 0
    }

    fun getBackend(): Int {
        return backend - 1; // Shift down so default returns -1
    }
}
