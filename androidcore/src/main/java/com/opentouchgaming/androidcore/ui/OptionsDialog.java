package com.opentouchgaming.androidcore.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.AppSettings;
import com.opentouchgaming.androidcore.R;
import com.xw.repo.BubbleSeekBar;

/**
 * Created by Emile on 31/10/2017.
 */

public class OptionsDialog {

    Activity activity;
    Runnable update;
    BubbleSeekBar screenDivSeek;

    public OptionsDialog(final Activity act, View extraOptions, Runnable update) {
        activity = act;
        this.update = update;

        final Dialog dialog = new Dialog(act);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_main_options);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        dialog.setOnDismissListener(dialog12 -> update.run());

        CheckBox immersiveCheck = dialog.findViewById(R.id.immersive_mode_checkbox);
        CheckBox cutoutCheckbox = dialog.findViewById(R.id.expand_cutout_checkbox);
        CheckBox altTouchCodeCheck = dialog.findViewById(R.id.alt_touch_code_checkbox);

        CheckBox hideGameMenu = dialog.findViewById(R.id.hide_game_menu_checkBox);
        CheckBox useSystemKeyboard = dialog.findViewById(R.id.use_system_keyboard_checkBox);
        CheckBox useMouse = dialog.findViewById(R.id.capture_mouse_checkBox);
        CheckBox groupSimilar = dialog.findViewById(R.id.group_similar_engines_checkBox);
        CheckBox enableVibrate = dialog.findViewById(R.id.enable_vibrate_checkBox);
        CheckBox oldSDLAudio = dialog.findViewById(R.id.sdl_old_audio_checkBox);

        Button storagebutton = dialog.findViewById(R.id.storage_button);
        Button resetButton = dialog.findViewById(R.id.reset_button);
        screenDivSeek =  dialog.findViewById(R.id.screenDiv_bubbleSeek);

        screenDivSeek.setCustomSectionTextArray(new BubbleSeekBar.CustomSectionTextArray() {
            @NonNull
            @Override
            public SparseArray<String> onCustomize(int sectionCount, @NonNull SparseArray<String> array) {
                array.clear();
                array.put(0, "100%");
                array.put(1, "50%");
                array.put(2, "33%");
                array.put(3, "25%");
                return array;
            }
        });

        storagebutton.setOnClickListener(v -> {
            StorageConfigDialog scd = new StorageConfigDialog(act, AppInfo.storageExamples, update);
            dialog.dismiss();
        });
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

        int selected = AppSettings.getIntOption(act, "res_div", 1);
        screenDivSeek.setProgress(selected);

        screenDivSeek.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                AppSettings.setIntOption(act,"res_div", progress);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

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

        oldSDLAudio.setChecked(AppSettings.getBoolOption(act, "old_sdl_audio", false));
        oldSDLAudio.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(act, "old_sdl_audio", isChecked));

        resetButton.setOnClickListener(v -> {

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle("WARNING: This will reset all app settings!");
            dialogBuilder.setPositiveButton("OK", (dialog1, which) -> {
                AppSettings.deleteAllOptions(act);
                dialog.dismiss();
            });

            dialogBuilder.create().show();
        });


        if (extraOptions != null) {
            LinearLayout layout = dialog.findViewById(R.id.extras_linearlayout);
            layout.addView(extraOptions);
        }

        updateUI();

        dialog.show();
    }

    private void updateUI() {

    }
}
