package com.opentouchgaming.androidcore.common;

import android.app.Activity;

import androidx.core.util.Pair;

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

    default String getRunDirectory(SubGame subGame) // For total conversions where the run folder might change
    {
        return getRunDirectory();
    }

    String getSecondaryDirectory();

    Pair<String, String> getQuickCommandsDirectory(SubGame subGame);

    String getArgs(GameEngine engine, SubGame subGame);

    boolean checkForDownloads(Activity activity, GameEngine engine, SubGame subGame);

    default boolean forceShowModsWads()
    {
        return false;
    }

    // For launchers that can run from one of several top level folders (e.g. FTEQW: "Q1" or "H2").
    // Return null (default) if there is nothing to select.
    default String[] getSelectableFolders()
    {
        return null;
    }

    // The currently selected folder, must be one of getSelectableFolders()
    default String getSelectedFolder()
    {
        return null;
    }

    // Called when the user picks a folder from the selection spinner
    default void setSelectedFolder(String folder)
    {
    }
}
