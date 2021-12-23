package com.opentouchgaming.androidcore.common;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.GameEngine;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Emile on 20/08/2017.
 */

public class CustomArgs implements Serializable
{
    static final long serialVersionUID = 1;


    private ArrayList<String> files = new ArrayList<>();
    private String args = "";

    CustomArgs()
    {

    }

    //Copy constructor
    public CustomArgs(CustomArgs c)
    {
        copy(c);
    }

    public void copy(CustomArgs c)
    {
        args = c.args;
        files.clear();
        //This is ok because strings are imutable
        files.addAll(c.files);
    }

    public boolean isEmpty()
    {
        return (getFiles().isEmpty() && args.contentEquals(""));
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
        }
        else
            return s;
    }

    private String buildFileType(String[] extensions, String option, boolean combine)
    {
        String result = "";
        boolean foundFile = false;
        for (int n = 0; n < getFiles().size(); n++)
        {
            boolean match = false;
            for (String ext : extensions)
            {
                if (getFiles().get(n).toLowerCase().endsWith(ext))
                    match = true;
            }

            if (match)
            {
                if (combine) // Combine so only one option
                {
                    if (!foundFile)
                    {
                        result += option + " ";
                        foundFile = true;
                    }
                    result += quote(getFiles().get(n)) + " ";
                }
                else // Option switch for each file
                {
                    result += option + " " + quote(getFiles().get(n)) + " ";
                }
            }
        }

        return result;
    }

    public String getModsString()
    {
        String result = "";
        if ((AppInfo.currentEngine.engine == GameEngine.Engine.GZDOOM) ||
            (AppInfo.currentEngine.engine == GameEngine.Engine.ZANDRONUM || (AppInfo.currentEngine.engine == GameEngine.Engine.LZDOOM)))//This is a bit shit referring to this...
        {
            result += buildFileType(new String[]{".wad", ".pk3", ".pk7", ".zip"}, "-file ", false);
            result += buildFileType(new String[]{".deh", ".bex"}, "-deh ", false);
            result += buildFileType(new String[]{".lmp"}, "-playdemo ", true);
            result += buildFileType(new String[]{".sf2"}, "+set fluid_patchset ", true);
        }
        else if (AppInfo.currentEngine.engine == GameEngine.Engine.PRBOOM)
        {
            result += buildFileType(new String[]{".wad", ".pk3", ".pk7"}, "-file ", true);
            result += buildFileType(new String[]{".deh", ".bex"}, "-deh ", true);
            result += buildFileType(new String[]{".lmp"}, "-playdemo ", true);
        }
        else if (AppInfo.currentEngine.engine == GameEngine.Engine.CHOC)
        {
            result += buildFileType(new String[]{".wad"}, "-merge ", true);
            result += buildFileType(new String[]{".deh", ".bex"}, "-deh ", true);
            result += buildFileType(new String[]{".lmp"}, "-playdemo ", true);

        }
        else if (AppInfo.currentEngine.engine == GameEngine.Engine.RETRO)
        {
            result += buildFileType(new String[]{".wad", ".pk3", ".pk7"}, "-file ", false);
            result += buildFileType(new String[]{".deh", ".bex"}, "-deh ", false);
            result += buildFileType(new String[]{".lmp"}, "-playdemo ", true);
        }
        else if ((AppInfo.currentEngine.engine == GameEngine.Engine.RAZE_DUKE) || (AppInfo.currentEngine.engine == GameEngine.Engine.RAZE_BLOOD) ||
                 (AppInfo.currentEngine.engine == GameEngine.Engine.RAZE_NAM) || (AppInfo.currentEngine.engine == GameEngine.Engine.RAZE_POWERSLAVE) ||
                 (AppInfo.currentEngine.engine == GameEngine.Engine.RAZE_SW) || (AppInfo.currentEngine.engine == GameEngine.Engine.RAZE_REDNECK))
        {
            result += buildFileType(new String[]{".grp", ".zip"}, "-file ", false);
        }
        else if ((AppInfo.currentEngine.engine == GameEngine.Engine.EDUKE32) || (AppInfo.currentEngine.engine == GameEngine.Engine.EDUKE32_IONFURY))
        {
            result += buildFileType(new String[]{".grp", ".zip"}, "-g ", false);
            result += buildFileType(new String[]{".map"}, "-map ", false);
            result += buildFileType(new String[]{".con"}, "-mx ", false);
        }
        return result;
    }

    public String getArgsString()
    {
        return args;
    }

    public String getFinalArgs()
    {
        return getArgsString() + " " + getModsString();
    }

    public void setFiles(ArrayList<String> files)
    {
        this.files = files;
    }

    public ArrayList<String> getFiles()
    {
        if (files == null)
            files = new ArrayList<>();
        return files;
    }
}
