package com.opentouchgaming.androidcore.controls;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import com.opentouchgaming.androidcore.AppInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class TouchSettings
{
    public static final int GAME_OPTION_AUTO_HIDE_GAMEPAD = 0x1;
    public static final int GAME_OPTION_HIDE_MENU_AND_GAME = 0x2;
    public static final int GAME_OPTION_USE_SYSTEM_KEYBOARD = 0x4;
    public static final int GAME_OPTION_GLES2 = 0x8;
    public static final int GAME_OPTION_GLES3 = 0x10;
    public static final int GAME_OPTION_SDL_OLD_AUDIO = 0x20;
    public static final int GAME_OPTION_GL4ES = 0x40;
    public static final int GAME_OPTION_SDL_AAUDIO_AUDIO = 0x80;
    public static final int GAME_OPTION_SDL_MIDI_FLUIDSYNTH = 0x100;

    public static boolean DEBUG = true;
    public static String gamePadControlsFile;

    public static boolean gamePadEnabled;
    public static boolean altTouchCode;
    public static boolean gamepadHidetouch;
    public static boolean hideGameAndMenuTouch;
    public static boolean useSystemKeyboard;

    public static void reloadSettings(Context ctx)
    {
        gamePadControlsFile = ctx.getFilesDir().toString() + "/gamepadSettings.dat";
        gamePadEnabled = getBoolOption(ctx, "gamepad_enabled", true);
        altTouchCode = getBoolOption(ctx, "alt_touch_code", false);
        gamepadHidetouch = getBoolOption(ctx, "gamepad_hide_touch", true);
        hideGameAndMenuTouch = getBoolOption(ctx, "hide_game_menu_touch", AppInfo.isAndroidTv); // default to hide on Android TV
        useSystemKeyboard = getBoolOption(ctx, "use_system_keyboard", false);
    }

    public static float getFloatOption(Context ctx, String name, float def)
    {
        SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", Context.MODE_MULTI_PROCESS);
        return settings.getFloat(name, def);
    }

    public static void setFloatOption(Context ctx, String name, float value)
    {
        SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(name, value);
        editor.commit();
    }

    public static boolean getBoolOption(Context ctx, String name, boolean def)
    {
        SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", Context.MODE_MULTI_PROCESS);
        return settings.getBoolean(name, def);
    }

    public static void setBoolOption(Context ctx, String name, boolean value)
    {
        SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }

    public static int getIntOption(Context ctx, String name, int def)
    {
        SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", Context.MODE_MULTI_PROCESS);
        return settings.getInt(name, def);
    }

    public static void setIntOption(Context ctx, String name, int value)
    {
        SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public static long getLongOption(Context ctx, String name, long def)
    {
        SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", Context.MODE_MULTI_PROCESS);
        return settings.getLong(name, def);
    }

    public static void setLongOption(Context ctx, String name, long value)
    {
        SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(name, value);
        editor.commit();
    }

    public static String getStringOption(Context ctx, String name, String def)
    {
        SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", Context.MODE_MULTI_PROCESS);
        return settings.getString(name, def);
    }

    public static void setStringOption(Context ctx, String name, String value)
    {
        SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, value);
        editor.commit();
    }

    static public void copyFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
        out.close();
    }

    static public void copyPNGAssets(Context ctx, String dir, String prefix)
    {

        if (prefix == null)
            prefix = "";

        File d = new File(dir);
        if (!d.exists())
            d.mkdirs();

        AssetManager assetManager = ctx.getAssets();
        String[] files = null;
        try
        {
            files = assetManager.list("");
        }
        catch (IOException e)
        {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files)
        {
            if (filename.endsWith("png") && filename.startsWith(prefix))
            {
                InputStream in = null;
                OutputStream out = null;
                //Log.d("test","file = " + filename);
                try
                {
                    in = assetManager.open(filename);
                    out = new FileOutputStream(dir + "/" + filename.substring(prefix.length()));
                    copyFile(in, out);
                    in.close();
                    in = null;
                    out.flush();
                    out.close();
                    out = null;
                }
                catch (IOException e)
                {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                }
            }
        }
    }
}
