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
		String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
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

	public static String filesInfoString(String path, String ext, int maxFiles)
	{
		File files[] = new File(path).listFiles();

		String pakFiles = "[ ";
		int nbrFiles = 0;
		int totalSize = 0;
		for (File file : files)
		{
			if (file.getName().toLowerCase().endsWith(ext))
			{
				if( nbrFiles < maxFiles )
				{
					pakFiles += file.getName() + ", ";
				}
				totalSize += file.length();
				nbrFiles++;
			}
		}
		pakFiles += "]";

		String ret = nbrFiles + " files (" + Utils.humanReadableByteCount(totalSize, false) + ") : " + pakFiles;
		return ret;
	}
}
