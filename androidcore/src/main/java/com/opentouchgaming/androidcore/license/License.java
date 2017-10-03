package com.opentouchgaming.androidcore.license;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.opentouchgaming.androidcore.AppSettings;
import com.opentouchgaming.androidcore.GD;


public class License
{

    private static final String LOG = "License";

    private static final int LIC_NETWORK_RETRIES = 4; //How many times to automatically retry license check
    private static final int LIC_MAX_UNLIC_CNT = 2; //Number of times it can run unlicened before quits

    private static final long MILLIS_PER_MINUTE = 60 * 1000;

    private static final long TIME_BEFORE_FIRST_CHECK = MILLIS_PER_MINUTE * 5; //5 mins

    private static final long TIME_BETWEEN_CHECKS = MILLIS_PER_MINUTE * 60 * 24 * 3; //3 days
    //private static final long TIME_BETWEEN_CHECKS = 0;

    private int licNetworkRetries = LIC_NETWORK_RETRIES;

    final Activity ctx;
    final int game;
    final String key;


    public License(Activity ctx, int game, String public_key)
    {
        this.ctx = ctx;
        this.game = game;
        this.key = public_key;
        if (GD.DEBUG) Log.d(LOG, "CREATED");
    }


    public void doCheck()
    {
        if (GD.DEBUG) Log.v(LOG, "doCheck()");
        licNetworkRetries = LIC_NETWORK_RETRIES;

        long installed_time = AppSettings.getLongOption(ctx, "inst_time", 0);
        if (installed_time == 0)
        {
            if (GD.DEBUG) Log.v(LOG, "Install time is 0");
            installed_time = System.currentTimeMillis();
            AppSettings.setLongOption(ctx, "inst_time", installed_time);
            return;
        } else
        {
            if (System.currentTimeMillis() < (installed_time + TIME_BEFORE_FIRST_CHECK))
            {
                if (GD.DEBUG) Log.v(LOG, "First install grace period");
                return;
            }
        }

        int badLic = AppSettings.getIntOption(ctx, "ul", 0);

        long last_check_time = AppSettings.getLongOption(ctx, "last_check_time", 0);
        if ((badLic == 1) || (System.currentTimeMillis() > (last_check_time + TIME_BETWEEN_CHECKS)))
        {
            if (GD.DEBUG) Log.v(LOG, "Doing check");
            AppSettings.setLongOption(ctx, "last_check_time", System.currentTimeMillis());
            NdkLicense.check(ctx, key, new LCallback());
        } else
        {
            if (GD.DEBUG) Log.v(LOG, "NOT Doing check");
        }
    }

    public class LCallback implements NdkLicenseCallback
    {

        @Override
        public void status(final LicStatus ret)
        {

            if (ret.code == NdkLicenseCallback.GOOD)
            {
                if (GD.DEBUG) Log.v(LOG, "Google says it is licensed");
                AppSettings.setIntOption(ctx, "ul", 0);
            } else if (ret.code == NdkLicenseCallback.NO_GOOD)
            {
                if (GD.DEBUG) Log.v(LOG, "Google says it is unlicensed");
                ctx.runOnUiThread(new Runnable()
                {
                    public void run()
                    {


                        Toast.makeText(ctx, "Unlicensed, reason: " + ret.desc, Toast.LENGTH_LONG).show();

                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setMessage("Unlicensed, reason: " + ret.desc)
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

                AppSettings.setIntOption(ctx, "ul", 1);
            } else
            {
                if (GD.DEBUG) Log.v(LOG, "Got responce " + ret.code + " Desc = " + ret.desc);
            }
        }
    }

    public boolean isLicensed(String file)
    {
        int badLic = AppSettings.getIntOption(ctx, "ul", 0);

        if (badLic == 0)
            return true;
        else
            return false;
    }

    public void showBadLicense(Context ctx)
    {
        final License this_ = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("Google License error. Please Contact support@beloko.com if this is an error, Thank you.")
                .setCancelable(true)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        this_.doCheck();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

}
