package com.opentouchgaming.androidcore.controls;

import android.app.Activity;

import java.io.Serializable;


public class ActionInput implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1L;
    public String tag;
    public String description;
    public boolean invert;
    public float scale = 1; //sensitivity for analog
    public float deadZone = 0.2f;
    public ActionType actionType;
    public int actionCode;
    public SourceType sourceType;
    public int source = -1;
    public boolean sourcePositive = true; //Used when using analog as a button
    public int shift = 0; // 0 = none, 1/2 = only active while that shift button is held
    transient ActionInputExtra extraDialog;

    public ActionInput(String tag, String description, ActionType actionType, int actionCode, SourceType sourceType, int source, ActionInputExtra extraDialog)
    {
        this.tag = tag;
        this.description = description;
        this.actionCode = actionCode;
        this.actionType = actionType;

        this.sourceType = sourceType;
        this.source = source;
        //this.sourcePositive = sourcePositive;
        this.extraDialog = extraDialog;
    }

    public String toString()
    {
        return description + " : " + sourceType.toString() + " source: " + source + " sourcePositive: " + sourcePositive + " shift: " + shift;
    }

    public boolean isShift()
    {
        return actionCode == PortActDefs.PORT_ACT_SHIFT_1 || actionCode == PortActDefs.PORT_ACT_SHIFT_2;
    }

    // 1 or 2 for shift actions, 0 otherwise
    public int shiftIndex()
    {
        return isShift() ? actionCode - PortActDefs.PORT_ACT_SHIFT_1 + 1 : 0;
    }

    @Override
    protected ActionInput clone()
    {
        ActionInput clone = null;
        try
        {
            clone = (ActionInput) super.clone();

        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e); // won't happen
        }

        return clone;
    }

    public enum ActionType
    {
        ANALOG, BUTTON, MENU
    }


    public enum SourceType
    {
        AXIS, BUTTON
    }

    public interface ActionInputExtra
    {
        void show(Activity activity, ActionInput action, Runnable runnable);
    }

}

