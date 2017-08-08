package com.opentouchgaming.androidcore.controls;

import java.util.ArrayList;

/**
 * Created by Emile on 03/08/2017.
 */

public class ActionInputDefinition
{
    String filename;
    ArrayList<ActionInput> actions = new ArrayList<>();

    public ActionInputDefinition(String filename)
    {
        this.filename = filename;
    }

    public void addAction(String tag, String description, ActionInput.ActionType actionType, int actionCode, ActionInput.SourceType sourceType, int source)
    {
        ActionInput a = new ActionInput(tag,description,actionType,actionCode,sourceType,source);
        actions.add(a);
    }
}
