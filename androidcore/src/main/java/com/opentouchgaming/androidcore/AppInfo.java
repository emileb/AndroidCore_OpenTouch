package com.opentouchgaming.androidcore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.opentouchgaming.androidcore.ui.tutorial.Tutorial;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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

    public static String flashRoot; // Root of internal flash
    public static String sdcardRoot; // Root of the SD card
    public static String sdcardWritable; // WRITABLE area on the SD dard


    public static String packageId;
    public static String emailAddress;
    public static String key;

    public static boolean isAndroidTv;

    public static GameEngine gameEngines[];

    public static GameEngine currentEngine;

    public static ArrayList<Tutorial> tutorials = new ArrayList<>();

    private static Context context;

    private static final int SCOPED_VERSION = 30;

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


        AppInfo.flashRoot = Environment.getExternalStorageDirectory().toString();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File files[] = context.getExternalFilesDirs(null);
            log.log(DebugLog.Level.D,"files lenth = " + files.length);
            if( files != null && files.length > 1 && files[1] != null) {

                if( !files[1].exists())
                    files[1].mkdirs();

                AppInfo.sdcardWritable = files[1].getAbsolutePath();
                AppInfo.sdcardRoot = AppInfo.sdcardWritable.substring(0, AppInfo.sdcardWritable.indexOf("/Android/data"));
            }
        }
        log.log(DebugLog.Level.D, "Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
        log.log(DebugLog.Level.D, "flashRoot = " + flashRoot);
        log.log(DebugLog.Level.D, "sdcardRoot = " + sdcardRoot);
        log.log(DebugLog.Level.D, "sdcardWritable = " + sdcardWritable);
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

    static public boolean isScoped()
    {
        return  (Build.VERSION.SDK_INT >= SCOPED_VERSION);
    }

    static public void setAppDirectory(String appDir)
    {
        AppSettings.setStringOption(context, "app_dir", appDir);
    }

    static public void setAppSecDirectory(String appDir)
    {
        AppSettings.setStringOption(context, "app_sec_dir", appDir);
    }


    // PRIMARY DEFAULT
    static public String getDefaultAppDirectory()
    {
        if (isScoped() == false) {
            return flashRoot + "/OpenTouch/" + directory;
        }
        else // Android R!!!! FUCKK
        {
            File files[] = context.getExternalFilesDirs(null);

            if( !files[0].exists())
                files[0].mkdirs();

            return files[0].getAbsolutePath();
        }
    }

    // SECONDARY DEFAULT
    static public String getDefaultAppSecDirectory()
    {
        if( sdcardRoot != null ) {
            if (isScoped() == false) {
                return sdcardRoot + "/OpenTouch/" + directory;
            } else {
                return sdcardWritable;
            }
        }
        else
        {
            return null;
        }
    }

    static public String getAppDirectory( String fileInfo)
    {
        String appDir = AppSettings.getStringOption(context, "app_dir", null);
        if (appDir == null)
        {
            appDir = getDefaultAppDirectory();
            AppSettings.setStringOption(context, "app_dir", appDir);
        }

        Utils.mkdirs( AppInfo.getContext(), appDir,fileInfo);

        return appDir;
    }

    static public String getAppDirectory()
    {
        return getAppDirectory( null);
    }

    static public String getAppSecDirectory()
    {
        String appDir = AppSettings.getStringOption(context, "app_sec_dir", null);
        if (appDir == null)
        {
            appDir = getDefaultAppSecDirectory();
            AppSettings.setStringOption(context, "app_sec_dir", appDir);
        }

        //if (appDir == null)
        //    appDir = "/";

        return appDir;
    }

    static public String replaceRootPaths(String path)
    {
        if( path != null) {
            String ret = path.replace(flashRoot, "<Internal>");
            if (sdcardRoot != null) {
                ret = ret.replace(sdcardRoot, "<SD-Card>");
            }
            return ret;
        }
        else
        {
            return "Not set";
        }
    }

    static public String hideAppPaths(String path)
    {
        String appPath = getAppDirectory();
        String appSecPath = getAppSecDirectory();

        String ret = path.replace(appPath + "/", "");

        if( appSecPath != null) {
            ret = ret.replace(appSecPath + "/", "");
        }
        return ret;
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
