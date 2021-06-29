package com.opentouchgaming.androidcore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.core.util.Pair;

import com.opentouchgaming.androidcore.ui.ScopedStorageDialog;
import com.opentouchgaming.androidcore.ui.StorageConfigDialog;
import com.opentouchgaming.androidcore.ui.tutorial.Tutorial;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emile on 03/08/2017.
 */

public class AppInfo
{
    private static final int SCOPED_VERSION = 30;
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
    public static List<StorageConfigDialog.StorageExamples> storageExamples;
    public static ScopedStorageDialog.Tutorial scopedTutorial;
    public static String website = null;

    public static int defaultAppImage;

    public static boolean showRateButton = true;

    static DebugLog log;
    private static Context context;

    static
    {
        log = new DebugLog(DebugLog.Module.CORE, "AppInfo");
    }

    static public void setApp(Apps app)
    {
        AppInfo.app = app;
    }

    static public void setAppInfo(Context ctx, Apps app, String title, String directory, String pkg, String email, boolean isAndroidTv, int defaultAppImage)
    {
        AppInfo.context = ctx;
        AppInfo.app = app;
        AppInfo.title = title;
        AppInfo.directory = directory;
        AppInfo.internalFiles = ctx.getFilesDir().getAbsolutePath();
        AppInfo.cacheFiles = ctx.getCacheDir().toString();

        AppInfo.packageId = pkg;
        AppInfo.emailAddress = email;

        AppInfo.isAndroidTv = isAndroidTv;
        AppInfo.defaultAppImage = defaultAppImage;

        AppInfo.flashRoot = Environment.getExternalStorageDirectory().toString();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            File files[] = context.getExternalFilesDirs(null);
            if (files != null && files.length > 1 && files[1] != null)
            {

                if (!files[1].exists())
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

    static public GameEngine getGameEngine(GameEngine.Engine type)
    {
        GameEngine ret = null;
        for (GameEngine e : gameEngines)
        {
            if (e.engine == type)
            {
                ret = e;
                break;
            }
        }
        return ret;
    }

    static public void setScoped(boolean enabled)
    {
        AppSettings.setBoolOption(AppInfo.getContext(), "scoped_storage_enabled", enabled);
        setAppDirectory(getDefaultAppDirectory());
        setAppSecDirectory(getDefaultAppSecDirectory());
    }

    static public boolean isScopedAllowed()
    {
        return (Build.VERSION.SDK_INT >= SCOPED_VERSION);
    }

    static public boolean isScopedEnabled()
    {
        if (isScopedAllowed())
        {
            return AppSettings.getBoolOption(AppInfo.getContext(), "scoped_storage_enabled", false);
        }
        else
            return false;
    }

    // PRIMARY DEFAULT
    @SuppressLint("NewApi")
    static public String getDefaultAppDirectory()
    {
        if (isScopedEnabled() == false)
        {
            return flashRoot + "/OpenTouch/" + directory;
        }
        else // Android R!!!! FUCKK
        {
            File files[] = context.getExternalFilesDirs(null);

            if (!files[0].exists())
                files[0].mkdirs();

            return files[0].getAbsolutePath();
        }
    }

    // SECONDARY DEFAULT
    static public String getDefaultAppSecDirectory()
    {
        if (sdcardRoot != null)
        {
            if (isScopedEnabled() == false)
            {
                return sdcardRoot + "/OpenTouch/" + directory;
            }
            else
            {
                return null;
                //return sdcardWritable;
            }
        }
        else
        {
            return null;
        }
    }

    static public String getAppDirectory(String fileInfo)
    {
        String appDir = AppSettings.getStringOption(context, "app_dir", null);
        if (appDir == null)
        {
            appDir = getDefaultAppDirectory();
            AppSettings.setStringOption(context, "app_dir", appDir);
        }

        Utils.mkdirs(AppInfo.getContext(), appDir, fileInfo);

        return appDir;
    }

    static public String getAppDirectory()
    {
        return getAppDirectory(null);
    }

    static public void setAppDirectory(String appDir)
    {
        AppSettings.setStringOption(context, "app_dir", appDir);
    }

    static public String getAppSecDirectory()
    {
        String appDir = AppSettings.getStringOption(context, "app_sec_dir", null);

        if (appDir == null)
        {
            appDir = getDefaultAppSecDirectory();
            AppSettings.setStringOption(context, "app_sec_dir", appDir);
        }

        return appDir;
    }

    static public void setAppSecDirectory(String appDir)
    {
        AppSettings.setStringOption(context, "app_sec_dir", appDir);
    }

    // User files (engine config, savegames, touch control saves)
    static public String getUserFiles()
    {
        String userFiles = getAppDirectory() + "/user_files";

        new File(userFiles).mkdirs();

        return userFiles;
    }

    static public String getUserFiles_FROMSEC()
    {
        String userFiles = getAppSecDirectory() + "/user_files";

        return userFiles;
    }

    static public String replaceRootPaths(String path)
    {
        if (path != null)
        {
            String ret = path.replace(flashRoot, "<Internal>");
            if (sdcardRoot != null)
            {
                ret = ret.replace(sdcardRoot, "<SD-Card>");
            }
            return ret;
        }
        else
        {
            return "Not set";
        }
    }

    static public String hidePaths(String path, String hide1, String hide2)
    {
        String ret = path.replace(hide1 + "/", "");

        if (hide2 != null)
        {
            ret = ret.replace(hide2 + "/", "");
        }
        return ret;
    }

    public static Pair<String, Integer> getDisplayPathAndImage(String path)
    {

        String newPath = path;
        Integer image = R.drawable.ic_baseline_phone_android_black;

        if (path != null)
        {
            if (path.contains("/[internal]"))  // SAF paths
            {
                newPath = path.replace("/[internal]", "");
                image = R.drawable.ic_baseline_phone_android_black;
            }
            else if (path.contains("/[SD-Card]"))  // SAF paths
            {
                newPath = path.replace("/[SD-Card]", "");
                image = R.drawable.ic_baseline_sd_card_black;
            }
            else if (path.contains(flashRoot))
            {
                newPath = path.replace(flashRoot, "");
                image = R.drawable.ic_baseline_phone_android_black;
            }
            else if ((sdcardRoot != null) && (path.contains(sdcardRoot)))
            {
                newPath = path.replace(sdcardRoot, "");
                image = R.drawable.ic_baseline_sd_card_black;
            }
        }
        else
        {
            newPath = " -- Not Set --";
            image = R.drawable.ic_baseline_error_outline_black;
        }

        return new androidx.core.util.Pair(newPath, image);
    }

    static public String hideAppPaths(String path)
    {
        String appPath = getAppDirectory();
        String appSecPath = getAppSecDirectory();

        String ret = path.replace(appPath + "/", "");

        if (appSecPath != null)
        {
            ret = ret.replace(appSecPath + "/", "");
        }
        return ret;
    }

    static public String getGamepadDirectory()
    {
        return AppInfo.getUserFiles() + "/gamepad";
    }

    // JNI
    static public Context getContext()
    {
        return context;
    }

    static public void setContext(Context ctx)
    {
        context = ctx;
    }

    // JNI
    static public String getFilesDir()
    {
        return context.getFilesDir().getAbsolutePath();
    }

    public enum Apps
    {
        MOD_ENGINE, DELTA_TOUCH, ALPHA_TOUCH, QUAD_TOUCH,RAZE_TOUCH
    }
}
