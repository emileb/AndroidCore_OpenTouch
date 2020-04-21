package com.opentouchgaming.androidcore.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.AppSettings;
import com.opentouchgaming.androidcore.DirectoryChooserDialog;
import com.opentouchgaming.androidcore.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emile on 31/10/2017.
 */

public class OptionsDialog {

    TextView appDirTextView;
    TextView appSecDirTextView;

    Activity activity;

    public OptionsDialog(final Activity act, View extraOptions) {
        activity = act;

        final Dialog dialog = new Dialog(act);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_main_options);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dismiss();
            }
        });

        appDirTextView = dialog.findViewById(R.id.app_dir_textview);
        appSecDirTextView = dialog.findViewById(R.id.appSec_dir_textview);
        CheckBox immersiveCheck = dialog.findViewById(R.id.immersive_mode_checkbox);
        CheckBox cutoutCheckbox = dialog.findViewById(R.id.expand_cutout_checkbox);
        CheckBox altTouchCodeCheck = dialog.findViewById(R.id.alt_touch_code_checkbox);
        Spinner resSpinnder = dialog.findViewById(R.id.res_div_spinner);
        CheckBox hideGameMenu = dialog.findViewById(R.id.hide_game_menu_checkBox);
        CheckBox useSystemKeyboard = dialog.findViewById(R.id.use_system_keyboard_checkBox);
        CheckBox useMouse = dialog.findViewById(R.id.capture_mouse_checkBox);
        CheckBox groupSimilar = dialog.findViewById(R.id.group_similar_engines_checkBox);
        CheckBox enableVibrate = dialog.findViewById(R.id.enable_vibrate_checkBox);
        ImageView appDirButton = dialog.findViewById(R.id.app_dir_options_button);
        ImageView appSecDirButton = dialog.findViewById(R.id.appSec_dir_options_button);

        // PRIMARY folder options
        appDirButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, appDirButton);
            popup.getMenuInflater().inflate(R.menu.app_dir_popup, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.reset) {
                    AppInfo.setAppDirectory(null); //This resets it
                } else if (item.getItemId() == R.id.sdcard) {

                    // Primary always need to be on the writable SD card area
                    if (AppInfo.sdcardWritable != null) {
                        AppInfo.setAppDirectory(AppInfo.sdcardWritable);
                    } else
                        Toast.makeText(activity, "Did not detect SD card", Toast.LENGTH_LONG).show();

                } else if (item.getItemId() == R.id.choose) {
                    DirectoryChooserDialog directoryChooserDialog =
                            new DirectoryChooserDialog(activity,
                                    chosenDir -> updateAppDir(chosenDir));

                    directoryChooserDialog.chooseDirectory(AppInfo.getAppDirectory());
                }

                updateUI();

                return true;
            });

            popup.show();
        });

        // SECONDARY folder options
        appSecDirButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, appDirButton);

            if (AppInfo.isScoped()) {

                popup.getMenuInflater().inflate(R.menu.app_sec_dir_popup_scoped, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {

                    if (item.getItemId() == R.id.choose_saf) {
                        new ScopedStorageDialog(activity, () -> {
                            updateUI();
                        });
                    }
                    if (item.getItemId() == R.id.choose_folder) {
                        DirectoryChooserDialog directoryChooserDialog =
                                new DirectoryChooserDialog(activity,
                                        chosenDir -> updateAppSecDir(chosenDir));

                        directoryChooserDialog.chooseDirectory(AppInfo.getAppDirectory());
                    }

                    updateUI();

                    return true;
                });
            } else {
                popup.getMenuInflater().inflate(R.menu.app_sec_dir_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.reset) {
                        AppInfo.setAppSecDirectory(null); //This resets it
                    } else if (item.getItemId() == R.id.sdcard) {

                        if (AppInfo.sdcardRoot != null) {
                            if (AppInfo.isScoped()) { // Scoped storage can only read here now..
                                AppInfo.setAppSecDirectory(AppInfo.sdcardWritable);
                            } else {
                                AppInfo.setAppSecDirectory(AppInfo.sdcardRoot);
                            }
                        } else
                            Toast.makeText(activity, "Did not detect SD card", Toast.LENGTH_LONG).show();

                    } else if (item.getItemId() == R.id.internal) {
                        AppInfo.setAppSecDirectory(AppInfo.flashRoot);
                    } else if (item.getItemId() == R.id.choose) {
                        DirectoryChooserDialog directoryChooserDialog =
                                new DirectoryChooserDialog(activity,
                                        chosenDir -> updateAppSecDir(chosenDir));

                        directoryChooserDialog.chooseDirectory(AppInfo.getAppSecDirectory());
                    }

                    updateUI();

                    return true;
                });
            }
            popup.show();
        });

/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            sdcardDir.setOnClickListener(v -> {
                File[] files = activity.getExternalFilesDirs(null);

                if ((files.length < 2) || (files[1] == null)) {
                    showError("Can not find an external SD Card, is the card inserted?");
                    return;
                }

                final String path = files[1].toString();

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setTitle("WARNING");
                dialogBuilder.setMessage("This will use the special location on the external SD Card which can be written to by this app, Android will DELETE this"
                        + " area when you uninstall the app and you will LOSE YOUR SAVEGAMES and game data!");
                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        updateAppDir(path);
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {

                    }
                });

                final AlertDialog errdialog = dialogBuilder.create();
                errdialog.show();
            });
        } else {
            sdcardDir.setVisibility(View.GONE);
        }
*/
        // ----

        immersiveCheck.setChecked(AppSettings.getBoolOption(activity, "immersive_mode", false));
        immersiveCheck.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(activity, "immersive_mode", isChecked));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            immersiveCheck.setVisibility(View.GONE);
        }

        // ----

        cutoutCheckbox.setChecked(AppSettings.getBoolOption(activity, "expand_cutout", false));
        cutoutCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppSettings.setBoolOption(activity, "expand_cutout", isChecked);
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            cutoutCheckbox.setVisibility(View.GONE);
        }

        // ----

        altTouchCodeCheck.setChecked(AppSettings.getBoolOption(activity, "alt_touch_code", false));
        altTouchCodeCheck.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(activity, "alt_touch_code", isChecked));

        // ----

        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(act, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resSpinnder.setAdapter(dataAdapter);
        resSpinnder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                AppSettings.setIntOption(act, "res_div", position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int selected = AppSettings.getIntOption(act, "res_div", 1);
        resSpinnder.setSelection(selected - 1);

        // ----

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            useMouse.setChecked(AppSettings.getBoolOption(act, "use_mouse", true));
            useMouse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    AppSettings.setBoolOption(act, "use_mouse", isChecked);
                }
            });
        } else {
            useMouse.setVisibility(View.GONE);
        }


        hideGameMenu.setChecked(AppSettings.getBoolOption(act, "hide_game_menu_touch", AppInfo.isAndroidTv));
        hideGameMenu.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(act, "hide_game_menu_touch", isChecked));


        useSystemKeyboard.setChecked(AppSettings.getBoolOption(act, "use_system_keyboard", false));
        useSystemKeyboard.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(act, "use_system_keyboard", isChecked));


        groupSimilar.setChecked(AppSettings.getBoolOption(act, "group_similar_engines", false));
        groupSimilar.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(act, "group_similar_engines", isChecked));


        enableVibrate.setChecked(AppSettings.getBoolOption(act, "enable_vibrate", true));
        enableVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(act, "enable_vibrate", isChecked));

        if (extraOptions != null) {
            LinearLayout layout = dialog.findViewById(R.id.top_linearlayout);
            layout.addView(extraOptions);
        }

        updateUI();

        dialog.show();
    }

    private void updateUI() {
        appDirTextView.setText(AppInfo.replaceRootPaths(AppInfo.getAppDirectory()));
        appSecDirTextView.setText(AppInfo.replaceRootPaths(AppInfo.getAppSecDirectory()));
    }

    public void dismiss() {
        //Override me
    }

    private void updateAppDir(String dir) {
        File fdir = new File(dir);

        if (!fdir.isDirectory()) {
            showError(dir + " is not a directory");
            return;
        }

        if (!fdir.canWrite()) {
            showError(dir + " is not a writable");
            return;
        }

        //Test CAN actually write, the above canWrite can pass on KitKat SD cards WTF GOOGLE
        File test_write = new File(dir, "test_write");
        try {
            test_write.createNewFile();
            if (!test_write.exists()) {
                showError(dir + " is not a writable");
                return;
            }
        } catch (IOException e) {
            showError(dir + " is not a writable");
            return;
        }
        test_write.delete();

        if (dir.contains(" ")) {
            showError(dir + " must not contain any spaces");
            return;
        }

        AppInfo.setAppDirectory(dir);

        updateUI();
    }

    private void updateAppSecDir(String dir) {
        File fdir = new File(dir);

        if (!fdir.isDirectory()) {
            showError(dir + " is not a directory");
            return;
        }

        if (dir.contains(" ")) {
            showError(dir + " must not contain any spaces");
            return;
        }

        AppInfo.setAppSecDirectory(dir);

        updateUI();
    }


    private void showError(String error) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(error);
        dialogBuilder.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog errdialog = dialogBuilder.create();
        errdialog.show();
    }
}
