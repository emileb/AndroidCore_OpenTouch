package com.opentouchgaming.androidcore.license;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.opentouchgaming.androidcore.Utils;

public class NdkLicense
{
    static final String SERVICE = "com.android.vending.licensing.ILicensingService";

    public static void check(final Context context, final String key, final NdkLicenseCallback callback)
    {
        Intent intent = new Intent(SERVICE);
        intent.setPackage("com.android.vending");

        context.bindService(
                intent,
                new ServiceConnection()
                {
                    public void onServiceConnected(ComponentName name, IBinder binder)
                    {
                        Parcel d = Parcel.obtain();

                        long id = Utils.getSecureID(context);

                        try
                        {
                            d.writeInterfaceToken(SERVICE);
                            d.writeLong(id);
                            d.writeString(context.getPackageName());
                            d.writeStrongBinder(new NdkLicenseListener(callback, key));
                            binder.transact(1, d, null, IBinder.FLAG_ONEWAY);
                        } catch (RemoteException e)
                        {
                        }
                        d.recycle();
                    }

                    public void onServiceDisconnected(ComponentName name)
                    {
                    }
                },
                Context.BIND_AUTO_CREATE);
    }
}