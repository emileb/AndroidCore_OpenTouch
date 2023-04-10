package com.opentouchgaming.androidcore.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.view.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.R
import com.opentouchgaming.androidcore.Utils
import com.opentouchgaming.androidcore.databinding.DialogUserFilesManagerBinding
import com.opentouchgaming.androidcore.databinding.ListItemUserFilesEntryBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.uiThread
import org.ocpsoft.prettytime.PrettyTime
import java.io.File
import java.util.*

class UserFilesDialog
{
    lateinit var binding: DialogUserFilesManagerBinding

    data class UserFileEntryDescription(val name: String = "", val version: String = "", val icon: Int = 0, val path: String = "")

    class UserFileEntry(val description: UserFileEntryDescription)
    {
        val files: ArrayList<String> = ArrayList()
        var totalSize = 0L
        var lastModified = 0L
        var selected = true
    }

    enum class RunningState {
        READY, SCANNING, DELETING, EXPORTING, IMPORTING
    }

    var runningState = RunningState.READY

    var totalSize = 0L

    val userFileEntries: ArrayList<UserFileEntry> = ArrayList()

    val userFilesPath: String = AppInfo.getUserFiles()

    lateinit var adaptor: UserFileEntryAdapter

    lateinit var act: Activity

    fun showDialog(activity: Activity, entries: Array<UserFileEntryDescription>)
    {
        act = activity

        binding = DialogUserFilesManagerBinding.inflate(activity.layoutInflater)

        val dialog = Dialog(activity, R.style.Theme_Material3_Dark)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        dialog.setContentView(binding.root)

        val userPath = AppInfo.getDisplayPathAndImage(AppInfo.getUserFiles())
        binding.userDirTextview.text = userPath.first
        binding.userDirImage.setImageResource(userPath.second!!)

        adaptor = UserFileEntryAdapter(userFileEntries, userFilesPath)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
        binding.recyclerView.adapter = adaptor

        for (description in entries)
        {
            userFileEntries.add(UserFileEntry(description))
        }

        binding.importButton.onClick {
            val d = ImportExportDialog()
            d.showDialog(activity, true, userFileEntries, userFilesPath)
        }

        dialog.show()
        scanFolders()
    }

    fun scanFolders()
    {
        runningState = RunningState.SCANNING

        totalSize = 0;

        doAsync {
            for (entry in userFileEntries)
            {
                entry.totalSize = 0;
                entry.lastModified = 0L;
                entry.files.clear()

                val path = userFilesPath + "/" + entry.description.path
                File(path).walk(FileWalkDirection.BOTTOM_UP).forEach {
                    if (it.isFile)
                    {
                        println(it)
                        entry.lastModified = it.lastModified();
                        entry.files.add(it.absolutePath)
                        entry.totalSize += it.length()
                    }

                    Thread.sleep(5)

                    uiThread {
                        updateUI()
                    }
                }

                totalSize += entry.totalSize
            }

            uiThread {
                runningState = RunningState.READY
                updateUI()
            }
        }
    }

    fun deleteEntry(entry: UserFileEntry)
    {
        runningState = RunningState.DELETING
        updateUI()

        doAsync {
                val path = userFilesPath + "/" + entry.description.path
                val success =  File(path).deleteRecursively();
            uiThread {
                runningState = RunningState.READY
                // Rescan
                scanFolders()
            }
        }
    }

    fun showAlert(activity: Activity, title: String, message: String, function: () -> (Unit))
    {
        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle(title)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton("OK") { _: DialogInterface, _: Int ->
            function.invoke()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    fun updateUI()
    {
        adaptor.notifyDataSetChanged()
        binding.totalSizeTextView.text = Utils.humanReadableByteCount(totalSize, false)

        if (runningState != RunningState.READY)
        {
            binding.statusTextView.textColor = Color.parseColor("#FF9f0f0f")
            binding.statusTextView.text = "Processing..."
            binding.importButton.isEnabled = false
            binding.exportButton.isEnabled = false
        }
        else
        {
            binding.statusTextView.textColor = Color.parseColor("#1c7a07")
            binding.statusTextView.text = "Ready"
            binding.importButton.isEnabled = true
            binding.exportButton.isEnabled = true
        }
    }

    inner class UserFileEntryAdapter(val entries: ArrayList<UserFileEntry>, val userFilesPath: String) : RecyclerView.Adapter<UserFileEntryAdapter.ViewHolder>()
    {
        inner class ViewHolder(view: View, val binding: ListItemUserFilesEntryBinding) : RecyclerView.ViewHolder(view)

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder
        {
            val binding = ListItemUserFilesEntryBinding.inflate(LayoutInflater.from(viewGroup.context))
            return ViewHolder(binding.root, binding)
        }

        // Replace the contents of a view (invoked by the layout manager)
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int)
        {
            viewHolder.binding.engineIconImageView.imageResource = (entries[position].description.icon)
            viewHolder.binding.engineNameTextView.text = entries[position].description.name
            viewHolder.binding.engineVersionTextView.text = entries[position].description.version

            if (entries[position].lastModified != 0L)
            {
                val p = PrettyTime()
                viewHolder.binding.engineDetailsTextView.text = "Last modified - ${p.format(Date(entries[position].lastModified))}"
                viewHolder.binding.engineSizeTextView.text = Utils.humanReadableByteCount(entries[position].totalSize, false)
            }
            else
            {
                viewHolder.binding.engineDetailsTextView.text = "No files"
                viewHolder.binding.engineSizeTextView.text = ""
            }

            val userPath = AppInfo.getDisplayPathAndImage(userFilesPath + "/" + entries[position].description.path)
            viewHolder.binding.enginePathTextView.text = userPath.first
            viewHolder.binding.enginePathImageView.imageResource = userPath.second

            if (runningState == RunningState.READY && (entries[position].lastModified != 0L))
            {
                viewHolder.binding.deleteButton.visibility = View.VISIBLE

                viewHolder.binding.deleteButton.setOnClickListener {
                    showAlert(
                            act, "DELETE FILES",
                            "Delete all user files for: " + entries[position].description.name + " (" + entries[position].description.version + ")",
                    ) {
                        deleteEntry(entries[position])
                    }
                }
            }
            else
            {
                viewHolder.binding.deleteButton.visibility = View.INVISIBLE
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = entries.size
    }
}