package com.opentouchgaming.androidcore;

import android.widget.ImageButton;

import com.opentouchgaming.androidcore.controls.ActionInputDefinition;

/**
 * Created by Emile on 09/07/2017.
 */

public class GameEngine
{
    public enum Engine
    {
        GZDOOM,
        PRBOOM,
        CHOC,
        RETRO
    }

    public GameEngine(Engine engine, String title, String name, String[] versions, String[][] loadLibs, String args, ActionInputDefinition gamepadDefiniton, int iconRes, int color)
    {
        this.title = title;
        this.name = name;
        this.engine = engine;
        this.iconRes = iconRes;
        this.color = color;
        this.loadLibs = loadLibs;
        this.args = args;
        this.versions = versions;
        this.gamepadDefiniton = gamepadDefiniton;
    }

    final public int iconRes;
    final public int color;
    final public Engine engine;
    final public String title;
    final public String name;
    final public String args;
    final public String[] versions;
    final public ActionInputDefinition gamepadDefiniton;
    final public String[][] loadLibs;

    public ImageButton imageButton;
}
