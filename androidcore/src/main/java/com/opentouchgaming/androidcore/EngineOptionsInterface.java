package com.opentouchgaming.androidcore;

import android.app.Activity;
import android.arch.core.util.Function;


/**
 * Created by Emile on 10/12/2017.
 */

public interface EngineOptionsInterface
{
    interface MultiplayerCallback
    {
        void launch( String mpArgs );
    }

    void showDialog(final Activity act, GameEngine engine, int version, Function<Integer, Void> update);
    String getArgs(int version);
    int getGLESVersion(int version);
    boolean hasMultiplayer();
    void launchMultiplayer(Activity ac,GameEngine engine, int version, String mainArgs, MultiplayerCallback callback );

    default int audioOverrideFreq()
    {
        return 0;
    }

    default int audioOverrideSamples()
    {
        return 0;
    }

}
