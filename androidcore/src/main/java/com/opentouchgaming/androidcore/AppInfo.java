package com.opentouchgaming.androidcore;

import android.content.Context;
import android.os.Environment;

import com.opentouchgaming.androidcore.ui.tutorial.Tutorial;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
        MOD_ENGINE,
        DELTA_TOUCH,
        ALPHA_TOUCH,
        QUAD_TOUCH
    }

    public static Apps app;
    public static String title;
    public static String internalFiles;
    public static String cacheFiles;
    public static String directory;

    public static String packageId;
    public static String emailAddress;
    public static String key;

    public static boolean isAndroidTv;

    public static GameEngine gameEngines[];

    public static GameEngine currentEngine;

    public static ArrayList<Tutorial> tutorials = new ArrayList<>();

    private static Context context;

    static public void setAppInfo(Context ctx, Apps app, String title, String directory, String pkg, String email,  boolean isAndroidTv )
    {
        AppInfo.context = ctx;
        AppInfo.app = app;
        AppInfo.title = title;
        AppInfo.directory = directory;
        AppInfo.internalFiles = ctx.getFilesDir().getAbsolutePath();
        AppInfo.cacheFiles =  ctx.getCacheDir().toString();

        AppInfo.packageId = pkg;
        AppInfo.emailAddress = email;

        AppInfo.isAndroidTv = isAndroidTv;
    }

    static public GameEngine getGameEngine( GameEngine.Engine type )
    {
        GameEngine ret = null;
        for( GameEngine e : gameEngines )
        {
            if( e.engine == type )
            {
                ret = e;
                break;
            }
        }
        return ret;
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

    static public String getGamepadDirectory()
    {
        return AppInfo.internalFiles + "/gamepad/";
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
