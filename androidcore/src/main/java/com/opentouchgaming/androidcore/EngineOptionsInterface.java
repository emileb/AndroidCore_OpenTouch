package com.opentouchgaming.androidcore;

import android.app.Activity;

import androidx.arch.core.util.Function;


/**
 * Created by Emile on 10/12/2017.
 */

public interface EngineOptionsInterface
{
    void showDialog(final Activity act, GameEngine engine, int version, Function<Integer, Void> update);

    RunInfo getRunInfo(int version);

    boolean hasMultiplayer();

    void launchMultiplayer(Activity ac, GameEngine engine, int version, String mainArgs, MultiplayerCallback callback);

    default int audioOverrideFreq()
    {
        return 0;
    }

    default int audioOverrideSamples()
    {
        return 0;
    }

    default int audioOverrideBackend()
    {
        return -1;
    }

    interface MultiplayerCallback
    {
        void launch(String mpArgs);
    }

    class RunInfo
    {
        public String args;
        public int glesVersion;
        public boolean useGL4ES;

        public boolean maintainAspect;
        public String frameBufferWidth;
        public String frameBufferHeight;
    }

}
