package com.opentouchgaming.androidcore.common;

import android.app.Activity;

import com.opentouchgaming.androidcore.GameEngine;
import com.opentouchgaming.androidcore.SubGame;

import java.util.ArrayList;

/**
 * Created by Emile on 09/09/2018.
 */

public interface GameLauncherInterface
{
    void updateSubGames(GameEngine engine, ArrayList<SubGame> subGames);

    String getRunDirectory();

    String getSecondaryDirectory();

    String getArgs(GameEngine engine, SubGame subGame);

    boolean checkForDownloads(Activity activity, GameEngine engine, SubGame subGame);
}
