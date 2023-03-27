package com.opentouchgaming.androidcore.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
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
import org.jetbrains.anko.uiThread
import org.ocpsoft.prettytime.PrettyTime
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserFilesDialog
{
    lateinit var binding: DialogUserFilesManagerBinding

    data class UserFileEntryDescription(val name: String = "", val version: String = "", val icon: Int = 0, val path: String = "")

    class UserFileEntry(val description: UserFileEntryDescription)
    {
        val files: ArrayList<String> = ArrayList()
        var totalSize = 0L
        var lastModified = 0L
    }

    var totalSize = 0L

    val userFileEntries: ArrayList<UserFileEntry> = ArrayList()
    var scanComplete = false

    val userFilesPath: String = AppInfo.getUserFiles()

    lateinit var adaptor: CustomAdapter

    fun showDialog(activity: Activity, entries: Array<UserFileEntryDescription>)
    {
        binding = DialogUserFilesManagerBinding.inflate(activity.layoutInflater)

        val dialog = Dialog(activity, R.style.DialogThemeFullscreen_dark)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        dialog.setContentView(binding.root)

        val userPath = AppInfo.getDisplayPathAndImage(AppInfo.getUserFiles())
        binding.userDirTextview.text = userPath.first
        binding.userDirImage.setImageResource(userPath.second!!)

        adaptor = CustomAdapter(userFileEntries, userFilesPath)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
        binding.recyclerView.adapter = adaptor

        for (description in entries)
        {
            userFileEntries.add(UserFileEntry(description))
        }

        dialog.show()

        scanFolders()
    }

    fun scanFolders()
    {
        scanComplete = false
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
                        //val attr: BasicFileAttributes = Files.readAttributes(it, BasicFileAttributes::class.java)
                        val attr: BasicFileAttributes = Files.readAttributes<BasicFileAttributes>(it.toPath(), BasicFileAttributes::class.java)
                        entry.lastModified = attr.lastModifiedTime().toMillis()
                        entry.files.add(it.absolutePath)
                        entry.totalSize += it.length()
                    }
                }

                totalSize += entry.totalSize
            }

            uiThread {
                scanComplete = true
                updateUI()
            }
        }
    }

    fun updateUI()
    {
        adaptor.notifyDataSetChanged()
    }

    class CustomAdapter(val entries: ArrayList<UserFileEntry>, val userFilesPath: String) : RecyclerView.Adapter<CustomAdapter.ViewHolder>()
    {
        class ViewHolder(view: View, val binding: ListItemUserFilesEntryBinding) : RecyclerView.ViewHolder(view)

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

            viewHolder.binding.engineDetailsTextView.text = "Size = " + Utils.humanReadableByteCount(entries[position].totalSize, false)

            if(entries[position].lastModified != 0L)
            {
                val p = PrettyTime()
                viewHolder.binding.engineDetailsTextView.text = "Last modified - ${p.format(Date(entries[position].lastModified))}"
            }
            else
            {
                viewHolder.binding.engineDetailsTextView.text = "No files"
            }

            val userPath = AppInfo.getDisplayPathAndImage(userFilesPath + "/" + entries[position].description.path)
            viewHolder.binding.enginePathTextView.text = userPath.first
            viewHolder.binding.enginePathImageView.imageResource = userPath.second
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = entries.size

    }
}