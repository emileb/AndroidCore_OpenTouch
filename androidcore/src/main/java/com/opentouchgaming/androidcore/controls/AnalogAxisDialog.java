package com.opentouchgaming.androidcore.controls;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.SeekBar;

import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.R;

/**
 * Created by Emile on 31/10/2017.
 */

public class AnalogAxisDialog implements ActionInput.ActionInputExtra {
    static DebugLog log;

    static {
        log = new DebugLog(DebugLog.Module.CONTROLS, "AnalogAxisDialog");
    }

    Activity activity;


    public AnalogAxisDialog() {

    }

    public void dismiss() {
        //Override me
    }

    @Override
    public void show(Activity activity, ActionInput action,Runnable runnable) {
        log.log(DebugLog.Level.D, "SHOW");

        this.activity = activity;

        final Dialog dialog = new Dialog(activity);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_analog_axis);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        final SeekBar sensitivity = dialog.findViewById(R.id.sensitivity_seekBar);
        final SeekBar deadZone = dialog.findViewById(R.id.dead_zone_seekBar);
        final CheckBox invert = dialog.findViewById(R.id.invert_checkBox);

        invert.setChecked(action.invert);

        sensitivity.setMax(150);
        sensitivity.setProgress((int) (action.scale * 50));

        deadZone.setMax(95);
        deadZone.setProgress((int) ((action.deadZone) * 100) - 5);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                action.scale = (float) sensitivity.getProgress() / 50.f;
                action.invert = invert.isChecked();
                action.deadZone = (float) (deadZone.getProgress() + 5) / 100.f;
                runnable.run();
            }
        });


        dialog.show();
    }

}
