package com.opentouchgaming.androidcore.ui;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;

import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.R;


public class StorageConfigDialog
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.LICENSE, "StorageConfigDialog");
    }

    Activity activity;

    public StorageConfigDialog(final Activity act)
    {
        activity = act;

        final Dialog dialog = new Dialog(act, R.style.DialogThemeFullscreen);
        //final Dialog dialog = new Dialog(act);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_storage_config);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);


        dialog.show();
    }



}
