package com.opentouchgaming.androidcore;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emile on 31/10/2017.
 */

public class OptionsDialog
{

    TextView appDirTextView;

    Activity activity;

    public OptionsDialog(final Activity act, View extraOptions)
    {
        activity = act;

        final Dialog dialog = new Dialog(act);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        dialog.setTitle("Game options");
        dialog.setContentView(R.layout.dialog_main_options);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                dismiss();
            }
        });

        appDirTextView = (TextView) dialog.findViewById(R.id.app_dir_textview);
        appDirTextView.setText(AppInfo.getAppDirectory());

        Button chooseDir = (Button) dialog.findViewById(R.id.choose_base_button);
        chooseDir.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                DirectoryChooserDialog directoryChooserDialog =
                        new DirectoryChooserDialog(activity,
                                new DirectoryChooserDialog.ChosenDirectoryListener()
                                {
                                    @Override
                                    public void onChosenDir(String chosenDir)
                                    {
                                        updateBaseDir(chosenDir);
                                    }
                                });

                directoryChooserDialog.chooseDirectory(AppInfo.getAppDirectory());
            }
        });

        Button resetDir = (Button) dialog.findViewById(R.id.reset_base_button);
        resetDir.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                AppInfo.setAppDirectory(null); //Thsi resets it
                appDirTextView.setText(AppInfo.getAppDirectory());
            }
        });


        Button sdcardDir = (Button) dialog.findViewById(R.id.sdcard_base_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            sdcardDir.setOnClickListener(new View.OnClickListener()
            {

                @TargetApi(Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v)
                {
                    File[] files = activity.getExternalFilesDirs(null);

                    if ((files.length < 2) || (files[1] == null))
                    {
                        showError("Can not find an external SD Card, is the card inserted?");
                        return;
                    }

                    final String path = files[1].toString();

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setTitle("WARNING");
                    dialogBuilder.setMessage("This will use the special location on the external SD Card which can be written to by this app, Android will DELETE this"
                            + " area when you uninstall the app and you will LOSE YOUR SAVEGAMES and game data!");
                    dialogBuilder.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            updateBaseDir(path);
                        }
                    });
                    dialogBuilder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });

                    final AlertDialog errdialog = dialogBuilder.create();
                    errdialog.show();

                }
            });
        } else
        {
            sdcardDir.setVisibility(View.GONE);
        }

        CheckBox immersiveCheck = (CheckBox) dialog.findViewById(R.id.immersive_mode_checkbox);
        immersiveCheck.setChecked(AppSettings.getBoolOption(activity, "immersive_mode", false));

        immersiveCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                AppSettings.setBoolOption(activity, "immersive_mode", isChecked);
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
        {
            immersiveCheck.setVisibility(View.GONE);
        }


        Spinner resSpinnder = (Spinner) dialog.findViewById(R.id.res_div_spinner);
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(act, android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        resSpinnder.setAdapter(dataAdapter);
        resSpinnder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id)
            {
                AppSettings.setIntOption(act, "res_div", position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        int selected = AppSettings.getIntOption(act, "res_div", 1);
        resSpinnder.setSelection(selected - 1);


        if (extraOptions != null)
        {
            LinearLayout layout = dialog.findViewById(R.id.top_linearlayout);
            layout.addView(extraOptions);
        }
        dialog.show();

    }

    public void dismiss()
    {
        //Override me
    }

    private void updateBaseDir(String dir)
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
        appDirTextView.setText(AppInfo.getAppDirectory());
    }

    private void showError(String error)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(error);
        dialogBuilder.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        final AlertDialog errdialog = dialogBuilder.create();
        errdialog.show();
    }
}
