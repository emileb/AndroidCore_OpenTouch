package com.opentouchgaming.androidcore

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

class FileMover {

    fun doMove(activity: Activity, destination: String, files: ArrayList<String>): Boolean {

        var filesFiltered = fileFiles(files);

        // No files to move
        if (filesFiltered.size == 0)
            return false;


        var dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_move_files)

        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)
        var text =  dialog.findViewById<TextView>(R.id.title)

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        progressBar.max = filesFiltered.size;
        dialog.show()

        doAsync {

            for (file in filesFiltered) {
                // Move (rename file)
                var fileReal = File(file)
                var fileName = fileReal.name
                fileReal.renameTo(File(destination + "/" + fileName))

                uiThread {
                    progressBar.progress++
                }
            }

            uiThread {
                dialog.dismiss()
            }
        }

        return true
    }


    fun fileFiles(files: ArrayList<String>): ArrayList<String> {
        var filesFiltered = ArrayList<String>()

        for (file in files) {
            if (File(file).exists()) {
                filesFiltered.add(file);
            }
        }

        return filesFiltered;
    }
}