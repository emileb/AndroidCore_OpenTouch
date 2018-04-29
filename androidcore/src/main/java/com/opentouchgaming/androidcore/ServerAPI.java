package com.opentouchgaming.androidcore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.opentouchgaming.androidcore.license.PackageVerif;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.opentouchgaming.androidcore.DebugLog.Level.D;

public class ServerAPI
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.CORE, "ServerAPI");
    }

    static Activity ctx;
    static int size;

    public static void downloadFile(Activity ctx, String file, String path, int size)
    {
        ServerAPI.size = size;
        ServerAPI.ctx = ctx;
        new DLFileThread().execute(file, path);
    }

    static private class DLFileThread extends AsyncTask<String, Integer, Long>
    {
        private ProgressDialog progressBar;
        String errorstring = null;

        boolean cancel = false;

        @Override
        protected void onPreExecute()
        {
            progressBar = new ProgressDialog(ctx);
            progressBar.setMessage("Downloading/Extracting files..");
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.setCancelable(false);
            progressBar.setCancelable(false);
            progressBar.setButton("Cancel", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    cancel = true;
                    return;
                }
            });
            progressBar.show();
        }

        int getTotalZipSize(String file)
        {
            int ret = 0;
            try
            {
                ZipFile zf = new ZipFile(file);
                Enumeration e = zf.entries();
                while (e.hasMoreElements())
                {
                    ZipEntry ze = (ZipEntry) e.nextElement();
                    String name = ze.getName();

                    ret += ze.getSize();
                    long compressedSize = ze.getCompressedSize();
                }
            } catch (IOException ex)
            {
                System.err.println(ex);
            }
            return ret;
        }

        protected Long doInBackground(String... info)
        {
            String file = info[0];
            String basePath = info[1];

            progressBar.setProgress(0);

            try
            {
                // Get sig and data
                File f = new File(AppInfo.internalFiles + "/l.dat");
                BufferedReader b = new BufferedReader(new FileReader(f));
                String lic_data = b.readLine();
                String lic_sig = b.readLine();

                String apk_hash = PackageVerif.bytesToString(PackageVerif.packageSig(ctx).sig);

                String url_full = "http://opentouchgaming.com/api/download.php?" + ""
                        + "ldata=" + URLEncoder.encode(lic_data, "UTF-8")
                        + "&lsig=" + URLEncoder.encode(lic_sig, "UTF-8")
                        + "&apkhash=" + URLEncoder.encode(apk_hash, "UTF-8")
                        + "&file=" + URLEncoder.encode(file, "UTF-8");


                log.log(D, "url = " + url_full);

                URL url = new URL(url_full);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "");
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.connect();
                int code = connection.getResponseCode();

                log.log(D, "resp code = " + code);

                if (code != 200)
                {
                    errorstring = connection.getResponseMessage();
                    log.log(D, "resp message = " + errorstring);
                    return 1L;
                }

                InputStream inputStream = connection.getInputStream();

                int dlSize = connection.getContentLength();

                progressBar.setMax(dlSize);
                log.log(D, "File size = " + dlSize);

                BufferedInputStream in = null;
                in = new BufferedInputStream(inputStream);

                if (file.endsWith(".zip"))
                {
                    if (ServerAPI.size != 0)
                    {
                        progressBar.setMax(ServerAPI.size);
                    }
                    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null)
                    {
                        if (entry.isDirectory())
                        {
                            // Assume directories are stored parents first then children.
                            log.log(D, "Extracting directory: " + entry.getName());
                            // This is not robust, just for demonstration purposes.
                            (new File(basePath, entry.getName())).mkdirs();
                            continue;
                        }
                        log.log(D, "Extracting file: " + entry.getName());
                        (new File(basePath, entry.getName())).getParentFile().mkdirs();
                        BufferedInputStream zin = new BufferedInputStream(zis);
                        OutputStream out = new FileOutputStream(new File(basePath, entry.getName()));
                        Utils.copyFile(zin, out, progressBar);

                        if (cancel)
                            break;
                    }
                } else
                {
                    File outZipFile = new File(basePath, "temp.zip");

                    OutputStream fout = new FileOutputStream(outZipFile);
                    byte data[] = new byte[1024];
                    int count;
                    while ((count = in.read(data, 0, 1024)) != -1)
                    {
                        fout.write(data, 0, count);
                        progressBar.setProgress(progressBar.getProgress() + count);

                        if (cancel)
                            break;
                    }
                    in.close();
                    fout.close();

                    if (!cancel)
                        outZipFile.renameTo(new File(basePath, file));

                    return 0l;
                }

            } catch (IOException e)
            {
                errorstring = e.toString();
                return 1l;
            }

            return 0l;
        }

        protected void onProgressUpdate(Integer... progress)
        {

        }

        protected void onPostExecute(Long result)
        {
            progressBar.dismiss();
            if (errorstring != null)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setMessage("Error accessing server: " + errorstring)
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {

                            }
                        });

                builder.show();
            }
        }
    }

}
