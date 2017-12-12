package com.opentouchgaming.androidcore;

import android.app.Activity;

/**
 * Created by Emile on 10/12/2017.
 */

public interface EngineOptionsInterface
{
    void showDialog(Activity act, int version, String root);
    String getArgs(int version);
}
