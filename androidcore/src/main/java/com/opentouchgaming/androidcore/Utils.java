package com.opentouchgaming.androidcore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.opentouchgaming.androidcore.license.LicenseCheck;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

;

public class Utils {
	static String LOG = "Utils";

	static DebugLog log;

	static
	{
		log = new DebugLog(DebugLog.Module.CONTROLS, "Utils");
	}

	static public int dpToPx( Resources r, int dp )
	{
		int px = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				dp,
				r.getDisplayMetrics()
		);
		return px;
	}

	static public boolean mkdirs( Context context, String path)
	{
		File file = new File(path);
		if (!file.exists())
		{
			if( !file.mkdirs() )
			{
				log.log(DebugLog.Level.E, "Did not create base folder");
			}

			File f = new File(path, "temp_");
			try {
				f.createNewFile();
				new SingleMediaScanner(context, false,  f.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		else
		{
			new File(path, "temp_").delete();
			return false;
		}
	}
	static public void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
		out.close(); 
	}

	static public void copyFile(InputStream in, OutputStream out,ProgressDialog pb) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
			pb.setProgress(pb.getProgress() + 1024);
		}
		out.close(); 
	}

	static public  void showDownloadDialog(final Activity act,String key, String title,final String directory,final String file,final int size)
	{
        boolean ok = LicenseCheck.checkLicenseFile(act, key);
        if (!ok)
        {
            LicenseCheck.fetchLicense(act, true, key);
            return;
        }

		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setMessage(title)
		.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				ServerAPI.downloadFile(act,file,directory,size);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		AlertDialog alert = builder.create();
		alert.show(); 
	}
	
	public static String checkFiles(String basePath,String[] files_to_ceck)
	{
		File[] files = new File(basePath ).listFiles();
		boolean ok=true;

		String filesNotFound = "";

		String[] expected;
		expected = files_to_ceck;

		if (files == null)
			files = new File[0];

		if (files!=null)
		{
			for (File f: files)
			{
				Log.d(LOG,"FILES: " + f.toString());
			}

			for (String e: expected)
			{
				boolean found=false;
				for (File f: files)
				{
					if (f.toString().toLowerCase().endsWith(e.toLowerCase()))
						found = true;
				}
				if (!found)
				{
					Log.d(LOG,"Didnt find " + e);
					filesNotFound +=  e + "\n";
					ok = false;
				}
			}
		}

		if (filesNotFound.contentEquals(""))
			return null;
		else
			return filesNotFound;

	}
	static public void copyPNGAssets(Context ctx,String dir) {
		copyPNGAssets(ctx,dir,"");
	}

	static public void copyPNGAssets(Context ctx,String dir,String prefix) {

		if (prefix == null)
			prefix = "";

		File d = new File(dir);
		if (!d.exists())
			d.mkdirs();

		AssetManager assetManager = ctx.getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		for(String filename : files) {
			if (filename.endsWith("png") && filename.startsWith(prefix)){
				InputStream in = null;
				OutputStream out = null;
				//Log.d("test","file = " + filename);
				try {
					in = assetManager.open(filename);
					out = new FileOutputStream(dir + "/" + filename.substring(prefix.length()));
					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				} catch(IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				}       
			}
		}
	}

	public static void ExtractAsset(Context ctx,String file, String dest, long size)
	{
		ExtractAsset.ctx = ctx;
		ExtractAsset.totalSize = size;
		new ExtractAsset().execute(file,dest);
	}

	static private class ExtractAsset extends AsyncTask<String, Integer, Long> {

		private ProgressDialog progressBar;
		String errorstring= null;
		static Context ctx;
		static long totalSize;

		@Override
		protected void onPreExecute() {
			progressBar = new ProgressDialog(ctx);
			progressBar.setMessage("Extracting files..");
			progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressBar.setCancelable(false);
			progressBar.show();
		}

		int getTotalZipSize(String file)
		{
			int ret = 0;
			try {
				ZipFile zf = new ZipFile(file);
				Enumeration e = zf.entries();
				while (e.hasMoreElements()) {
					ZipEntry ze = (ZipEntry) e.nextElement();
					String name = ze.getName();

					ret += ze.getSize();
					long compressedSize = ze.getCompressedSize();
				}
			} catch (IOException ex) {
				System.err.println(ex);
			}
			return ret;
		}

		int getTotalZipSize(InputStream ins)
		{
			int ret = 0;
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(ins));
			ZipEntry entry;
			try {
				while ((entry = zis.getNextEntry()) != null) {
                    if(entry.isDirectory()) {

                    }
                    else {
                        ret += entry.getSize();
                    }
                }
				ins.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (GD.DEBUG) Log.d(LOG,"File size is " + ret);

			return ret;
		}
		protected Long doInBackground(String... info) {

			String file = info[0];
			String basePath = info[1];

			boolean isLocal = false;

			progressBar.setProgress(0);

            try
            {
                BufferedInputStream in = null;
                FileOutputStream fout = null;

                AssetManager assetManager = ctx.getAssets();
                InputStream ins = assetManager.open(file);

                in = new BufferedInputStream(ins);

                if (file.endsWith(".zip"))
                {
					if( totalSize != 0)
						progressBar.setMax((int)totalSize);
					else
						progressBar.setMax(getTotalZipSize(ins));

					ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        if(entry.isDirectory()) {
                            // Assume directories are stored parents first then children.
                            System.err.println("Extracting directory: " + entry.getName());
                            // This is not robust, just for demonstration purposes.
                            (new File(basePath, entry.getName())).mkdirs();
                            continue;
                        }
                        if (GD.DEBUG) Log.d(LOG,"Extracting file: " + entry.getName());

                        (new File(basePath, entry.getName())).getParentFile().mkdirs();
                        BufferedInputStream zin = new BufferedInputStream(zis);
                        OutputStream out =  new FileOutputStream(new File(basePath, entry.getName()));
                        Utils.copyFile(zin,out,progressBar);
                    }
                }
                else
                {

					File outZipFile = new File(basePath,"temp.zip");

					//progressBar.setMax(ins.get); //TODO FIX ME

					fout = new FileOutputStream(outZipFile);
                    byte data[] = new byte[1024];
                    int count;
                    while ((count = in.read(data, 0, 1024)) != -1)
                    {
                        fout.write(data, 0, count);
                        progressBar.setProgress(progressBar.getProgress() + count);
                    }
                    in.close();
                    fout.close();
                    //AppSettings.setBoolOption(ctx,"DLF" + ServerAPI.file, true);

                    outZipFile.renameTo(new File(basePath , file));
                    return 0l;
                }

            } catch (IOException e) {
                errorstring = e.toString();
                return 1l;
            }

			return 0l;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Long result) {
			progressBar.dismiss();
			if (errorstring!=null)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				builder.setMessage("Error extracting: " + errorstring)
						.setCancelable(true)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						});

				builder.show();
			}
		}
	}

	static public String[] creatArgs(String appArgs)
	{
		//ArrayList<String> a = new ArrayList<String>(Arrays.asList(appArgs.split(" ")));
		ArrayList<String> a  = new ArrayList<String>(Arrays.asList(appArgs.split("\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?")));
		Iterator<String> iter = a.iterator();
		while (iter.hasNext()) {
			if (iter.next().contentEquals("")) {
				iter.remove();
			}
		}

		return a.toArray(new String[a.size()]);
	}


	public static void expand(final View v) {
		v.measure(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		final int targtetHeight = v.getMeasuredHeight();

		v.getLayoutParams().height = 0;
		v.setVisibility(View.VISIBLE);
		Animation a = new Animation()
		{
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				v.getLayoutParams().height = interpolatedTime == 1
						? LayoutParams.WRAP_CONTENT
								: (int)(targtetHeight * interpolatedTime);
				v.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		// 1dp/ms
		a.setDuration((int)(targtetHeight / v.getContext().getResources().getDisplayMetrics().density));
		v.startAnimation(a);
	}

	public static void collapse(final View v) {
		final int initialHeight = v.getMeasuredHeight();

		Animation a = new Animation()
		{
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if(interpolatedTime == 1){
					v.setVisibility(View.GONE);
				}else{
					v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		// 1dp/ms
		a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
		v.startAnimation(a);
	}


	static final int BUFFER_SIZE = 1024;

	static public String getLogCat() {
		String[] logcatArgs = new String[] {"logcat", "-d", "-v", "time"};

		Process logcatProc = null;
		try {
			logcatProc = Runtime.getRuntime().exec(logcatArgs);
		}
		catch (IOException e) {
			return null;
		}

		BufferedReader reader = null;
		String response = null;
		try {
			String separator = System.getProperty("line.separator");
			StringBuilder sb = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), BUFFER_SIZE);
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append(separator);
			}
			response = sb.toString();
		}
		catch (IOException e) {
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {}
			}
		}

		return response;
	}


	static public void copyAsset(Context ctx,String file,String destdir) {
		AssetManager assetManager = ctx.getAssets();

		InputStream in = null;
		OutputStream out = null;

		try {
			in = assetManager.open(file);
			out = new FileOutputStream(destdir + "/" + file);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch(IOException e) {
			Log.e("tag", "Failed to copy asset file: " + file + " error = " + e.toString());
		}       
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;  
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public static void setImmersionMode(final Activity act)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

			if (AppSettings.getBoolOption(act,"immersive_mode", false))
			{
				act.getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
						| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
						| View.SYSTEM_UI_FLAG_IMMERSIVE
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );

				View decorView = act.getWindow().getDecorView();
				decorView.setOnSystemUiVisibilityChangeListener
				(new View.OnSystemUiVisibilityChangeListener() {
					@Override
					public void onSystemUiVisibilityChange(int visibility) {
						Log.d(LOG,"onSystemUiVisibilityChange");

						act.getWindow().getDecorView().setSystemUiVisibility(
								View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
								| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
								| View.SYSTEM_UI_FLAG_IMMERSIVE
								| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );

					}
				});
			}
		}
	}

	public static void onWindowFocusChanged(final Activity act,final boolean hasFocus)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

			if (AppSettings.getBoolOption(act,"immersive_mode",false))
			{
				/*
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {

						if (hasFocus) {
							act.getWindow().getDecorView().setSystemUiVisibility(
									View.SYSTEM_UI_FLAG_LAYOUT_STABLE
											| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
											| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
											| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
											| View.SYSTEM_UI_FLAG_FULLSCREEN
											| View.SYSTEM_UI_FLAG_IMMERSIVE
											| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
						}
					}

				}, 2000);
				*/
				if (hasFocus) {
					act.getWindow().getDecorView().setSystemUiVisibility(
							View.SYSTEM_UI_FLAG_LAYOUT_STABLE
									| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
									| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
									| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
									| View.SYSTEM_UI_FLAG_FULLSCREEN
									| View.SYSTEM_UI_FLAG_IMMERSIVE
									| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
				}
			}
		}
	}
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static long getSecureID(Context ctx) {
		BigInteger b = new BigInteger(Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID), 16);
		// Log.d("TEST","long = " + b.longValue());
		return b.longValue();
	}

	public static float convertDpToPixel(float dp, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return px;
	}
/*
	public static ArrayList<ActionInput> getGameGamepadConfig(GD.IDGame game)
	{
		ArrayList<ActionInput> actions = new ArrayList<ActionInput>();

		actions.add(new ActionInput("analog_move_fwd","Forward/Back", ControlConfig.ACTION_ANALOG_FWD, ActionInput.ActionType.ANALOG));
		actions.add(new ActionInput("analog_move_strafe","Strafe",ControlConfig.ACTION_ANALOG_STRAFE, ActionInput.ActionType.ANALOG));


		if (game != GD.IDGame.Wolf3d)
			actions.add(new ActionInput("analog_look_pitch","Look Up/Look Down",ControlConfig.ACTION_ANALOG_PITCH, ActionInput.ActionType.ANALOG));

		actions.add(new ActionInput("analog_look_yaw","Look Left/Look Right",ControlConfig.ACTION_ANALOG_YAW, ActionInput.ActionType.ANALOG));

		actions.add(new ActionInput("attack","Attack",ControlConfig.PORT_ACT_ATTACK, ActionInput.ActionType.BUTTON));

		if ((game == GD.IDGame.Doom) || (game == GD.IDGame.Wolf3d)|| (game == GD.IDGame.Hexen)|| (game == GD.IDGame.Strife)|| (game == GD.IDGame.Heretic))
			actions.add(new ActionInput("use","Use/Open",ControlConfig.PORT_ACT_USE, ActionInput.ActionType.BUTTON));

		if (game == GD.IDGame.RTCW)
		{
			actions.add(new ActionInput("use","Use/Open",ControlConfig.PORT_ACT_USE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("reload","Reload",ControlConfig.PORT_ACT_RELOAD, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("alt_fire","Alt Weapon",ControlConfig.PORT_ACT_ALT_FIRE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("binocular","Binocuar",ControlConfig.PORT_ACT_ZOOM_IN, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("quick_kick","Kick",ControlConfig.PORT_ACT_KICK, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("lean_left","Lean Left",ControlConfig.PORT_ACT_LEAN_LEFT, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("lean_right","Lean Right",ControlConfig.PORT_ACT_LEAN_RIGHT, ActionInput.ActionType.BUTTON));
		}

		if (game == GD.IDGame.Quake) {
			actions.add(new ActionInput("malice_reload", "Malice Reload", ControlConfig.PORT_MALICE_RELOAD, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("malice_use", "Malice Use", ControlConfig.PORT_MALICE_USE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("malice_cycle", "Malice Cycle", ControlConfig.PORT_MALICE_CYCLE, ActionInput.ActionType.BUTTON));
		}

		if (game == GD.IDGame.Quake3)
		{
			actions.add(new ActionInput("zoomin","Zoom in/out",ControlConfig.PORT_ACT_ZOOM_IN, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("custom_0","Custom F1",ControlConfig.PORT_ACT_CUSTOM_0, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("custom_1","Custom F2",ControlConfig.PORT_ACT_CUSTOM_1, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("custom_2","Custom F3",ControlConfig.PORT_ACT_CUSTOM_2, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("custom_3","Custom F4",ControlConfig.PORT_ACT_CUSTOM_3, ActionInput.ActionType.BUTTON));
		}

		if ((game == GD.IDGame.JK2) || (game == GD.IDGame.JK3))
		{
			actions.add(new ActionInput("attack_alt","Alt Attack",ControlConfig.PORT_ACT_ALT_ATTACK, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("use_force","Use Force",ControlConfig.PORT_ACT_FORCE_USE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("saber_style","Saber Style",ControlConfig.PORT_ACT_SABER_STYLE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("saber_show_hide","Saber Sheath/Unsheath",ControlConfig.PORT_ACT_SABER_SEL, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("use","Use/Open",ControlConfig.PORT_ACT_USE, ActionInput.ActionType.BUTTON));
		}

		if ((game != GD.IDGame.Doom) && (game != GD.IDGame.Wolf3d))
			actions.add(new ActionInput("jump","Jump",ControlConfig.PORT_ACT_JUMP, ActionInput.ActionType.BUTTON));

		if ((game == GD.IDGame.Quake2) || (game == GD.IDGame.Quake3)|| (game == GD.IDGame.Hexen2)|| (game == GD.IDGame.RTCW)|| (game == GD.IDGame.JK2) || (game == GD.IDGame.JK3))
			actions.add(new ActionInput("crouch","Crouch",ControlConfig.PORT_ACT_DOWN, ActionInput.ActionType.BUTTON));

		//Add GZDoom specific actions
		if (game == GD.IDGame.Doom)
		{
			actions.add(new ActionInput("attack_alt","Alt Attack (GZ)",ControlConfig.PORT_ACT_ALT_ATTACK, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("jump","Jump (GZ)",ControlConfig.PORT_ACT_JUMP, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("crouch","Crouch (GZ)",ControlConfig.PORT_ACT_DOWN, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("custom_0","Custom A (GZ)",ControlConfig.PORT_ACT_CUSTOM_0, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("custom_1","Custom B (GZ)",ControlConfig.PORT_ACT_CUSTOM_1, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("custom_2","Custom C (GZ)",ControlConfig.PORT_ACT_CUSTOM_2, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("custom_3","Custom D (GZ)",ControlConfig.PORT_ACT_CUSTOM_3, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("custom_4","Custom E (GZ)",ControlConfig.PORT_ACT_CUSTOM_4, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("custom_5","Custom F (GZ)",ControlConfig.PORT_ACT_CUSTOM_5, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("quick_save","Quick Save (GZ)",ControlConfig.PORT_ACT_QUICKSAVE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("quick_load","Quick Load (GZ)",ControlConfig.PORT_ACT_QUICKLOAD, ActionInput.ActionType.BUTTON));
		}

		actions.add(new ActionInput("fwd","Move Forward",ControlConfig.PORT_ACT_FWD, ActionInput.ActionType.BUTTON));
		actions.add(new ActionInput("back","Move Backwards",ControlConfig.PORT_ACT_BACK, ActionInput.ActionType.BUTTON));
		actions.add(new ActionInput("left","Strafe Left",ControlConfig.PORT_ACT_MOVE_LEFT, ActionInput.ActionType.BUTTON));
		actions.add(new ActionInput("right","Strafe Right",ControlConfig.PORT_ACT_MOVE_RIGHT, ActionInput.ActionType.BUTTON));

		if ((game != GD.IDGame.Doom) && (game != GD.IDGame.Wolf3d))
		{
			actions.add(new ActionInput("look_up","Look Up",ControlConfig.PORT_ACT_LOOK_UP, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("look_down","Look Down",ControlConfig.PORT_ACT_LOOK_DOWN, ActionInput.ActionType.BUTTON));
		}

		actions.add(new ActionInput("look_left","Look Left",ControlConfig.PORT_ACT_LEFT, ActionInput.ActionType.BUTTON));
		actions.add(new ActionInput("look_right","Look Right",ControlConfig.PORT_ACT_RIGHT, ActionInput.ActionType.BUTTON));

		if ((game != GD.IDGame.Wolf3d) && (game != GD.IDGame.JK2) || (game != GD.IDGame.JK3))
		{
			actions.add(new ActionInput("strafe_on","Strafe On",ControlConfig.PORT_ACT_STRAFE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("speed","Run On",ControlConfig.PORT_ACT_SPEED, ActionInput.ActionType.BUTTON));
		}
		actions.add(new ActionInput("next_weapon","Next Weapon",ControlConfig.PORT_ACT_NEXT_WEP, ActionInput.ActionType.BUTTON));
		actions.add(new ActionInput("prev_weapon","Previous Weapon",ControlConfig.PORT_ACT_PREV_WEP, ActionInput.ActionType.BUTTON));

		if ((game == GD.IDGame.JK2)|| (game == GD.IDGame.JK3))
		{
			actions.add(new ActionInput("next_force","Next Force",ControlConfig.PORT_ACT_NEXT_FORCE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("prev_force","Previous Force",ControlConfig.PORT_ACT_PREV_FORCE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("force_pull","Force Pull",ControlConfig.PORT_ACT_FORCE_PULL, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("force_push","Force Push",ControlConfig.PORT_ACT_FORCE_PUSH, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("force_speed","Force Speed",ControlConfig.PORT_ACT_FORCE_SPEED, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("force_heal","Force Heal",ControlConfig.PORT_ACT_FORCE_HEAL, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("force_mind","Force Mind",ControlConfig.PORT_ACT_FORCE_MIND, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("force_grip","Force Grip",ControlConfig.PORT_ACT_FORCE_GRIP, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("force_lightning","Force Lightning",ControlConfig.PORT_ACT_FORCE_LIGHT, ActionInput.ActionType.BUTTON));

		}

		if ((game == GD.IDGame.Quake2) || (game == GD.IDGame.Hexen2)|| (game == GD.IDGame.RTCW))
		{
			actions.add(new ActionInput("help_comp","Show Objectives",ControlConfig.PORT_ACT_HELPCOMP, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_show","Show Inventory",ControlConfig.PORT_ACT_INVEN, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_use","Use Item",ControlConfig.PORT_ACT_INVUSE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_next","Next Item",ControlConfig.PORT_ACT_INVNEXT, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_prev","Prev Item",ControlConfig.PORT_ACT_INVPREV, ActionInput.ActionType.BUTTON));
		}

		if (game == GD.IDGame.JK2)
		{
			actions.add(new ActionInput("help_comp","Show Data Pad",ControlConfig.PORT_ACT_DATAPAD, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_use","Use Item",ControlConfig.PORT_ACT_INVUSE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_next","Next Item",ControlConfig.PORT_ACT_INVNEXT, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_prev","Prev Item",ControlConfig.PORT_ACT_INVPREV, ActionInput.ActionType.BUTTON));
		}

		if (game == GD.IDGame.Hexen)
		{
			actions.add(new ActionInput("inv_use","Use Item",ControlConfig.PORT_ACT_INVUSE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_next","Next Item",ControlConfig.PORT_ACT_INVNEXT, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_prev","Prev Item",ControlConfig.PORT_ACT_INVPREV, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("fly_up","Fly Up",ControlConfig.PORT_ACT_FLY_UP, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("fly_down","Fly Down",ControlConfig.PORT_ACT_FLY_DOWN, ActionInput.ActionType.BUTTON));

		}

		if (game == GD.IDGame.Strife)
		{
			actions.add(new ActionInput("inv_use","Use Item",ControlConfig.PORT_ACT_INVUSE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_drop","Drop Item",ControlConfig.PORT_ACT_INVDROP, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_next","Next Item",ControlConfig.PORT_ACT_INVNEXT, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_prev","Prev Item",ControlConfig.PORT_ACT_INVPREV, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("show_weap","Show Stats/Weapons",ControlConfig.PORT_ACT_SHOW_WEAPONS, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("show_keys","Show Keys",ControlConfig.PORT_ACT_SHOW_KEYS, ActionInput.ActionType.BUTTON));

		}

		if (game == GD.IDGame.Heretic)
		{
			actions.add(new ActionInput("inv_use","Use Item",ControlConfig.PORT_ACT_INVUSE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_next","Next Item",ControlConfig.PORT_ACT_INVNEXT, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("inv_prev","Prev Item",ControlConfig.PORT_ACT_INVPREV, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("fly_up","Fly Up",ControlConfig.PORT_ACT_FLY_UP, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("fly_down","Fly Down",ControlConfig.PORT_ACT_FLY_DOWN, ActionInput.ActionType.BUTTON));
		}

		if (game == GD.IDGame.Quake3)
		{
			actions.add(new ActionInput("inv_use","Use Item",ControlConfig.PORT_ACT_USE, ActionInput.ActionType.BUTTON));
		}

		if (game == GD.IDGame.Doom)
		{
			actions.add(new ActionInput("map_show","Show Automap",ControlConfig.PORT_ACT_MAP, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("map_up","Automap Up",ControlConfig.PORT_ACT_MAP_UP, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("map_down","Automap Down",ControlConfig.PORT_ACT_MAP_DOWN, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("map_left","Automap Left",ControlConfig.PORT_ACT_MAP_LEFT, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("map_right","Automap Right",ControlConfig.PORT_ACT_MAP_RIGHT, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("map_zoomin","Automap Zoomin",ControlConfig.PORT_ACT_MAP_ZOOM_IN, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("map_zoomout","Automap Zoomout",ControlConfig.PORT_ACT_MAP_ZOOM_OUT, ActionInput.ActionType.BUTTON));
		}

		if ((game == GD.IDGame.RTCW) || (game == GD.IDGame.JK2) || (game == GD.IDGame.JK3))
		{
			actions.add(new ActionInput("quick_save","Quick Save",ControlConfig.PORT_ACT_QUICKSAVE, ActionInput.ActionType.BUTTON));
			actions.add(new ActionInput("quick_load","Quick Load",ControlConfig.PORT_ACT_QUICKLOAD, ActionInput.ActionType.BUTTON));
		}

		if ((game == GD.IDGame.Doom) || (game == GD.IDGame.Heretic)  || (game == GD.IDGame.Hexen)
				|| (game == GD.IDGame.Strife)|| (game == GD.IDGame.Quake)|| (game == GD.IDGame.Quake2)
				|| (game == GD.IDGame.Hexen2) || (game == GD.IDGame.Wolf3d)
				|| (game == GD.IDGame.JK2)  || (game == GD.IDGame.JK3) || (game == GD.IDGame.TestPlatform))
		{
			actions.add(new ActionInput("menu_up","Menu Up",ControlConfig.MENU_UP, ActionInput.ActionType.MENU));
			actions.add(new ActionInput("menu_down","Menu Down",ControlConfig.MENU_DOWN, ActionInput.ActionType.MENU));
			actions.add(new ActionInput("menu_left","Menu Left",ControlConfig.MENU_LEFT, ActionInput.ActionType.MENU));
			actions.add(new ActionInput("menu_right","Menu Right",ControlConfig.MENU_RIGHT, ActionInput.ActionType.MENU));
			actions.add(new ActionInput("menu_select","Menu Select",ControlConfig.MENU_SELECT, ActionInput.ActionType.MENU));
			actions.add(new ActionInput("menu_back","Menu Back",ControlConfig.MENU_BACK, ActionInput.ActionType.MENU));
		}

		return actions;
	}
	*/
}
