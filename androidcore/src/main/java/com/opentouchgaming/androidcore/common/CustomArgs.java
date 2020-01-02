package com.opentouchgaming.androidcore.common;

import java.io.Serializable;

/**
 * Created by Emile on 20/08/2017.
 */

public class CustomArgs implements Serializable
{
    static final long serialVersionUID = 1;

    private String args = "";

    CustomArgs()
    {

    }

    //Copy constructor
    CustomArgs(CustomArgs c)
    {
        args = c.args;

    }

    public boolean isEmpty()
    {
        return ( args.contentEquals(""));
    }

    public void setArgs(String args)
    {
        this.args = args;
    }

    private String quote(String s)
    {
        if (s.contains(" "))
        {
            return "\"" + s + "\"";
        } else return s;
    }


    public String getArgsString()
    {
        return args;
    }

    public String getFinalArgs()
    {
        return getArgsString();
    }
}
