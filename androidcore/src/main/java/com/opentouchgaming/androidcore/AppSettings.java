package com.opentouchgaming.androidcore;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.opentouchgaming.androidcore.controls.TouchSettings;

public class AppSettings {

	public static String belokoBaseDir;

	public static void resetBaseDir(Context ctx)
	{
		belokoBaseDir  =  Environment.getExternalStorageDirectory().toString() + "/Beloko";
		setStringOption(ctx, "base_path", belokoBaseDir);
	}
	
	public static void reloadSettings(Context ctx)
	{
		TouchSettings.reloadSettings(ctx);

		belokoBaseDir = getStringOption(ctx, "base_path", null);
		if (belokoBaseDir == null)
		{
			resetBaseDir(ctx);
		}
	}

	public static String getBaseDir()
	{
		return  AppSettings.belokoBaseDir;
	}



	public static boolean showAbout(Context ctx)
	{
		try {
			int versionCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;

			SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 0);
			int last_ver = settings.getInt("last_opened_version", -1);
			//Log.d("test"," ver = " +  versionCode + " las=" + last_ver);
			if (versionCode != last_ver)
			{
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("last_opened_version", versionCode);
				editor.commit();
				return true;
			}
			else
				return false;

		} catch (NameNotFoundException e) {
			return false;
		}
	}

	public static float getFloatOption(Context ctx,String name, float def)
	{
		SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 	Context.MODE_MULTI_PROCESS);
		return settings.getFloat(name, def);
	}

	public static void setFloatOption(Context ctx,String name, float value)
	{
		SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 	Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat(name, value);
		editor.commit();
	}

	public static boolean getBoolOption(Context ctx,String name, boolean def)
	{
		SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 	Context.MODE_MULTI_PROCESS);
		return settings.getBoolean(name, def);
	}

	public static void setBoolOption(Context ctx,String name, boolean value)
	{
		SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 	Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	public static int getIntOption(Context ctx,String name, int def)
	{
		SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 	Context.MODE_MULTI_PROCESS);
		return settings.getInt(name, def);
	}

	public static void setIntOption(Context ctx,String name, int value)
	{
		SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 	Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(name, value);
		editor.commit();
	}

	public static long getLongOption(Context ctx,String name, long def)
	{
		SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 	Context.MODE_MULTI_PROCESS);
		return settings.getLong(name, def);
	}

	public static void setLongOption(Context ctx,String name, long value)
	{
		SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 	Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(name, value);
		editor.commit();
	}

	public static String getStringOption(Context ctx,String name, String def)
	{
		SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 	Context.MODE_MULTI_PROCESS);
		return settings.getString(name, def);
	}

	public static void setStringOption(Context ctx,String name, String value)
	{
		SharedPreferences settings = ctx.getSharedPreferences("OPTIONS", 	Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(name, value);
		editor.commit();
	}

}
