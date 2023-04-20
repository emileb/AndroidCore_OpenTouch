package com.opentouchgaming.androidcore.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.Utils
import com.opentouchgaming.androidcore.databinding.DialogImportExportBinding
import com.opentouchgaming.androidcore.databinding.ListItemUserFilesExportBinding
import org.jetbrains.anko.doIfSdk
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.collections.ArrayList

class ImportExportDialog
{
    lateinit var binding: DialogImportExportBinding
    lateinit var adaptor: ImportExportEntryAdapter
    lateinit var activity: Activity

    var export = false

    val backupPaths = ArrayList<String>()
    val exportList = ArrayList<String>()

    fun showDialog(activity: Activity, export: Boolean, entries: ArrayList<UserFilesDialog.UserFileEntry>, userFilesPath: String,
                   callback: ((filename: String, folders: ArrayList<String>) -> Unit))
    {
        this.activity = activity
        this.export = export

        binding = DialogImportExportBinding.inflate(activity.layoutInflater)

        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)

        val width: Int = ((activity.getResources().getDisplayMetrics().widthPixels * 0.98).toInt())
        val height: Int = ((activity.getResources().getDisplayMetrics().heightPixels * 0.90).toInt())
        dialog.getWindow()?.setLayout(width, height)

        adaptor = ImportExportEntryAdapter(entries, userFilesPath)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
        binding.recyclerView.adapter = adaptor

        for (e in entries)
        {
            e.selected = true
        }

        backupPaths.add(AppInfo.getAppDirectory() + "/backup/")

        val customAdapter = CustomSpinnerAdapter(activity, backupPaths)

        //val adapter = ArrayAdapter(activity, com.opentouchgaming.androidcore.R.layout.spinner_item_end, backupPaths)
        //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.savePathSpinner.adapter = customAdapter

        if(export)
            binding.okButton.text = "Export"
        else
            binding.okButton.text = "Import"

        binding.okButton.setOnClickListener(View.OnClickListener {
            exportList.clear()
            for(entry in entries)
            {
                if(entry.selected)
                    exportList.add(entry.description.path)
            }
            callback.invoke(backupPaths[0] + "test.zip", exportList)
            dialog.dismiss()
        })
        dialog.show()
    }


    class CustomSpinnerAdapter(context: Context, private val itemList: List<String>) : ArrayAdapter<String>(context, 0, itemList)
    {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
        {
            val view: View = convertView ?: LayoutInflater.from(context).inflate(com.opentouchgaming.androidcore.R.layout.spinner_item_end, parent, false)
            val textView = view.findViewById<TextView>(com.opentouchgaming.androidcore.R.id.path)
            val image = view.findViewById<ImageView>(com.opentouchgaming.androidcore.R.id.image)
            val userPath = AppInfo.getDisplayPathAndImage(itemList[position])
            textView.text = userPath.first
            image.setImageResource(userPath.second!!)
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View
        {
            val view: View = convertView ?: LayoutInflater.from(context).inflate(com.opentouchgaming.androidcore.R.layout.spinner_item_end, parent, false)
            val textView = view.findViewById<TextView>(com.opentouchgaming.androidcore.R.id.path)
            val image = view.findViewById<ImageView>(com.opentouchgaming.androidcore.R.id.image)
            val userPath = AppInfo.getDisplayPathAndImage(itemList[position])
            textView.text = userPath.first
            image.setImageResource(userPath.second!!)
            return view
        }
    }


    inner class ImportExportEntryAdapter(val entries: ArrayList<UserFilesDialog.UserFileEntry>, val userFilesPath: String) :
            RecyclerView.Adapter<ImportExportEntryAdapter.ViewHolder>()
    {
        inner class ViewHolder(view: View, val binding: ListItemUserFilesExportBinding) : RecyclerView.ViewHolder(view)

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder
        {
            val binding = ListItemUserFilesExportBinding.inflate(LayoutInflater.from(viewGroup.context))
            return ViewHolder(binding.root, binding)
        }

        // Replace the contents of a view (invoked by the layout manager)
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int)
        {
            viewHolder.binding.engineIconImageView.imageResource = (entries[position].description.icon)

            if(entries[position].description.version.isNotEmpty())
                viewHolder.binding.engineNameTextView.text = entries[position].description.name + " (" + entries[position].description.version + ")"
            else
                viewHolder.binding.engineNameTextView.text = entries[position].description.name

            //viewHolder.binding.engineVersionTextView.text = entries[position].description.version

            if (entries[position].lastModified != 0L)
            {
                val p = PrettyTime()
                //  viewHolder.binding.engineDetailsTextView.text = "Last modified - ${p.format(Date(entries[position].lastModified))}"
                viewHolder.binding.engineSizeTextView.text = Utils.humanReadableByteCount(entries[position].totalSize, false)
                viewHolder.binding.enableCheckBox.isEnabled = true
            }
            else
            {
                //   viewHolder.binding.engineDetailsTextView.text = "No files"
                viewHolder.binding.engineSizeTextView.text = ""
                viewHolder.binding.enableCheckBox.isEnabled = false
            }

            // Clear to stop it triggering when recycling views
            viewHolder.binding.enableCheckBox.setOnCheckedChangeListener (null);

            viewHolder.binding.enableCheckBox.isChecked = entries[position].selected

            viewHolder.binding.enableCheckBox.onCheckedChange { _, isChecked ->
                entries[position].selected = isChecked
                adaptor.notifyDataSetChanged()
            }

            viewHolder.binding.enginePathTextView.text = entries[position].description.path
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = entries.size
    }
}