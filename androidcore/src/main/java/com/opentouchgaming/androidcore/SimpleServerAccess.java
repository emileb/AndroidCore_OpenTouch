package com.opentouchgaming.androidcore;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.util.Consumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SimpleServerAccess
{
    String LOG = "SimpleServerAccess";
    Context ctx;

    public SimpleServerAccess(Context context, AccessInfo accessInfo)
    {
        ctx = context;
        new ServerAccessThread(accessInfo).execute();
    }

    public static class AccessInfo
    {
        public String url;
        public boolean showUI;
        public Consumer<ByteArrayOutputStream> callback;
    }

    private class ServerAccessThread extends AsyncTask<Void, Integer, Long>
    {
        final AccessInfo accessInfo;
        String errorstring = null;
        ByteArrayOutputStream data_out = new ByteArrayOutputStream();
        private ProgressDialog progressBar;

        ServerAccessThread(AccessInfo accessInfo)
        {
            this.accessInfo = accessInfo;
        }

        @Override
        protected void onPreExecute()
        {
            if (accessInfo.showUI)
            {
                progressBar = new ProgressDialog(ctx);
                progressBar.setMessage("Accessing Server..");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setCancelable(false);
                progressBar.show();
            }
        }

        protected Long doInBackground(Void... info)
        {
            String url_full = accessInfo.url;

            if (GD.DEBUG)
                Log.d(LOG, url_full);

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder().url(url_full).post(RequestBody.create(new byte[0])) // Replace with actual body if needed
                    .build();

            try (Response response = client.newCall(request).execute())
            {
                int code = response.code();

                if (GD.DEBUG)
                {
                    Log.d(LOG, "code = " + code);
                    Log.d(LOG, "reason = " + response.message());
                }

                if (code != 200)
                {
                    errorstring = response.message();
                    return 1L;
                }

                ResponseBody body = response.body();
                if (body == null)
                {
                    errorstring = "Empty response body";
                    return 1L;
                }

                long dlSize = body.contentLength();
                InputStream ins = body.byteStream();

                if (accessInfo.showUI)
                    progressBar.setMax((int) dlSize);

                if (GD.DEBUG)
                    Log.d(LOG, "File size = " + dlSize);

                BufferedInputStream in = new BufferedInputStream(ins);
                byte[] data = new byte[1024];
                int count;
                while ((count = in.read(data)) != -1)
                {
                    data_out.write(data, 0, count);
                }
                in.close();
            }
            catch (IOException e)
            {
                errorstring = e.toString();
                return 1L;
            }

            return 0L;
        }

        protected void onProgressUpdate(Integer... progress)
        {

        }

        protected void onPostExecute(Long result)
        {
            if (accessInfo.showUI)
            {
                progressBar.dismiss();
                if (errorstring != null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setMessage("Error accessing server: " + errorstring).setCancelable(true)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {

                                }
                            });

                    builder.show();
                }
            }

            if (errorstring == null)
            {
                accessInfo.callback.accept(data_out);
            }
        }
    }
}
