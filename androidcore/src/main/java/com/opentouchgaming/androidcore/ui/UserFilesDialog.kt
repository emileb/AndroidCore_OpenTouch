package com.opentouchgaming.androidcore.ui

import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.R
import com.opentouchgaming.androidcore.databinding.DialogUserFilesManagerBinding

class UserFilesDialog
{
    lateinit var binding: DialogUserFilesManagerBinding

    fun showDialog(activity: Activity)
    {
        binding = DialogUserFilesManagerBinding.inflate(activity.layoutInflater)

        val dialog = Dialog(activity, R.style.DialogThemeFullscreen_dark)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)

        val userPath = AppInfo.getDisplayPathAndImage(AppInfo.getUserFiles())
        binding.userDirTextview.setText(userPath.first)
        binding.userDirImage.setImageResource(userPath.second!!)
        binding.userDirTextview
        dialog.show()
    }
}