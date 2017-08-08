package com.opentouchgaming.androidcore.controls;

import java.io.Serializable;


public class ActionInput implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum ActionType
    {
        ANALOG, BUTTON, MENU
    }

    public enum SourceType
    {
        AXIS,
        BUTTON
    }

    public String tag;
    public String description;
    public boolean invert;
    public float scale = 1; //senstivty for analog

    public ActionType actionType;
    public int actionCode;

    public SourceType sourceType;
    public int source = -1;
    public boolean sourcePositive = true; //Used when using analog as a button


    public ActionInput(String tag, String description, ActionType actionType, int actionCode, SourceType sourceType, int source)
    {
        this.tag = tag;
        this.description = description;
        this.actionCode = actionCode;
        this.actionType = actionType;

        this.sourceType = sourceType;
        this.source = source;
        //this.sourcePositive = sourcePositive;
    }



    public String toString()
    {
        return description + " : " + sourceType.toString() + " source: " + source + " sourcePositive: " + sourcePositive;
    }


}

