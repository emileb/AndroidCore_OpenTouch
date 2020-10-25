package com.opentouchgaming.androidcore;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.opentouchgaming.androidcore.controls.GamePadFragment;

public class GamepadActivity extends Activity
{
    GamePadFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AppInfo.Apps app = AppInfo.Apps.valueOf(getIntent().getStringExtra("app"));
        AppInfo.setApp(app);

        if(AppInfo.getContext() == null)
            AppInfo.setContext(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamepad);

        fragment = (GamePadFragment) getFragmentManager().findFragmentById(R.id.gamepad_fragment_container);
    }


    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        return fragment.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (fragment.onKeyDown(keyCode, event))
            return true;
        else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if ( fragment.onKeyUp(keyCode, event))
            return true;
        else
            return super.onKeyUp(keyCode, event);

    }
}
