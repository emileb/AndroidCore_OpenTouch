package com.opentouchgaming.androidcore.ui.SuperMod;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
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
import com.opentouchgaming.androidcore.ui.FileSelectDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.opentouchgaming.androidcore.DebugLog.Level.D;
import static com.opentouchgaming.androidcore.DebugLog.Level.E;
import static com.opentouchgaming.androidcore.DebugLog.Level.I;


public class SuperModDialog
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.APP, "SuperModDialog");
    }

    final Dialog dialog;
    Activity activity;

    private ListView mainList;
    private ListAdapter listAdapter;

    ArrayList<SuperModItem> items;
    ArrayList<SuperModItem> itemsSorted;


    int sortType = 1; // 0 = added, 1 = title, 2 = wad

    public SuperModDialog(final Activity act, final SuperModItem newSuperMod, Consumer<SuperModItem> selectCallback)
    {
        activity = act;

        dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_super_mod);
        dialog.setCancelable(true);

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

        mainList = dialog.findViewById(R.id.listView);

        listAdapter = new ListAdapter();
        mainList.setAdapter(listAdapter);

        mainList.setOnItemLongClickListener((parent, view, position, id) ->
                                            {
                                                editItem(position);
                                                return true;
                                            });

        mainList.setOnItemClickListener((parent, view, position, id) ->
                                        {
                                            SuperModItem item = itemsSorted.get(position);
                                            log.log(D, "Selected " + id + " Title = " + item.title);
                                            selectCallback.accept(itemsSorted.get(position));
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
                String[] colors = {"Date added", "Title", "IWAD"};

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Sort by..");
                builder.setItems(colors, new DialogInterface.OnClickListener()
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

        dialog.show();

        sortList();

        if (items.size() == 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(
                    "This screen allows you to save your wad + mod + args configurations. Configure your game how to wish to play then come to this screen and press the '+' button to save.")
                    .setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener()
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

        if (sortType == 0)
        {

        }
        else if (sortType == 1) // by title
        {
            comparator = new Comparator<SuperModItem>()
            {
                @Override
                public int compare(SuperModItem lhs, SuperModItem rhs)
                {
                    int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.title, rhs.title);

                    return res;
                    // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending

                }
            };
        }
        /*
        else if (sortType == 2) // by iwad
        {
            comparator = new Comparator<SuperModItem>()
            {
                @Override
                public int compare(SuperModItem lhs, SuperModItem rhs)
                {
                    int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.iwad, rhs.iwad);
                    if (res == 0) // Is the same iwad, then sort by title
                    {
                        res = String.CASE_INSENSITIVE_ORDER.compare(lhs.title, rhs.title);
                    }
                    return res;
                    // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending

                }
            };
        }
*/
        if (comparator != null)
            Collections.sort(itemsSorted, comparator);

        listAdapter.notifyDataSetChanged();
    }

    private ArrayList<SuperModItem> loadList()
    {
        String fileName = AppInfo.getUserFiles() + "/mymods.dat";
        ArrayList<SuperModItem> data = null;

        try
        {
            InputStream fis = null;
            ObjectInputStream in = null;

            fis = new FileInputStream(fileName);
            in = new ObjectInputStream(fis);

            ArrayList<SuperModItem> d = (ArrayList<SuperModItem>) in.readObject();
            data = d;
            log.log(I, "File " + fileName + " loaded");
        } catch (FileNotFoundException e)
        {
            log.log(I, "File " + fileName + " not found");
        } catch (IOException e)
        {
            log.log(E, "Could not open file " + fileName + " :" + e.toString());

        } catch (ClassNotFoundException e)
        {
            log.log(E, "Error reading file " + fileName + " :" + e.toString());
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
        String fileName = AppInfo.getUserFiles() + "/mymods.dat";

        FileOutputStream fos = null;
        ObjectOutputStream out = null;

        try
        {
            fos = new FileOutputStream(fileName);
            out = new ObjectOutputStream(fos);
            out.writeObject(items);
            out.close();
        } catch (FileNotFoundException e)
        {
            log.log(E, "Could not open file " + fileName + " :" + e.toString());
            e.printStackTrace();
        } catch (IOException e)
        {
            log.log(E, "Error writing file " + fileName + " :" + e.toString());
            e.printStackTrace();
        }

        sortList();
    }

    class ListAdapter extends BaseAdapter
    {

        //Picasso picassoInstance;

        public ListAdapter()
        {
            //Picasso.Builder picassoBuilder = new Picasso.Builder(activity);
            //picassoBuilder.addRequestHandler(new CachedModPicDownloader());
            //picassoInstance = picassoBuilder.build();
        }


        public int getCount()
        {
            return itemsSorted.size();
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

            SuperModItem item = itemsSorted.get(position);

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

                if(item.gameTypeImage != null)
                    iwad_imageView.setImageURI(Uri.fromFile(new File(item.gameTypeImage)));
                else
                    iwad_imageView.setImageResource(AppInfo.defaultAppImage);

/*
                // Find the iwad image
                String iwadImage = TitlePicFinder.getTitlePicPath(item.iwad);
                File iwadImageFile = new File(iwadImage);
                if (iwadImageFile.exists())
                {
                    iwad_imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    iwad_imageView.setImageURI(Uri.fromFile(iwadImageFile));
                } else
                {
                    iwad_imageView.setImageResource(DoomIWad.getGameImage(item.iwad));
                }
*/
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

    /*
        class CachedModPicDownloader extends RequestHandler
        {

            public CachedModPicDownloader()
            {

            }

            @Override
            public boolean canHandleRequest(Request data)
            {
                return true;
            }

            @Override
            public RequestHandler.Result load(Request request, int networkPolicy) throws IOException
            {
                InputStream in = null;

                String uri = request.uri.toString();
                String image = null;
                if (uri.contains(":")) // Only pngs in zips supported for now
                {
                    String[] split = uri.split(":");
                    if (split.length == 2)
                    {
                        String zipFile = split[0];
                        String imageFile = split[1];
                        log.log(D, "zip = " + zipFile + " file = " + imageFile);

                        String imageCache = activity.getCacheDir().toString() + "/" + new File(zipFile).getName() + "/" + imageFile;
                        log.log(D, "cache = " + imageCache);
                        if (new File(imageCache).exists())
                        {
                            // Found cached image
                            image = imageCache;
                        } else // Not cached, try to generate
                        {
                            if (TitlePicFinder.extractFile(zipFile, imageFile, imageCache) == true) ;
                            {
                                image = imageCache;
                            }
                        }
                    }
                }

                if (image != null)
                {
                    in = new FileInputStream(image);
                } else
                {
                    in = activity.getResources().openRawResource(+R.drawable.questionmark);
                }
                return new Result(in, Picasso.LoadedFrom.DISK);
            }
        }
    */
    private void editItem(final int pos)
    {
        final SuperModItem item = itemsSorted.get(pos);

        final Dialog dialog = new Dialog(activity);
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
                                            //itemsSorted.remove(pos);
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
                                                     item.gameTypeImage = imageOverride;

                                                 };

                                                 new FileSelectDialog(activity, callback, AppInfo.getAppDirectory(), new String[]{".png", ".jpg"}, false);

                                             }); dialog.show();
    }
}
