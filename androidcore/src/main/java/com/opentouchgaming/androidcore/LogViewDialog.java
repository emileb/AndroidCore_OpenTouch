package com.opentouchgaming.androidcore;

import android.app.Activity;
import android.app.Dialog;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.opentouchgaming.saffal.FileSAF;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by Emile on 31/10/2017.
 */

public class LogViewDialog
{
    TextView textView;

    Activity activity;

    String text;

    public LogViewDialog(final Activity act, String file, String name)
    {
        activity = act;

        final Dialog dialog = new Dialog(act);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        dialog.setContentView(R.layout.dialog_log_view);

        dialog.setTitle("Game log for: " + name);

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        text = readFile(file);
        textView = dialog.findViewById(R.id.textView);
        textView.setText(text);

        Button copyButton = dialog.findViewById(R.id.copy_button);

        copyButton.setOnClickListener(view ->
                                      {
                                          Utils.copyToClipboard(act, AppInfo.title, text);
                                      });
        dialog.show();
    }

    String readFile(String fileName)
    {
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileSAF(fileName).getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null)
            {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "--No log file--";
    }

}
