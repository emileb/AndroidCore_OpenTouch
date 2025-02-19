package com.opentouchgaming.androidcore.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.AppSettings
import com.opentouchgaming.androidcore.R
import com.opentouchgaming.androidcore.databinding.DialogMainOptionsNewBinding
import com.opentouchgaming.androidcore.ui.widgets.SpinnerWidget
import com.opentouchgaming.androidcore.ui.widgets.SwitchWidget

class OptionsDialogKt(
    activity: Activity, private val extraOptions: View?, private val update: Runnable
)
{
    var binding: DialogMainOptionsNewBinding = DialogMainOptionsNewBinding.inflate(activity.layoutInflater)

    companion object
    {
        const val SYSTEM_RESOLUTION_OVERRIDE = "system_resolution_override"
        const val HIDE_NAV_BAR = "hide_nav_bar"
        const val EXPAND_INTO_NOTCH = "expand_into_notch"
        const val HIDE_TOUCH_GFX = "hide_touch_graphics"
        const val USE_SYSTEM_KEYBOARD = "use_system_keyboard"
        const val USE_ALT_TOUCH_CODE = "use_alt_touch_code"
        const val GROUP_SIMILAR_ENGINES = "group_similar_engines"
        const val SDL_AUDIO_BACKEND = "sdl_audio_backend"

        val resolutions = arrayListOf(
            Pair("100%", 1.0f), Pair("75%", 0.75f), Pair("60%", 0.6f), Pair("50%", 0.5f), Pair("30%", 0.30f), Pair("25%", 0.25f)
        )

        fun GetResolutionScale(ctx: Context): Float
        {
            var idx = SpinnerWidget.fetchValue(ctx, SYSTEM_RESOLUTION_OVERRIDE, 0);
            return if (idx < resolutions.size) resolutions[idx].second;
            else 1.0f
        }
    }

    private var dontUpdate = false

    init
    {

        val dialog = Dialog(activity)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.setOnDismissListener {
            if (!dontUpdate) update.run()
        }

        val items: Array<Pair<String, View?>> = resolutions.map { Pair(it.first, null) }.toTypedArray()
        // Create and Setup the spinner options
        SpinnerWidget(
            activity,
            binding.resolutionOverride.root,
            "Display resolution",
            "Reduce the screen resolution for all engines.\nIt is recommended to use engine specific options instead of this.",
            items,
            SYSTEM_RESOLUTION_OVERRIDE,
            0,
            R.drawable.setting_resolution
        )

        SwitchWidget(
            activity,
            binding.hideNavButtons.root,
            "Full screen",
            "Go full screen by hiding the system navigation buttons",
            HIDE_NAV_BAR,
            false,
            R.drawable.settings_hide_nav
        )

        SwitchWidget(
            activity,
            binding.expandToNotch.root,
            "Expand in to camera notch",
            "Expand the screen in to the camera notch",
            EXPAND_INTO_NOTCH,
            false,
            R.drawable.setting_expand_notch
        )

        SwitchWidget(
            activity,
            binding.hideTouchGraphics.root,
            "Hide on-screen controls",
            "Hide all the touch screen graphics on menu and in-game",
            HIDE_TOUCH_GFX,
            false,
            R.drawable.setting_hide_touch_graphics
        )

        SwitchWidget(
            activity,
            binding.useAndroidKeyboard.root,
            "Use system keyboard",
            "Disable built-in keyboard. This option may not work with some keyboards",
            USE_SYSTEM_KEYBOARD,
            false
        )

        SwitchWidget(
            activity, binding.altTouchCode.root, "Use alternative touch code", "May fix touch screen control issues", USE_ALT_TOUCH_CODE, false
        )

        SwitchWidget(
            activity,
            binding.groupSimilarEngines.root,
            "Group similar engines",
            "Group engines on the left panel, makes icons larger",
            GROUP_SIMILAR_ENGINES,
            AppInfo.groupSimilarEngines
        )

        val sdlAudioItems: Array<Pair<String, View?>>

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            sdlAudioItems = arrayOf(
                Pair("OpenSL (Default)", null), Pair("Audio Tack (Old)", null), Pair("AAudio (low latency)", null)
            )
        } else
        {
            sdlAudioItems = arrayOf(
                Pair("OpenSL (Default)", null), Pair("Audio Tack (Old)", null)
            )
        }

        SpinnerWidget(
            activity,
            binding.sdlAudioBackend.root,
            "SDL audio backend",
            "Audio backend SDL uses to play audio",
            sdlAudioItems,
            SDL_AUDIO_BACKEND,
            0,
            R.drawable.setting_audio
        )

        binding.resetButton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(activity)
            dialogBuilder.setTitle("WARNING: This will reset all app settings! (Game data and save games are not touched)")
            dialogBuilder.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
                dontUpdate = true
                AppSettings.deleteAllOptions(activity)
                AppInfo.currentEngine = null
                dialog.dismiss()
                System.exit(0) // Kill the process so everything is reloaded
            }
            dialogBuilder.create().show()
        }

        if (extraOptions != null)
        {
            val layout = dialog.findViewById<LinearLayout>(R.id.extras_linearlayout)
            binding.extrasLinearlayout.addView(extraOptions)
        }



        dialog.show()
    }


}