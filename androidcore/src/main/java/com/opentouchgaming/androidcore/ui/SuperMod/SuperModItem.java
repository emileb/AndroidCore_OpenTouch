package com.opentouchgaming.androidcore.ui.SuperMod;

import com.opentouchgaming.androidcore.GameEngine;
import com.opentouchgaming.androidcore.common.CustomArgs;

import java.io.Serializable;

public class SuperModItem implements Serializable
{

    static final long serialVersionUID = 1;

    public String title;
    public GameEngine.Engine engine;
    public int version;
    public String subgameTag;
    public CustomArgs customArgs;
    public long lastPlayed;

    String gameTypeImage;

    String modImage; // null = none, "zipfile:filename" = image in zip/wad file

    public SuperModItem(GameEngine.Engine engine, int version, String subgameTag, String image, CustomArgs customArgs)
    {
        this.engine = engine;
        this.version = version;
        this.subgameTag = subgameTag;
        this.customArgs = customArgs;
        this.gameTypeImage = image;

        lastPlayed = 0;
        title = "";
    }

    SuperModItem(SuperModItem item)
    {
        engine = item.engine;
        title = item.title;
        version = item.version;
        subgameTag = item.subgameTag;
        customArgs = new CustomArgs(item.customArgs);
        lastPlayed = item.lastPlayed;
        gameTypeImage = item.gameTypeImage;
    }
}
