package org.libsdl.app2010;

import android.content.Intent;
import android.util.Log;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.Utils;
import com.opentouchgaming.androidcore.controls.TouchSettings;

import org.libsdl.app.NativeLib;

import java.io.File;

public class OpenTouchSDL {

    static public void launch(Intent intent, int width, int height, String nativeLibs)
    {
        String args = intent.getStringExtra("args");

        args = args.replace("$W", Integer.toString(width));
        args = args.replace("$H", Integer.toString(height));

        String[] args_array = Utils.creatArgs(args);

        String gamePath = intent.getStringExtra("game_path");

        int options = 0;
        if (TouchSettings.gamepadHidetouch)
            options |= TouchSettings.GAME_OPTION_AUTO_HIDE_GAMEPAD;

        if (TouchSettings.hideGameAndMenuTouch)
            options |= TouchSettings.GAME_OPTION_HIDE_MENU_AND_GAME;

        if (TouchSettings.useSystemKeyboard)
            options |= TouchSettings.GAME_OPTION_USE_SYSTEM_KEYBOARD;

        if( intent.getBooleanExtra("use_gles2", false))
            options |= TouchSettings.GAME_OPTION_GLES2;

        int gameType = intent.getIntExtra("game_type", 0);
        //NativeLib.setScreenSize(1920,1104);
        //NativeLib.setScreenSize(1280,736);
        String logFilename = intent.getStringExtra("log_filename");

        String nativeSoPath = nativeLibs;

        File folder = new File(nativeSoPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                Log.v("SDL","File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                Log.v("SDL","Directory " + listOfFiles[i].getName());
            }
        }
        Log.v("SDL", "Native .so path = " + nativeSoPath);
        int ret = NativeLib.init(AppInfo.internalFiles + "/", options, args_array, gameType, gamePath, logFilename,nativeSoPath);

        Log.v("SDL", "SDL thread terminated");
    }

}
