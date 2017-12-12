package com.opentouchgaming.androidcore;

import android.app.Activity;
import android.app.Dialog;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Emile on 31/10/2017.
 */

public class LogViewDialog
{

    TextView textView;

    Activity activity;

    public LogViewDialog(final Activity act, String file)
    {
        activity = act;

        final Dialog dialog = new Dialog(act);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        dialog.setContentView(R.layout.dialog_log_view);

        dialog.setTitle("Game log");

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        textView = (TextView)dialog.findViewById(R.id.textView);
        textView.setText(readFile(file));

        dialog.show();
    }

    String readFile(String fileName)
    {
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null)
            {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {

        }
        return "--No log file--";
    }

}
