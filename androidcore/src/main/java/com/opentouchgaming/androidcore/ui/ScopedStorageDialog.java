package com.opentouchgaming.androidcore.ui;

import android.app.Activity;
import android.app.Dialog;
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
import com.opentouchgaming.saffal.UtilsSAF;

import java.util.ArrayList;
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

    Activity activity;
    RecyclerView recyclerView;
    ViewAdapter viewAdapter;
    List<Pair<Integer,String>> items;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ScopedStorageDialog(final Activity activity, Runnable update)
    {
        this.activity = activity;

        final Dialog dialog = new Dialog(activity);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_scoped_storage);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        items = new ArrayList<>();
        //items.add(R.drawable.ss_1);
        items.add(new Pair<>(R.drawable.ss_2, "Select where you want your files."));
        items.add(new Pair<>(R.drawable.ss_3, "Create (or select) the 'OpenTouch' folder."));
        items.add(new Pair<>(R.drawable.ss_4, "Create (or select) the 'Quad' folder."));
        items.add(new Pair<>(R.drawable.ss_5, "Check the path is correct and press 'USE THIS FOLDER'"));


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
                }
                return null;
            }
        };

        viewAdapter.notifyDataSetChanged();
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
            holder.image.setImageResource(items.get(position).first);
            holder.number.setText("");
            holder.info.setText(items.get(position).second);
        }


        @Override
        public int getItemCount()
        {
            return items.size();
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
                this.number =  view.findViewById(R.id.number_textView);
                this.info = view.findViewById(R.id.info_textView);
            }
        }
    }
}
