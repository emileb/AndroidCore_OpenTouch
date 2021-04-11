package com.opentouchgaming.androidcore.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.DirectoryChooserDialog;
import com.opentouchgaming.androidcore.R;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class StorageConfigDialog
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.LICENSE, "StorageConfigDialog");
    }

    Activity activity;
    Runnable update;
    TextView appDirTextView;
    ImageView appDirIcon;
    TextView appSecDirTextView;
    ImageView appSecDirIcon;
    RecyclerView examplesRecyclerView;
    PathExampleViewAdapter examplesViewAdapter;
    List<StorageExamples> examples;

    public StorageConfigDialog(final Activity act, List<StorageExamples> examples, Runnable update)
    {
        this.activity = act;
        this.examples = examples;
        this.update = update;

        final Dialog dialog = new Dialog(act, R.style.DialogThemeFullscreen);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_storage_config);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        appDirTextView = dialog.findViewById(R.id.app_dir_textview);
        appDirIcon = dialog.findViewById(R.id.app_dir_image);
        appSecDirTextView = dialog.findViewById(R.id.appSec_dir_textview);
        appSecDirIcon = dialog.findViewById(R.id.appSec_dir_image);
        ImageView appDirButton = dialog.findViewById(R.id.app_dir_options_button);
        ImageView appSecDirButton = dialog.findViewById(R.id.appSec_dir_options_button);

        examplesRecyclerView = dialog.findViewById(R.id.examples_RecyclerView);
        examplesRecyclerView.setLayoutManager(new LinearLayoutManager(act));
        examplesViewAdapter = new PathExampleViewAdapter();
        examplesRecyclerView.setAdapter(examplesViewAdapter);

        Switch scopedStorage = dialog.findViewById(R.id.scoped_stroage_switch);


        scopedStorage.setEnabled(AppInfo.isScopedAllowed());

        scopedStorage.setChecked(AppInfo.isScopedEnabled());
        scopedStorage.setOnCheckedChangeListener((buttonView, isChecked) ->
                                                 {
                                                     AppInfo.setScoped(isChecked);
                                                     updateUI();
                                                 });

        // PRIMARY folder options
        appDirButton.setOnClickListener(v ->
                                        {
                                            PopupMenu popup = new PopupMenu(activity, appDirButton);
                                            popup.getMenuInflater().inflate(R.menu.app_dir_popup, popup.getMenu());
                                            popup.setOnMenuItemClickListener(item ->
                                                                             {
                                                                                 if (item.getItemId() == R.id.reset)
                                                                                 {
                                                                                     AppInfo.setAppDirectory(null); //This resets it
                                                                                 }
                                                                                 else if (item.getItemId() == R.id.sdcard)
                                                                                 {

                                                                                     // Primary always need to be on the writable SD card area
                                                                                     if (AppInfo.sdcardWritable != null)
                                                                                     {
                                                                                         AppInfo.setAppDirectory(AppInfo.sdcardWritable);
                                                                                     }
                                                                                     else
                                                                                         Toast.makeText(activity, "Did not detect SD card", Toast.LENGTH_LONG).show();

                                                                                 }
                                                                                 else if (item.getItemId() == R.id.choose)
                                                                                 {
                                                                                     DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(activity, chosenDir ->
                                                                                     {
                                                                                         updateAppDir(chosenDir);
                                                                                         updateUI();
                                                                                     });
                                                                                     directoryChooserDialog.chooseDirectory(AppInfo.getAppDirectory());
                                                                                 }

                                                                                 updateUI();

                                                                                 return true;
                                                                             });

                                            popup.show();
                                        });

        // SECONDARY folder options
        appSecDirButton.setOnClickListener(v ->
                                           {
                                               PopupMenu popup = new PopupMenu(activity, appSecDirButton);

                                               if (AppInfo.isScopedEnabled())
                                               {
                                                   new ScopedStorageDialog(activity, () ->
                                                   {
                                                       updateUI();
                                                   });
                                                   updateUI();
                                               }
                                               else
                                               {
                                                   popup.getMenuInflater().inflate(R.menu.app_sec_dir_popup, popup.getMenu());
                                                   popup.setOnMenuItemClickListener(item ->
                                                                                    {
                                                                                        if (item.getItemId() == R.id.reset)
                                                                                        {
                                                                                            AppInfo.setAppSecDirectory(null); //This resets it
                                                                                        }
                                                                                        else if (item.getItemId() == R.id.sdcard)
                                                                                        {

                                                                                            if (AppInfo.sdcardRoot != null)
                                                                                            {
                                                                                                if (AppInfo.isScopedEnabled())
                                                                                                { // Scoped storage can only read here now..
                                                                                                    AppInfo.setAppSecDirectory(AppInfo.sdcardWritable);
                                                                                                }
                                                                                                else
                                                                                                {
                                                                                                    AppInfo.setAppSecDirectory(AppInfo.sdcardRoot);
                                                                                                }
                                                                                            }
                                                                                            else
                                                                                                Toast.makeText(activity, "Did not detect SD card", Toast.LENGTH_LONG).show();

                                                                                        }
                                                                                        else if (item.getItemId() == R.id.internal)
                                                                                        {
                                                                                            AppInfo.setAppSecDirectory(AppInfo.flashRoot);
                                                                                        }
                                                                                        else if (item.getItemId() == R.id.choose)
                                                                                        {
                                                                                            DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(activity, chosenDir ->
                                                                                            {
                                                                                                updateAppSecDir(chosenDir);
                                                                                                updateUI();
                                                                                            });
                                                                                            directoryChooserDialog.chooseDirectory(AppInfo.getAppSecDirectory());
                                                                                        }

                                                                                        updateUI();

                                                                                        return true;
                                                                                    });
                                               }
                                               popup.show();
                                           });


        updateUI();

        dialog.setOnDismissListener(dialog12 -> update.run());

        dialog.show();
    }

    private void updateUI()
    {

        Pair<String, Integer> pathApp = AppInfo.getDisplayPathAndImage(AppInfo.getAppDirectory());
        appDirTextView.setText(pathApp.first);
        appDirIcon.setImageResource(pathApp.second);

        Pair<String, Integer> pathAppSec = AppInfo.getDisplayPathAndImage(AppInfo.getAppSecDirectory());
        appSecDirTextView.setText(pathAppSec.first);
        appSecDirIcon.setImageResource(pathAppSec.second);

        examplesViewAdapter.notifyDataSetChanged();
    }

    private void updateAppDir(String dir)
    {
        File fdir = new File(dir);

        if (!fdir.isDirectory())
        {
            showError(dir + " is not a directory");
            return;
        }

        if (!fdir.canWrite())
        {
            showError(dir + " is not a writable");
            return;
        }

        //Test CAN actually write, the above canWrite can pass on KitKat SD cards WTF GOOGLE
        File test_write = new File(dir, "test_write");
        try
        {
            test_write.createNewFile();
            if (!test_write.exists())
            {
                showError(dir + " is not a writable");
                return;
            }
        } catch (IOException e)
        {
            showError(dir + " is not a writable");
            return;
        }
        test_write.delete();

        if (dir.contains(" "))
        {
            showError(dir + " must not contain any spaces");
            return;
        }

        AppInfo.setAppDirectory(dir);

        updateUI();
    }

    private void updateAppSecDir(String dir)
    {
        File fdir = new File(dir);

        if (!fdir.isDirectory())
        {
            showError(dir + " is not a directory");
            return;
        }

        if (dir.contains(" "))
        {
            showError(dir + " must not contain any spaces");
            return;
        }

        AppInfo.setAppSecDirectory(dir);

        updateUI();
    }

    private void showError(String error)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(error);

        dialogBuilder.setPositiveButton("OK", (dialog, which) ->
        {
        });

        final AlertDialog errdialog = dialogBuilder.create();
        errdialog.show();
    }

    public static enum PathLocation
    {
        PRIM, SEC, BOTH
    }

    public static class StorageExamples
    {
        String title;
        String files;
        PathLocation pathLocation;
        String path;

        public StorageExamples(String info, String files, PathLocation pathLocation, String path)
        {
            this.title = info;
            this.files = files;
            this.pathLocation = pathLocation;
            this.path = path;
        }
    }

    public class PathExampleViewAdapter extends RecyclerView.Adapter<PathExampleViewAdapter.ViewHolder>
    {

        @Override
        public PathExampleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_storage_example, parent, false);
            //view.setFocusable(true);
            //view.setBackgroundResource(R.drawable.focusable);
            return new PathExampleViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {

            holder.title.setText(examples.get(position).title);
            holder.files.setText(examples.get(position).files);

            String path = examples.get(position).path;
            PathLocation loc = examples.get(position).pathLocation;


            if (loc == PathLocation.BOTH || loc == PathLocation.PRIM)
            {
                holder.appDirTextView.setVisibility(View.VISIBLE);
                holder.appDirIcon.setVisibility(View.VISIBLE);

                Pair<String, Integer> pathApp = AppInfo.getDisplayPathAndImage(AppInfo.getAppDirectory());

                holder.appDirTextView.setText(pathApp.first + path);
                holder.appDirIcon.setImageResource(pathApp.second);
            }
            else
            {
                holder.appDirTextView.setVisibility(View.GONE);
                holder.appDirIcon.setVisibility(View.GONE);
            }


            if ((AppInfo.getAppSecDirectory() != null) && (loc == PathLocation.BOTH || loc == PathLocation.SEC))
            {
                holder.appSecDirTextView.setVisibility(View.VISIBLE);
                holder.appSecDirIcon.setVisibility(View.VISIBLE);

                Pair<String, Integer> pathApp = AppInfo.getDisplayPathAndImage(AppInfo.getAppSecDirectory());

                holder.appSecDirTextView.setText(pathApp.first + path);
                holder.appSecDirIcon.setImageResource(pathApp.second);
            }
            else
            {
                holder.appSecDirTextView.setVisibility(View.GONE);
                holder.appSecDirIcon.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount()
        {
            return examples.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public final TextView title;
            public final TextView files;
            public final TextView appDirTextView;
            public final ImageView appDirIcon;
            public final TextView appSecDirTextView;
            public final ImageView appSecDirIcon;
            public String item;

            public ViewHolder(View view)
            {
                super(view);

                this.title = view.findViewById(R.id.title_textView);
                this.files = view.findViewById(R.id.files_textView);
                this.appDirTextView = view.findViewById(R.id.app_dir_textview);
                this.appDirIcon = view.findViewById(R.id.app_dir_image);
                this.appSecDirTextView = view.findViewById(R.id.appSec_dir_textview);
                this.appSecDirIcon = view.findViewById(R.id.appSec_dir_image);
            }
        }
    }
}
