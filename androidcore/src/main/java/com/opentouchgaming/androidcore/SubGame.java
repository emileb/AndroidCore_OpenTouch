package com.opentouchgaming.androidcore;

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

    public SubGame( String title, String path, int image, String detail1, String detail2 )
    {
        this.path = path;
        this.title = title;
        this.image = image;
        this.detail1 = detail1;
        this.detail2 =  detail2;
        this.imagePng = null;
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


    public boolean selected;
}
