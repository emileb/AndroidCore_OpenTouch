package com.opentouchgaming.androidcore;

/**
 * Created by Emile on 03/08/2017.
 */

public class AppInfo
{
    public enum Apps
    {
        DELTA_TOUCH,
        ALPHA_TOUCH,
    }

    static public void  setAppInfo(Apps app, String title, String internalFiles, String directory)
    {
        AppInfo.app = app;
        AppInfo.title = title;
        AppInfo.internalFiles = internalFiles;
    }

    public static Apps app;
    public static String title;
    public static String internalFiles;

    public static GameEngine currentEngine;
}
