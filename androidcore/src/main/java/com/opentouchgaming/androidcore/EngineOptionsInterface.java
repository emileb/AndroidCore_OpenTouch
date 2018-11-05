package com.opentouchgaming.androidcore;

import android.app.Activity;

/**
 * Created by Emile on 10/12/2017.
 */

public interface EngineOptionsInterface
{
    interface MultiplayerCallback
    {
        void launch( String mpArgs );
    }

    void showDialog(final Activity act, GameEngine engine, int version,final String root);
    String getArgs(int version);
    boolean hasMultiplayer();
    void launchMultiplayer(Activity ac,GameEngine engine, int version, String mainArgs, MultiplayerCallback callback );
}
