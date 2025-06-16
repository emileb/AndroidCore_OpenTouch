package com.opentouchgaming.androidcore.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import com.opentouchgaming.androidcore.R;
import com.opentouchgaming.androidcore.Utils;

public class FullscreenDialog extends Dialog
{

    public FullscreenDialog(Context context)
    {
        super(context, R.style.DialogFullscreenAnimate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // This is where we adjust the dialog's size
        if (getWindow() != null)
        {
            Utils.expandToCutout(getContext(), getWindow(), OptionsDialogKt.LAUNCHER_EXPAND_INTO_NOTCH);
            Utils.setImmersionMode(getContext(), getWindow(), OptionsDialogKt.LAUNCHER_HIDE_NAV_BAR);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        /*
        if (getWindow() != null) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

            // Get screen dimensions
            // For newer APIs (API 30+), you might prefer WindowMetrics
            // For older APIs, DisplayMetrics is common
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            int screenWidth;
            int screenHeight;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // API 30 and above
                WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
                screenWidth = metrics.getBounds().width();
                screenHeight = metrics.getBounds().height();
            } else {
                // Deprecated for newer APIs, but still widely used for compatibility
                DisplayMetrics displayMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                screenWidth = displayMetrics.widthPixels;
                screenHeight = displayMetrics.heightPixels;
            }

            // Set dialog width to 90% of screen width
            layoutParams.width = (int) (screenWidth * 0.95);
            // Set dialog height to 90% of screen height
            layoutParams.height = (int) (screenHeight * 0.95);

            getWindow().setAttributes(layoutParams);
        }

         */
    }
}
