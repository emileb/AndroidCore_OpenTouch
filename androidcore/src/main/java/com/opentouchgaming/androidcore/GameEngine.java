package com.opentouchgaming.androidcore;

import android.app.Activity;
import android.widget.ImageView;

import com.opentouchgaming.androidcore.controls.ActionInputDefinition;

/**
 * Created by Emile on 09/07/2017.
 */

public class GameEngine
{
    final public int iconRes;
    final public int color;
    final public Engine engine;
    final public String title;
    final public String name;
    final public String args;
    final public String directory;
    final public String[] versions;
    final public ActionInputDefinition gamepadDefiniton;
    final public String[][] loadLibs;
    final public int uiGroup;
    final Class engineOptionsClass;
    public ImageView imageButton;
    public ImageView imageButtonCfg;
    public EngineOptionsInterface engineOptions;
    public GameEngine(Engine engine, int uiGroup, String title, String name, String directory, String[] versions, String[][] loadLibs, String args, ActionInputDefinition gamepadDefiniton, int iconRes,
                      int color, Class opCls)
    {
        this.title = title;
        this.name = name;
        this.directory = directory;
        this.engine = engine;
        this.uiGroup = uiGroup;
        this.iconRes = iconRes;
        this.color = color;
        this.loadLibs = loadLibs;
        this.args = args;
        this.versions = versions;
        this.gamepadDefiniton = gamepadDefiniton;
        this.engineOptionsClass = opCls;
    }

    public void init(Activity act)
    {
        if (engineOptionsClass != null && engineOptions == null)
        {
            try // Create class from class type
            {
                engineOptions = (EngineOptionsInterface) engineOptionsClass.newInstance();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            } catch (InstantiationException e)
            {
                e.printStackTrace();
            }
        }
    }

    public String getLogFilename()
    {
        return AppInfo.internalFiles + "/" + name + ".txt";
    }

    public enum Engine
    {
        GZDOOM, PRBOOM, CHOC, RETRO, BLAKE, ROTT, WOLF, QUAKESDL, QUAKEDP, QUAKEFTEQW, QUAKE2, YQUAKE2, IOQUAKE3, HEXEN2, ZANDRONUM, LZDOOM, D3ES,
        RAZE_DUKE, RAZE_SW, RAZE_BLOOD, RAZE_REDNECK, RAZE_NAM, RAZE_POWERSLAVE, EDUKE32, WRATH, EDUKE32_IONFURY,
        }
}
