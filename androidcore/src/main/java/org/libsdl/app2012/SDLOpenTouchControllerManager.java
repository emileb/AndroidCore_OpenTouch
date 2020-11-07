package org.libsdl.app2012;

import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SDLOpenTouchControllerManager extends SDLGenericMotionListener_API12 {

    private boolean mRelativeModeEnabled;

    @Override
    public boolean onGenericMotion(View v, MotionEvent event) {
        return SDLOpenTouch.controlInterp.onGenericMotionEvent(event);
    }

    public boolean supportsRelativeMouse() {
        return true;
    }

    @Override
    public boolean inRelativeMode() {
        return mRelativeModeEnabled;
    }

    public boolean setRelativeMouseEnabled(boolean enabled) {
        if (!SDLActivity.isDeXMode() && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            if (enabled) {
                Log.d("TEST", "MOUSE ENABLED");
                SDLActivity.getContentView().requestPointerCapture();
            }
            else {
                Log.d("TEST", "MOUSE DISABLE");
                SDLActivity.getContentView().releasePointerCapture();
            }
            mRelativeModeEnabled = enabled;
            return true;
        }
        else
        {
            return false;
        }
    }
}
