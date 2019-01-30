package com.opentouchgaming.androidcore;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Window;
import android.widget.EditText;

/**
 * Created by Emile on 20/05/2018.
 */

public class SubGame
{
    String title;
    int image;
    String imagePng;
    String detail1;
    String detail2;

    String path;
    int gameType;

    String tag; // unique tag for each sub-game

    String downloadPath;
    String downloadFilename;

    String extraArgs;


    public SubGame(String tag, String title, String path, int gameType, int image, String detail1, String detail2)
    {
        this.tag = tag;
        this.path = path;
        this.title = title;
        this.image = image;
        this.detail1 = detail1;
        this.detail2 = detail2;
        this.imagePng = null;
        this.gameType = gameType;
    }

    public void load(Context ctx)
    {
        if (tag != null)
        {
            String title = AppSettings.getStringOption(ctx, tag + "title", null);
            if (title != null)
                setTitle(title);
        }
    }


    public void save(Context ctx)
    {
        if (tag != null)
        {
            AppSettings.setStringOption(ctx, tag + "title", title);
        }
    }

    public String getExtraArgs()
    {
        if (extraArgs == null)
            return "";
        else
            return extraArgs;
    }

    public void setExtraArgs(String extraArgs)
    {
        this.extraArgs = extraArgs;
    }

    public void setDownloadInfo(String path, String filename)
    {
        downloadPath = path;
        downloadFilename = filename;
    }

    public String getDownloadPath()
    {
        return downloadPath;
    }

    public String getDownloadFilename()
    {
        return downloadFilename;
    }

    public String getImagePng()
    {
        return imagePng;
    }

    public void setImagePng(String imagePng)
    {
        this.imagePng = imagePng;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public int getImage()
    {
        return image;
    }

    public void setImage(int image)
    {
        this.image = image;
    }

    public String getDetail1()
    {
        return detail1;
    }

    public void setDetail1(String detail1)
    {
        this.detail1 = detail1;
    }

    public String getDetail2()
    {
        return detail2;
    }

    public void setDetail2(String detail2)
    {
        this.detail2 = detail2;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public int getGameType()
    {
        return gameType;
    }

    public void setGameType(int gameType)
    {
        this.gameType = gameType;
    }

    public boolean selected;

    public interface DialogCallback{
        void dismiss();
    }

    public void edit(final Activity act, final DialogCallback callback)
    {
        Dialog dialog = new Dialog(act);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_subgame_options);
        dialog.setCancelable(true);


        final EditText iwadTitle = dialog.findViewById(R.id.subgame_title_edittext);
        iwadTitle.setText(getTitle());

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                setTitle(iwadTitle.getText().toString());
                save(act);
                if( callback!= null )
                {
                    callback.dismiss();
                }
            }
        });

        dialog.show();
    }

}
