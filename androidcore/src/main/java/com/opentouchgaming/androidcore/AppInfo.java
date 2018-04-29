package com.opentouchgaming.androidcore;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Emile on 03/08/2017.
 */

public class AppInfo
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.CORE, "AppInfo");
    }

    public enum Apps
    {
        DELTA_TOUCH,
        ALPHA_TOUCH,
    }

    public static Apps app;
    public static String title;
    public static String internalFiles;
    public static String directory;

    public static GameEngine currentEngine;

    private static Context context;

    static public void setAppInfo(Context ctx, Apps app, String title, String directory)
    {
        AppInfo.context = ctx;
        AppInfo.app = app;
        AppInfo.title = title;
        AppInfo.directory = directory;
        AppInfo.internalFiles = ctx.getFilesDir().getAbsolutePath();
    }

    static public void setAppDirectory(String appDir)
    {
        AppSettings.setStringOption(context, "app_dir", appDir);
    }

    static public String getAppDirectory()
    {
        String appDir = AppSettings.getStringOption(context, "app_dir", null);
        if (appDir == null)
        {
            appDir = Environment.getExternalStorageDirectory().toString() + "/OpenTouch/" + directory;
            AppSettings.setStringOption(context, "app_dir", appDir);
        }

        // Check if exists, create if not
        File file = new File(appDir);
        if (!file.exists())
        {
            if( !file.mkdirs() )
            {
                log.log(DebugLog.Level.E, "Did not create base folder");
            }

            File f = new File(appDir, "temp_");
            try {
                f.createNewFile();
                new SingleMediaScanner(context, false,  f.getAbsolutePath());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            new File(appDir, "temp_").delete();
        }

        return appDir;
    }

    // JNI
    static public Context getContext()
    {
        return context;
    }

    // JNI
    static public String getFilesDir()
    {
        return context.getFilesDir().getAbsolutePath();
    }
}
