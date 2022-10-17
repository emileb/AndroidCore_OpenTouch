package com.opentouchgaming.androidcore.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.AppSettings;
import com.opentouchgaming.androidcore.R;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;

/**
 * Created by Emile on 31/10/2017.
 */

public class OptionsDialog
{
    Activity activity;
    Runnable update;
    BubbleSeekBar screenDivSeek;

    ArrayList<Pair<String, Float>> resolutions = new ArrayList<>();

    boolean dontUpdate = false;

    public OptionsDialog(final Activity act, View extraOptions, Runnable update)
    {
        activity = act;
        this.update = update;

        final Dialog dialog = new Dialog(act);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_main_options);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        dialog.setOnDismissListener(dialog12 ->
        {
            if (!dontUpdate)
                update.run();
        });

        CheckBox immersiveCheck = dialog.findViewById(R.id.immersive_mode_checkbox);
        CheckBox cutoutCheckbox = dialog.findViewById(R.id.expand_cutout_checkbox);
        CheckBox altTouchCodeCheck = dialog.findViewById(R.id.alt_touch_code_checkbox);

        CheckBox hideGameMenu = dialog.findViewById(R.id.hide_game_menu_checkBox);
        CheckBox useSystemKeyboard = dialog.findViewById(R.id.use_system_keyboard_checkBox);
        CheckBox useMouse = dialog.findViewById(R.id.capture_mouse_checkBox);
        CheckBox groupSimilar = dialog.findViewById(R.id.group_similar_engines_checkBox);
        CheckBox enableVibrate = dialog.findViewById(R.id.enable_vibrate_checkBox);

        Button storagebutton = dialog.findViewById(R.id.storage_button);
        Button resetButton = dialog.findViewById(R.id.reset_button);
        screenDivSeek = dialog.findViewById(R.id.screenDiv_bubbleSeek);

        resolutions.add(new Pair<>("100%", 1.0f));
        resolutions.add(new Pair<>("75%", 0.75f));
        resolutions.add(new Pair<>("60%", 0.6f));
        resolutions.add(new Pair<>("50%", 0.5f));
        resolutions.add(new Pair<>("30%", 0.30f));
        resolutions.add(new Pair<>("25%", 0.25f));

        screenDivSeek.setCustomSectionTextArray((sectionCount, array) ->
        {
            array.clear();

            int n = 0;
            for (Pair<String, Float> res : resolutions)
            {
                array.put(n++, res.first);
            }
            return array;
        });

        storagebutton.setOnClickListener(v ->
        {
            StorageConfigDialog scd = new StorageConfigDialog(act, AppInfo.storageExamples, update);
            dialog.dismiss();
        });
        // ----

        immersiveCheck.setChecked(AppSettings.getBoolOption(activity, "immersive_mode", false));
        immersiveCheck.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(activity, "immersive_mode", isChecked));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
        {
            immersiveCheck.setVisibility(View.GONE);
        }

        // ----

        cutoutCheckbox.setChecked(AppSettings.getBoolOption(activity, "expand_cutout", false));
        cutoutCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(activity, "expand_cutout", isChecked));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
        {
            cutoutCheckbox.setVisibility(View.GONE);
        }

        // ----

        altTouchCodeCheck.setChecked(AppSettings.getBoolOption(activity, "alt_touch_code", false));
        altTouchCodeCheck.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(activity, "alt_touch_code", isChecked));

        // ----

        // Search for the value to find the seekbar position
        float multiplier = AppSettings.getFloatOption(act, "res_div_float", 1);
        int seekPos = 0;
        for (Pair<String, Float> res : resolutions)
        {
            if (res.second == multiplier)
                break;
            else
                seekPos++;
        }

        if (seekPos >= resolutions.size())
            seekPos = 0;

        screenDivSeek.setProgress(seekPos);

        screenDivSeek.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener()
        {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser)
            {
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat)
            {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser)
            {
                float multiplier = resolutions.get(progress).second;

                Log.d("test", "multipiler = " + multiplier);
                AppSettings.setFloatOption(act, "res_div_float", multiplier);
            }
        });

        // ----

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            useMouse.setChecked(AppSettings.getBoolOption(act, "use_mouse", true));
            useMouse.setOnCheckedChangeListener((buttonView, isChecked) -> AppSettings.setBoolOption(act, "use_mouse", isChecked));
        }
        else
        {
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

        Spinner spinner = dialog.findViewById(R.id.audio_spinner);

        String[] paths;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            paths = new String[]{"OpenSL (Default)", "Audio Tack (Old)", "AAudio (low latency)"};
        }
        else
        {
            paths = new String[]{"OpenSL (Default)", "Audio Tack (Old)"};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, paths);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(AppSettings.getIntOption(act, "sdl_audio_backend", 0));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                AppSettings.setIntOption(act, "sdl_audio_backend", position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        resetButton.setOnClickListener(v ->
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle("WARNING: This will reset all app settings!");
            dialogBuilder.setPositiveButton("OK", (dialog1, which) ->
            {
                dontUpdate = true;
                AppSettings.deleteAllOptions(act);
                dialog.dismiss();
                act.finish();
            });

            dialogBuilder.create().show();
        });


        if (extraOptions != null)
        {
            LinearLayout layout = dialog.findViewById(R.id.extras_linearlayout);
            layout.addView(extraOptions);
        }

        updateUI();

        dialog.show();
    }

    private void updateUI()
    {

    }
}
