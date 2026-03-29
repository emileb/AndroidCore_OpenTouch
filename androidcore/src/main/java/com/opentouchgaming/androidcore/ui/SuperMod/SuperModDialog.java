package com.opentouchgaming.androidcore.ui.SuperMod;


import static com.opentouchgaming.androidcore.DebugLog.Level.D;
import static com.opentouchgaming.androidcore.DebugLog.Level.E;
import static com.opentouchgaming.androidcore.DebugLog.Level.I;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.util.Consumer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.AppSettings;
import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.GameEngine;
import com.opentouchgaming.androidcore.R;
import com.opentouchgaming.androidcore.Utils;
import com.opentouchgaming.androidcore.ui.FileSelectDialog;
import com.opentouchgaming.saffal.FileSAF;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class SuperModDialog
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.APP, "SuperModDialog");
    }

    final Dialog dialog;
    private final ListView mainList;
    private final ListAdapter listAdapter;
    Activity activity;
    ArrayList<SuperModItem> items;
    ArrayList<SuperModItem> itemsSorted;
    ArrayList<SuperModItem> itemsFiltered;
    EditText searchEditText;
    RelativeLayout searchLayout;
    String searchText;
    int sortType = 1; // 0 = added, 1 = title, 2 = wad

    public SuperModDialog(final Activity act, final SuperModItem newSuperMod, Consumer<SuperModItem> selectCallback)
    {
        activity = act;

        dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_super_mod);
        dialog.setCancelable(true);
        Utils.setInsets(act, dialog);

        dialog.setOnKeyListener(new Dialog.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event)
            {
                return false;
            }
        });

        sortType = AppSettings.getIntOption(activity, "my_mods_sort", 0);

        items = loadList();
        itemsSorted = new ArrayList<>();
        itemsFiltered = new ArrayList<>();

        mainList = dialog.findViewById(R.id.listView);
        searchLayout = dialog.findViewById(R.id.search_relativeLayout);
        searchEditText = dialog.findViewById(R.id.search_editText);

        listAdapter = new ListAdapter();
        mainList.setAdapter(listAdapter);

        mainList.setOnItemLongClickListener((parent, view, position, id) ->
                                            {
                                                editItem(position);
                                                return true;
                                            });

        mainList.setOnItemClickListener((parent, view, position, id) ->
                                        {
                                            SuperModItem item = itemsFiltered.get(position);
                                            log.log(D, "Selected " + id + " Title = " + item.title);
                                            selectCallback.accept(itemsFiltered.get(position));
                                            dialog.dismiss();
                                        });

        ImageButton addButton = dialog.findViewById(R.id.add_imageButton);
        addButton.setOnClickListener(v ->
                                     {
                                         if (newSuperMod != null)
                                         {
                                             SuperModItem copy = new SuperModItem(newSuperMod);
                                             items.add(0, copy);
                                             saveList();
                                             editItem(0);
                                         }
                                     });

        ImageButton sortButton = dialog.findViewById(R.id.sort_imageButton);
        sortButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String[] options = {"Date added", "Title"};

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Sort by..");
                builder.setItems(options, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        sortType = which;
                        AppSettings.setIntOption(activity, "my_mods_sort", sortType);
                        sortList();
                    }
                });
                builder.show();
            }
        });

        // ---- Search ----------------------------------------------------------

        final ImageButton searchButton = dialog.findViewById(R.id.search_imageButton);
        searchButton.setOnClickListener(v ->
                                        {
                                            if (searchLayout.getVisibility() == View.GONE)
                                            {
                                                searchText = AppSettings.getStringOption(activity, "super_mod_search_filter", null);
                                                if (searchText != null)
                                                    searchEditText.setText(searchText);
                                                searchLayout.setVisibility(View.VISIBLE);
                                                applySearch();
                                            }
                                            else
                                            {
                                                searchText = null;
                                                searchLayout.setVisibility(View.GONE);
                                                applySearch();
                                            }
                                        });

        searchEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                searchText = s.toString();
                AppSettings.setStringOption(activity, "super_mod_search_filter", searchText);
                applySearch();
            }
        });

        ImageButton clearSearch = dialog.findViewById(R.id.clear_search_imageButton);
        clearSearch.setOnClickListener(v ->
                                       {
                                           searchText = null;
                                           searchEditText.setText("");
                                           applySearch();
                                       });

        // ----------------------------------------------------------------------

        dialog.show();

        sortList();

        if (items.size() == 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("This screen allows you to save your wad + mod + args configurations. Configure your game how to wish to play then come to " +
                               "this " +
                               "screen and press the '+' button to save.").setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                }
            });

            builder.show();
        }
    }


    private void sortList()
    {
        itemsSorted.clear();
        itemsSorted.addAll(items);

        Comparator<SuperModItem> comparator = null;

        if (sortType == 1) // by title
        {
            comparator = new Comparator<SuperModItem>()
            {
                @Override
                public int compare(SuperModItem lhs, SuperModItem rhs)
                {
                    return String.CASE_INSENSITIVE_ORDER.compare(lhs.title, rhs.title);
                }
            };
        }

        if (comparator != null)
            Collections.sort(itemsSorted, comparator);

        applySearch();
    }

    private void applySearch()
    {
        itemsFiltered.clear();

        if (searchText == null || searchText.length() == 0)
            itemsFiltered.addAll(itemsSorted);
        else
        {
            for (SuperModItem item : itemsSorted)
            {
                if (item.title == null || item.title.toLowerCase().contains(searchText.toLowerCase()))
                    itemsFiltered.add(item);
            }
        }

        listAdapter.notifyDataSetChanged();
    }

    private ArrayList<SuperModItem> loadList()
    {
        String fileName = AppInfo.getSuperModFile();

        ArrayList<SuperModItem> data = null;

        try
        {
            ObjectInputStream in = new ObjectInputStream(new FileSAF((fileName)).getInputStream());

            ArrayList<SuperModItem> d = (ArrayList<SuperModItem>) in.readObject();
            data = d;
            log.log(I, "File " + fileName + " loaded");
        }
        catch (FileNotFoundException e)
        {
            log.log(I, "File " + fileName + " not found");
        }
        catch (IOException e)
        {
            log.log(E, "Could not open file " + fileName + " :" + e);

        }
        catch (ClassNotFoundException e)
        {
            log.log(E, "Error reading file " + fileName + " :" + e);
        }

        //Failed to open
        if (data == null)
        {
            data = new ArrayList<SuperModItem>();
        }

        return data;
    }

    private void saveList()
    {
        String fileName = AppInfo.getSuperModFile();

        try
        {
            FileSAF fileSave = new FileSAF(fileName);

            if (!fileSave.exists())
                fileSave.createNewFile();

            ObjectOutputStream out = new ObjectOutputStream(fileSave.getOutputStream());
            out.writeObject(items);
            out.close();
        }
        catch (FileNotFoundException e)
        {
            log.log(E, "Could not open file " + fileName + " :" + e);
            e.printStackTrace();
        }
        catch (IOException e)
        {
            log.log(E, "Error writing file " + fileName + " :" + e);
            e.printStackTrace();
        }

        sortList();
    }


    private void editItem(final int pos)
    {
        final SuperModItem item = itemsFiltered.get(pos);

        final Dialog dialog = new Dialog(activity,  R.style.DialogEngineSettingsWrap);
        dialog.setTitle("Edit item");
        dialog.setContentView(R.layout.dialog_super_mod_edit_item);
        dialog.setCancelable(true);

        final EditText title_editText = dialog.findViewById(R.id.title_editText);
        title_editText.setText(item.title);

        final EditText args_editText = dialog.findViewById(R.id.args_editText);
        args_editText.setText(item.customArgs.getArgsString());

        dialog.setOnDismissListener(dialog1 ->
                                    {
                                        item.title = title_editText.getText().toString();
                                        item.customArgs.setArgs(args_editText.getText().toString());
                                        saveList();
                                    });

        Button removeButton = dialog.findViewById(R.id.delete_button);
        removeButton.setOnClickListener(v ->
                                        {
                                            items.remove(item);
                                            saveList();
                                            dialog.dismiss();
                                        });

        Button selectImageButton = dialog.findViewById(R.id.select_image_button);
        selectImageButton.setOnClickListener(v ->
                                             {

                                                 FileSelectDialog.FileSelectCallback callback = filesArray ->
                                                 {
                                                     String imageOverride;

                                                     if (filesArray == null || filesArray.size() == 0)
                                                     {
                                                         imageOverride = null;
                                                     }
                                                     else
                                                     {
                                                         imageOverride = filesArray.get(0);
                                                     }
                                                     item.modImage = imageOverride;

                                                 };

                                                 new FileSelectDialog(activity, callback, AppInfo.getAppDirectory(), new String[]{".png", ".jpg"}, false);

                                             });
        dialog.show();
    }

    class ListAdapter extends BaseAdapter
    {
        public ListAdapter()
        {

        }

        public int getCount()
        {
            return itemsFiltered.size();
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }


        public View getView(int position, View convertView, ViewGroup list)
        {
            View view;

            if (convertView == null)
                view = activity.getLayoutInflater().inflate(R.layout.super_mod_item, null);
            else
                view = convertView;

            SuperModItem item = itemsFiltered.get(position);

            TextView title_textView = view.findViewById(R.id.title_textView);
            ImageView iwad_imageView = view.findViewById(R.id.iwad_imageView);
            ImageView engine_imageView = view.findViewById(R.id.engine_imageView);
            ImageView mod_imageView = view.findViewById(R.id.mod_imageView);
            TextView version_textView = view.findViewById(R.id.version_textView);
            TextView files_textView = view.findViewById(R.id.files_textView);

            GameEngine engine = AppInfo.getGameEngine(item.engine);

            Typeface face = Typeface.createFromAsset(activity.getAssets(), "recharge_font.ttf");
            title_textView.setTypeface(face);

            if (engine != null)
            {
                // Set title
                if (item.title != null && !item.title.contentEquals(""))
                    title_textView.setText(item.title);
                else
                    title_textView.setText("NO TITLE");

                if (item.gameTypeImage != null)
                {
                    iwad_imageView.setVisibility(View.VISIBLE);
                    iwad_imageView.setImageURI(Uri.fromFile(new File(item.gameTypeImage)));
                }
                else if (AppInfo.defaultAppImage != 0)
                {
                    iwad_imageView.setVisibility(View.VISIBLE);
                    iwad_imageView.setImageResource(AppInfo.defaultAppImage);
                }
                else
                    iwad_imageView.setVisibility(View.GONE);

                // Find mod image, look in cache first, else try to extract
                if (item.modImage != null)
                {
                    mod_imageView.setVisibility(View.VISIBLE);
                    Glide.with(activity).load("zip_pic:" + item.modImage)
                            //.placeholder(R.drawable.questionmark)
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)).into(mod_imageView);
                }
                else
                {
                    mod_imageView.setVisibility(View.GONE);
                }


                // Engine icon
                engine_imageView.setImageResource(engine.iconRes);

                // Engine version
                if (item.version < engine.versions.length)
                {
                    version_textView.setText(engine.versions[item.version]);
                }
                else
                {
                    version_textView.setText("ERROR");
                }

                // Files
                StringBuilder str = new StringBuilder();
                int files = 0;
                for (String file : item.customArgs.getFiles())
                {
                    str.append("File: " + AppInfo.hideAppPaths(file) + "\n");
                    files++;
                    if (files == 3)
                        break; // Max files to show
                }
                str.append("Args: " + item.customArgs.getArgsString());
                files_textView.setText(str.toString());
            }
            return view;
        }
    }
}
