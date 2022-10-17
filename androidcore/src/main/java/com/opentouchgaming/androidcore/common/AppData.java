package com.opentouchgaming.androidcore.common;

import static com.opentouchgaming.androidcore.DebugLog.Level.D;
import static com.opentouchgaming.androidcore.DebugLog.Level.E;
import static com.opentouchgaming.androidcore.DebugLog.Level.I;

import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.GameEngine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EnumMap;

/**
 * Created by Emile on 24/08/2017.
 */

public class AppData implements Serializable
{
    static final long serialVersionUID = 1;

    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.APP, "AppData");
    }


    private final EnumMap<GameEngine.Engine, EngineData> engineData = new EnumMap<>(GameEngine.Engine.class);

    public static void saveToFile(String file, AppData appData)
    {
        log.log(I, "Saving data to " + file);
        FileOutputStream fos = null;
        ObjectOutputStream out = null;

        try
        {
            fos = new FileOutputStream(file);
            out = new ObjectOutputStream(fos);
            out.writeObject(appData);
            out.close();
        }
        catch (FileNotFoundException e)
        {
            log.log(E, "Could not open file " + file + " :" + e);
            e.printStackTrace();
        }
        catch (IOException e)
        {
            log.log(E, "Error writing file " + file + " :" + e);
            e.printStackTrace();
        }
    }

    public static AppData loadFromFile(String file)
    {
        log.log(I, "Loading data from " + file);

        AppData data = null;

        try
        {
            InputStream fis = null;
            ObjectInputStream in = null;

            fis = new FileInputStream(file);
            in = new ObjectInputStream(fis);

            AppData d = (AppData) in.readObject();
            data = d;
            log.log(I, "File " + file + " loaded");
        }
        catch (FileNotFoundException e)
        {
            log.log(I, "File " + file + " not found");
        }
        catch (IOException e)
        {
            log.log(E, "Could not open file " + file + " :" + e);

        }
        catch (ClassNotFoundException e)
        {
            log.log(E, "Error reading file " + file + " :" + e);
        }

        //Failed to open
        if (data == null)
        {
            data = new AppData();
        }

        return data;
    }

    public EngineData getEngineData(GameEngine.Engine engine)
    {
        EngineData data = engineData.get(engine);

        // This will happen first time
        if (data == null)
        {
            log.log(D, "Could not find EngineData for " + engine.toString());
            data = new EngineData();
            engineData.put(engine, data);
        }
        else
        {
            log.log(D, "Found EngineData for " + engine.toString());
        }

        return data;
    }
}
