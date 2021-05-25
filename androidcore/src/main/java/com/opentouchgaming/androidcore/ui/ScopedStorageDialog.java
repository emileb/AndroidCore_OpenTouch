package com.opentouchgaming.androidcore.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.R;
import com.opentouchgaming.androidcore.ScopedStorage;
import com.opentouchgaming.androidcore.Utils;
import com.opentouchgaming.saffal.FileSAF;
import com.opentouchgaming.saffal.UtilsSAF;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;

import static com.opentouchgaming.androidcore.DebugLog.Level.D;


public class ScopedStorageDialog
{

    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.GAMEFRAGMENT, "ScopedStorageDialog");
    }


    public static class Tutorial
    {
        public String folder;
        public List<Pair<Integer, String>> items;
    }

    Activity activity;
    RecyclerView recyclerView;
    ViewAdapter viewAdapter;
    Tutorial tutorial;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ScopedStorageDialog(final Activity activity, Tutorial tutorial, Runnable update)
    {
        this.activity = activity;
        this.tutorial = tutorial;

        final Dialog dialog = new Dialog(activity);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_scoped_storage);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        TextView t = dialog.findViewById(R.id.suggested_folder_textView);
        t.setText(tutorial.folder);
        t = dialog.findViewById(R.id.suggested_folder_1_textView);
        t.setText(tutorial.folder);

        recyclerView = dialog.findViewById(R.id.image_recyclerView);
        //recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        viewAdapter = new ViewAdapter();
        recyclerView.setAdapter(viewAdapter);


        Button goButton = dialog.findViewById(R.id.go_button);

        goButton.setOnClickListener(v ->
                                    {
                                        UtilsSAF.openDocumentTree(activity, ScopedStorage.OPENDOCUMENT_TREE_RESULT);
                                    });

        dialog.setOnDismissListener(dialog1 -> update.run());

        dialog.show();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ScopedStorage.openDocumentCallback = new Function<String, Void>()
        {
            @Override
            public Void apply(String error)
            {
                if (error != null)
                {
                    log.log(D, "ERROR: " + error);
                    AppInfo.setAppSecDirectory(null);
                    Toast.makeText(activity, "ERROR: " + error, Toast.LENGTH_LONG).show();
                }
                else
                {
                    AppInfo.setAppSecDirectory(UtilsSAF.getTreeRoot().rootPath);
                    dialog.dismiss();

                    new CopyFilesTask().execute("");
                }
                return null;
            }
        };

        viewAdapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void copyFolder(FileSAF src, FileSAF dest) throws IOException
    {
        if (src.isDirectory())
        {
            if (!dest.exists())
            {
                dest.mkdir();
                log.log(D, "MKDIR: " + dest.toString());
            }

            String files[] = src.list();

            for (String file : files)
            {
                FileSAF srcFile = new FileSAF(src, file);
                FileSAF destFile = new FileSAF(dest, file);

                copyFolder(srcFile, destFile);
            }
        }
        else if (!dest.exists()) // Copy only if it does not exist
        {
            log.log(D, "COPYING: from (" + src.toString() + ")  to  (" + dest.toString() + ")");
            InputStream in;
            OutputStream out;

            if (src.isRealFile())
                in = new FileInputStream(src);
            else
                in = src.getInputStream();

            // Always writing to real file for now..
            out = new FileOutputStream(dest);

            Utils.copyFile(in, out);

            in.close();
            out.close();
        }
        else
        {
            log.log(D, "File" + dest.toString() + " already exists, not copying");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void copyFiles(FileSAF src, FileSAF dest)
    {
        FileSAF[] oldFiles = src.listFiles();

        if (oldFiles != null && oldFiles.length != 0)
        {
            log.log(D, "Old files = " + oldFiles.length + " copying to: " + dest.toString());
            try
            {
                copyFolder(src, dest);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    private void copyUserFiles()
    {
        if (AppInfo.isScopedEnabled() || AppInfo.getAppSecDirectory() != null)
        {

            FileSAF oldUserFilesDir = new FileSAF(AppInfo.getUserFiles_FROMSEC());
            FileSAF newUserFilesDir = new FileSAF(AppInfo.getUserFiles());

            copyFiles(oldUserFilesDir, newUserFilesDir);

            FileSAF oldAudio = new FileSAF(AppInfo.getAppSecDirectory() + "/audiopack");
            FileSAF newAudio = new FileSAF(AppInfo.getAppDirectory() + "/audiopack");

            copyFiles(oldAudio, newAudio);
        }
    }

    private class CopyFilesTask extends AsyncTask<String, Integer, Long>
    {
        private ProgressDialog progressBar;

        @Override
        protected void onPreExecute()
        {
            progressBar = new ProgressDialog(activity);
            progressBar.setMessage("Copying user files..");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setCancelable(false);
            progressBar.show();
        }

        protected Long doInBackground(String... url)
        {
            copyUserFiles();
            return 0l;
        }

        protected void onProgressUpdate(Integer... progress)
        {

        }

        protected void onPostExecute(Long result)
        {
            progressBar.dismiss();
        }
    }


    public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.ViewHolder>
    {

        @Override
        public ViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_scoped_tutorial, parent, false);

            return new ViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            holder.image.setImageResource(tutorial.items.get(position).first);
            holder.number.setText("");
            holder.info.setText(tutorial.items.get(position).second);
        }


        @Override
        public int getItemCount()
        {
            return tutorial.items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public final ImageView image;
            public final TextView number;
            public final TextView info;

            public ViewHolder(View view)
            {
                super(view);
                this.image = view.findViewById(R.id.imageView);
                this.number = view.findViewById(R.id.number_textView);
                this.info = view.findViewById(R.id.info_textView);
            }
        }
    }
}
