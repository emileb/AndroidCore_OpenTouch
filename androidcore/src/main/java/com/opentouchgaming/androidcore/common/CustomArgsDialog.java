package com.opentouchgaming.androidcore.common;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.opentouchgaming.androidcore.R;

public class CustomArgsDialog
{
    public final Dialog dialog;
    final EngineData engineData;
    String basePath;
    String extraPath = "";
    Activity activity;
    TextView customModsTextView;
    EditText customArgsEditText;

    public CustomArgsDialog(final Activity act, final String path, EngineData ed)
    {
        basePath = path;
        activity = act;
        this.engineData = ed;

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom_args);
        dialog.setCancelable(true);

        dialog.setOnKeyListener(new Dialog.OnKeyListener()
        {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event)
            {

                return false;
            }
        });

        customArgsEditText = (EditText) dialog.findViewById(R.id.editText_custom_args);

        ImageButton clearArgs = (ImageButton) dialog.findViewById(R.id.imagebutton_clear_custom);
        clearArgs.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                engineData.getCurrentCustomArgs().setArgs("");
                update();
            }
        });

        ImageButton history = (ImageButton) dialog.findViewById(R.id.imagebutton_args_history);
        history.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new CustomArgsHistoryDialog(act, engineData.getArgsHistory())
                {
                    public void selected(int position)
                    {
                        // Set the current args to one of the ones in history
                        engineData.getCurrentCustomArgs().setArgs(engineData.getArgsHistory().get(position).getArgsString());
                        update();
                    }
                };
            }
        });

        customArgsEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                engineData.getCurrentCustomArgs().setArgs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
        update();
        dialog.show();
    }

    private void update()
    {
        customArgsEditText.setText(engineData.getCurrentCustomArgs().getArgsString());
    }

    public void resultResult(String result)
    {

    }


}
